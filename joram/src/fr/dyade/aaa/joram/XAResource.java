/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.joram;

import java.util.*;
import javax.jms.*;
import javax.transaction.xa.*;
import fr.dyade.aaa.mom.*;

/**
 * The aim of this implementation of XAResource is to be used with
 * Jonas. It does not fully match the JTA specification (for example
 * it isn't thread safe).
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class XAResource implements javax.transaction.xa.XAResource {
    
    private boolean isStarted;
    private XASession refSession;
    private javax.transaction.xa.Xid currentXid;

    private Object synchroObject;

    public XAResource(XASession refSession) {
	isStarted = false;
	this.refSession = refSession;
	synchroObject = new Object();
    }


    /**
     * Start work on behalf of a transaction branch specified in xid.
     */
    public void start(javax.transaction.xa.Xid xid, int flags) throws XAException {
	
	if (isStarted)
	    throw new XAException("A transaction is already started");
	isStarted = true;
	currentXid = xid;
	
	/*
	 * If neither TMJOIN nor TMRESUME is specified and the
	 * transaction specified by xid has previously been seen
	 * by the resource manager, the resource manager throws
	 * the XAException exception with XAER_DUPID error code.
	 */
	if (refSession.xidTable.registeredXid(xid)) {
	    switch (flags) {
	    case TMJOIN:
		throw new XAException("Unimplemented flag");
	    case TMRESUME: // Resume a transaction
		try {
		    if (refSession.xidTable.getXidStatus(xid) != XidTable.SUSPENDED) {
			throw new XAException("You are trying to resume a non suspended transaction");
		    } else {
			// Restore this transaction from the hashtables
			refSession.xidTable.setXidStatus(xid, XidTable.ACTIVE);
			refSession.setMessageToSendVector(new Vector(refSession.xidTable.getMessageToSendXid(xid)));
			refSession.setMessageToAckVector(new Vector(refSession.xidTable.getMessageToAckXid(xid)));
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new XAException();
		}
		break;
	    case TMNOFLAGS:
		throw new XAException(XAException.XAER_DUPID);
	    default: // Bad flags
		throw new XAException(XAException.XAER_INVAL);
	    }
	} else {
	    if (flags != TMNOFLAGS)
		throw new XAException("Invalid or unimplemented flag");
	    try {
		refSession.xidTable.setXidStatus(xid, XidTable.ACTIVE);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new XAException();
	    }
	}
	
    }


    /*
     * Ends the work performed on behalf of a transaction branch.
     */
    public void end(javax.transaction.xa.Xid xid, int flags) throws XAException {
	int status;

	if (!isStarted)
	    throw new XAException("No transaction currently running");
	isStarted = false;

	if (xid != currentXid)
	    throw new XAException("You didn't specify the good xid");

	try {
	    status = refSession.xidTable.getXidStatus(xid);
	} catch (Exception e) {
	    throw new XAException();
	}
	if (status != XidTable.ACTIVE) {
	    throw new XAException("Invalid Xid state");
	}

	/*
	 * If TMSUSPEND is specified in flags, the transaction branch is
	 * temporarily suspended in incomplete state. The transaction context
	 * is in suspened state and must be resumed via start with TMRESUME
	 * specified.
	 *
	 * If TMFAIL is specified, the portion of work has failed. The
	 * resource manager may mark the transaction as rollback-only.
	 *
	 * If TMSUCCESS is specified, the portion of work has completed
	 * successfully.
	 */
	if (refSession.xidTable.registeredXid(xid)) {
	    
	    switch (flags) {
	    case TMSUSPEND:
		try {
		    refSession.xidTable.setXidStatus(xid, XidTable.SUSPENDED);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new XAException();
		}
		break;
	    case TMFAIL:
		try {
		    refSession.xidTable.setXidStatus(xid, XidTable.RB_ONLY);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new XAException();
		}
		break;
	    case TMSUCCESS:
		try {
		    refSession.xidTable.setXidStatus(xid, XidTable.SUCCESS);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new XAException();
		}
		break;
	    default:
		throw new XAException("Invalid flag");
	    }
	    // Saves the current transaction in the hashtables
	    try {
		refSession.xidTable.setXid(xid, refSession.getMessageToSendVector(),
					   refSession.getMessageToAckVector());
		refSession.setMessageToSendVector(new Vector());
		refSession.setMessageToAckVector(new Vector());
	    } catch (Exception e) {
		throw new XAException();
	    }
	} else {
	    throw new XAException("This transaction was not started");
	}

    }


    /**
     * Ask the resource manager to prepare for a transaction commit
     * of the transaction specified in xid.
     */
    public int prepare(javax.transaction.xa.Xid xid) throws XAException {
	MessageMOMExtern msgRecv;
	Long messageID = new Long(refSession.refConnection.getMessageMOMID());
	Vector ackVector;
	Vector msgVector;

	if (!refSession.xidTable.registeredXid(xid))
	    throw new XAException("Transaction not registered");

	try {
	    if (refSession.xidTable.getXidStatus(xid) != XidTable.SUCCESS)
		throw new XAException("Can't prepare this transaction");
	    
	    // Prepare the acknowledgments
	    ackVector = refSession.preparesTransactedAck(xid, messageID.longValue());

	    // Prepare the messages
	    msgVector = refSession.xidTable.getMessageToSendXid(xid);

	    // Send the vectors
	    msgRecv = refSession.sendMessageGetAnswer(new MessageXAPrepare(refSession.refConnection.getMessageMOMID(), ackVector, msgVector, xid));
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new XAException();
	}

	// Analyze the answer
	if (msgRecv instanceof MessageAckXAPrepare) {
	    try {
		refSession.xidTable.setXidStatus(xid, XidTable.PREPARED);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new XAException();
	    }
	    return XA_OK;
	} else if (msgRecv instanceof ExceptionMessageMOMExtern) {
	    try {
		refSession.xidTable.setXidStatus(xid, XidTable.RB_ONLY);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    ((ExceptionMessageMOMExtern) msgRecv).exception.printStackTrace();
	    throw new XAException("Error during the transaction's prepare");
	} else {
	    try {
		refSession.xidTable.setXidStatus(xid, XidTable.RB_ONLY);
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    throw new XAException("Error during the transaction's prepare");
	}
    }


    /**
     * Commit the global transaction specified by xid.
     */
    public void commit(javax.transaction.xa.Xid xid, boolean onePhase) throws XAException {
	MessageMOMExtern msgRecv;

	if (!refSession.xidTable.registeredXid(xid))
	    throw new XAException("Transaction not registered");
	
	try {
	    if (refSession.xidTable.getXidStatus(xid) == XidTable.RB_ONLY)
		throw new XAException("Only a rollback is possible for this transaction");
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new XAException();
	}

	// Perform the 2-phase protocol in place of the user
	if (onePhase) { // Prepare before commit
	    if (prepare(xid) != XA_OK)
		throw new XAException("One phase commit failed");
	}

	try {
	    if (refSession.xidTable.getXidStatus(xid) != XidTable.PREPARED)
		throw new XAException("Transaction not prepared");
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new XAException();
	}
	
	// Commit
	try {
	    msgRecv = refSession.sendMessageGetAnswer(new MessageXACommit(refSession.refConnection.getMessageMOMID(), xid));
	} catch (JMSException jmse) {
	    jmse.printStackTrace();
	    throw new XAException();
	}
    
	if (msgRecv instanceof MessageAckXACommit) {
	    try {
		refSession.xidTable.setXidStatus(xid, XidTable.COMMITTED);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new XAException();
	    }
	} else if (msgRecv instanceof ExceptionMessageMOMExtern) {
	    ((ExceptionMessageMOMExtern) msgRecv).exception.printStackTrace();
	    throw new XAException("Error during the transaction's prepare");
	} else {
	    throw new XAException("Error during the transaction's prepare");
	}
    }


    /**
     * Obtain a list of prepared transaction branches from a resource manager.
     */
    public javax.transaction.xa.Xid[] recover(int flag) throws XAException {
	MessageMOMExtern msgRecv;

	if (flag == TMSTARTRSCAN || flag == TMENDRSCAN)
	    throw new XAException("Not implemented");

	try {
	    msgRecv = refSession.sendMessageGetAnswer(new MessageXARecover(refSession.refConnection.getMessageMOMID()));
	} catch (JMSException jmse) {
	    jmse.printStackTrace();
	    throw new XAException();
	}

	if (msgRecv instanceof MessageAckXARecover) {
	    return ((MessageAckXARecover) msgRecv).msgRecover;
	} else if (msgRecv instanceof ExceptionMessageMOMExtern) {
	    ((ExceptionMessageMOMExtern) msgRecv).exception.printStackTrace();
	    throw new XAException("Error during the transaction's recover");
	} else {
	    throw new XAException("Error during the transaction's recover");
	}
    }


    /**
     * Inform the resource manager to roll back work done on behalf of a
     * transaction branch.
     */
    public void rollback(javax.transaction.xa.Xid xid) throws XAException {
	MessageMOMExtern msgRecv;
	Vector msgToRollbackVector;
	Vector msgSent;

 	if (!refSession.xidTable.registeredXid(xid))
 	    throw new XAException("Transaction not registered");

 	if (refSession.xidTable.getXidStatus(xid) == XidTable.ACTIVE)
 	    throw new XAException("Cannot rollback an active transaction");
	
	refSession.msgNoDeliveredQueue.remove();
	
	if (refSession.xidTable.getXidStatus(xid) == XidTable.PREPARED) {
	    try {
		msgToRollbackVector = refSession.createAckRollbackVector(xid);
		msgSent = refSession.xidTable.getMessageToSendXid(xid);
	    } catch (Exception e) {
		e.printStackTrace();
		throw new XAException();
	    }
	    System.out.println(msgToRollbackVector);
	    
	    // Send a message to the proxy
	    try {
		msgRecv = refSession.sendMessageGetAnswer(new MessageXARollback(refSession.refConnection.getMessageMOMID(), xid, msgToRollbackVector));
	    } catch (JMSException jmse) {
		jmse.printStackTrace();
		throw new XAException();
	    }

	    if (msgRecv instanceof MessageAckXARollback) {
		// OK
	    } else if (msgRecv instanceof ExceptionMessageMOMExtern) {
		((ExceptionMessageMOMExtern) msgRecv).exception.printStackTrace();
		throw new XAException("Error during the transaction's rollback");
	    } else {
		    throw new XAException("Error during the transaction's rollback");
	    }
	}

	// Anyway put the Xid in the ROLLBACKED state
	try {
	    refSession.xidTable.removeXid(xid);
	    refSession.xidTable.setXidStatus(xid, XidTable.ROLLBACKED);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new XAException();
	}
    }


    /**
     * Tell the resource manager to forget about a heuristically completed
     * transaction branch.
     */
    public void forget(javax.transaction.xa.Xid xid) throws XAException {
	throw new XAException("Not implemented");
    }


    /**
     * Set the current transaction timeout value for this
     * <code>XAResource</code> instance.
     */
    public boolean setTransactionTimeout(int seconds) throws XAException {
	throw new XAException("Not implemented");
    }


    /**
     * Obtain the current transaction timeout value set for this
     * <code>XAResource</code> instance.
     */
    public int getTransactionTimeout() throws XAException {
	throw new XAException("Not implemented");
    }


    /**
     * This method is called to determine if the resource manager instance
     * represented by the target object is the same as the
     * resource manager instance represented by the parameter <i>xares</i>.
     */
    public boolean isSameRM(javax.transaction.xa.XAResource xares) throws XAException {
	if (xares.equals(this))
	    return true;
	else
	    return false;
    }


} // XAResource
