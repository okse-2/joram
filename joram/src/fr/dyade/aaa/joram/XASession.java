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

import javax.jms.*;
import javax.transaction.xa.*;
import java.util.*;
import java.io.*;
import fr.dyade.aaa.mom.*;

/**
 * XASession extends the capability of Session by adding access to a
 * JMS provider's support for JTA. This support takes the form of a
 * <code>javax.transaction.xa.XAResource</code> object. The functionality
 * of this object closely resembles that defined by the standard X/Open XA
 * Resource interface.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public abstract class XASession extends Session implements javax.jms.XASession {

    /** The XAResource associated to this session */
    private XAResource xar;

    /** The number of consumers */
    private int consumerCounter;

    /** The object both commit and rollback synchronizes on */
    //private Object synchroObj;

    /** The object containing the vectors of messages and acks to send. */
    public XidTable xidTable;


    //public XASession(long sessionID, XAConnection refConnection) {
    public XASession(long sessionID, Connection refConnection) {
	super(true, fr.dyade.aaa.mom.CommonClientAAA.TRANSACTED, sessionID, (Connection) refConnection);
	isClosed = false;

	xar = new XAResource(this);
	xidTable = new XidTable();
    }
    
    public javax.transaction.xa.XAResource getXAResource() {
	return xar;
    }

    public boolean getTransacted() throws JMSException {
	return true;
    }

    /**
     * Throws TransactionInProgressException since it should not be called
     * for an XASession object.
     */
    public void commit() throws JMSException {
	throw new TransactionInProgressException("Use of a session's commit is prohibited in a distributed transaction");
    }

    /**
     * Throws TransactionInProgressException since it should not be called
     * for an XASession object.
     */
    public void rollback() throws JMSException {
	throw new TransactionInProgressException("Use of a session's rollback is prohibited in a distributed transaction");
    }

    /**
     * Closes the session and runs the garbage collector.
     */
    public void close() throws JMSException {
	isClosed = true;
	System.gc();
    }


    public void recover() throws JMSException {
	throw new JMSException("Not implemented");
    }

    public void acknowledgeMessage(String messageID) throws JMSException {
	throw new JMSException("Prohibited in a distributed transaction");
    }

    
    protected Vector preparesTransactedAck(long messageJMSMOMID) throws JMSException {
	throw new JMSException("Forbidden function call");
    }


    protected abstract Vector preparesTransactedAck(javax.transaction.xa.Xid xid,
						    long messageJMSMOMID) throws JMSException;


    public void acknowledgeTransactedMessage(javax.transaction.xa.Xid xid) throws JMSException {
	throw new JMSException("Forbidden function call");
    }


    protected Vector preparesHandlyAck(String messageID, long messageJMSMOMID) throws JMSException {
	throw new JMSException("Forbidden function call");
    }


    protected abstract void rollbackDeliveryMsg() throws JMSException;


    protected abstract Vector createAckRollbackVector(javax.transaction.xa.Xid xid) throws JMSException;
    

    /**
     * Send a message via the connection and wait for the answer.
     */
    protected MessageMOMExtern sendMessageGetAnswer(MessageMOMExtern msg) throws JMSException {
	Long messageID = new Long(msg.getMessageMOMExternID());
	Object synchroObj = new Object();

	synchronized(synchroObj) {
	    refConnection.waitThreadTable.put(messageID, synchroObj);
	    sendToConnection(msg);
	    try {
		synchroObj.wait();
	    } catch (InterruptedException ie) {
		ie.printStackTrace();
	    }
	}
	return ((MessageMOMExtern) refConnection.messageJMSMOMTable.remove(messageID));
    }


    /**
     * Get the vector of messages to send.
     */
    public abstract Vector getMessageToSendVector();


    /**
     * Set the vector of messages to send.
     */
    public abstract void setMessageToSendVector(Vector v);


    /*
     * Get the vector of acks to send.
     */
    public abstract Vector getMessageToAckVector();


    /**
     * Set the vector of acks to send.
     */
    public abstract void setMessageToAckVector(Vector v);

} // XASession
