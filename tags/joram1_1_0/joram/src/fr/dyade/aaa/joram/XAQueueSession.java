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
 * An XAQueueSession provides a regular QueueSession which can be used to
 * create QueueReceivers, QueueSenders and QueueBrowsers. 
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class XAQueueSession extends XASession implements javax.jms.XAQueueSession {
    
    /** The associated QueueSession */
    protected QueueSession qs;
    
    /**
     * Creates a new XAQueueSession.
     */
    protected XAQueueSession(long sessionID, XAConnection refConnection) {
	super(sessionID, refConnection);
	this.sessionID = sessionID;
	this.refConnection = refConnection;
 	qs = new QueueSession(true, fr.dyade.aaa.mom.CommonClientAAA.TRANSACTED,
			      sessionID, (Connection) refConnection);
    }
    
    /**
     * Get the queue session associated with this XAQueueSession.
     */
    public javax.jms.QueueSession getQueueSession() throws JMSException {
 	return qs;
    }

    protected void rollbackDeliveryMsg() throws JMSException {
	throw new JMSException("Forbidden function call");
    }

    protected Vector createAckRollbackVector(javax.transaction.xa.Xid xid) throws JMSException {
	Vector rollbackVector = new Vector();
	Vector msgToAckVector;

	try {
	    msgToAckVector = xidTable.getMessageToAckXid(xid);
	} catch (Exception e) {
	    JMSException jmse = new JMSException("Internal error");
	    jmse.setLinkedException(e);
	    throw jmse;
	}
	if (msgToAckVector == null) return null;
	// Create the vector of messages for rollback
	while (!msgToAckVector.isEmpty()) {
	    MessageQueueDeliverMOMExtern currentMsg = (MessageQueueDeliverMOMExtern) msgToAckVector.remove(msgToAckVector.size() - 1);
	    javax.jms.Destination destination = (javax.jms.Destination) currentMsg.message.getJMSDestination();
	    String messageMOMID = currentMsg.message.getJMSMessageID();
	    MessageRollbackMOMExtern msgRollback = new MessageRollbackMOMExtern(currentMsg.getMessageMOMExternID(),
										currentMsg.message.getJMSDestination(),
										new Long(sessionID).toString(),
										currentMsg.message.getJMSMessageID());
	    rollbackVector.addElement(msgRollback);
	}
	return rollbackVector;
    }

    protected Vector preparesTransactedAck(javax.transaction.xa.Xid xid,
					   long messageJMSMOMID) throws JMSException {
	Vector messageToAckVector;
	Hashtable ackTable = new Hashtable();
	MessageQueueDeliverMOMExtern msgFromMOM;
	javax.jms.Message message;
	QueueNaming destination;

	try {
	    messageToAckVector = xidTable.getMessageToAckXid(xid);
	} catch (Exception e) {
	    throw new JMSException("Internal error");
	}
	if (messageToAckVector == null) return null;
	int index = messageToAckVector.size() - 1; // The last element of the vector
	while (index >= 0) {
	    msgFromMOM = (MessageQueueDeliverMOMExtern) messageToAckVector.elementAt(index);
	    message = msgFromMOM.message;
	    destination = (QueueNaming) message.getJMSDestination();

	    if (!ackTable.containsKey(destination)) {
		ackTable.put(message.getJMSDestination(),
			     new AckQueueMessageMOMExtern(messageJMSMOMID, destination,
							  message.getJMSMessageID(),
							  CommonClientAAA.TRANSACTED,
							  new Long(sessionID).toString()));
	    }
	    index--;
	}
	return new Vector(ackTable.values());
    }


    protected RecoverObject[] preparesRecover() throws JMSException {
	throw new JMSException("Not implemented");
    }


    public Vector getMessageToSendVector() {
	return qs.transactedMessageToSendVector;
    }


    public void setMessageToSendVector(Vector v) {
	qs.transactedMessageToSendVector = v;
    }


    public Vector getMessageToAckVector() {
	return qs.transactedMessageToAckVector;
    }


    public void setMessageToAckVector(Vector v) {
	qs.transactedMessageToAckVector = v;
    }

} // XAQueueSession
