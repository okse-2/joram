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
 * An XATopicSession provides a regular TopicSession which can be used to
 * create TopicSubscribers and TopicPublishers.
 *
 * @author Laurent Chauvirey
 * @version 1.0
 */

public class XATopicSession extends XASession implements javax.jms.XATopicSession {

    /** The associated TopicSession */
    protected TopicSession ts;

    /**
     * Creates a new XATopicSession.
     */
    public XATopicSession(long sessionID, XAConnection refConnection) {
	super(sessionID, refConnection);
	this.sessionID = sessionID;
	this.refConnection = refConnection;
 	ts = new TopicSession(true, fr.dyade.aaa.mom.CommonClientAAA.TRANSACTED,
			      sessionID, (Connection) refConnection);
    }


    /**
     * Get the topic session associated with this XATopicSession.
     */
    public javax.jms.TopicSession getTopicSession() {
	return ts;
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
	    MessageTopicDeliverMOMExtern currentMsg = (MessageTopicDeliverMOMExtern) msgToAckVector.remove(0);
	    TopicNaming topic = new TopicNaming(((TopicNaming) currentMsg.message.getJMSDestination()).getTopicName(),
						currentMsg.theme);
	    AckTopicMessageMOMExtern msgAck = new AckTopicMessageMOMExtern(refConnection.getMessageMOMID(), topic,
									   currentMsg.nameSubscription,
									   currentMsg.message.getJMSMessageID(),
									   CommonClientAAA.AUTO_ACKNOWLEDGE);
	    rollbackVector.addElement(msgAck);
	}
	return rollbackVector;
    }


    protected Vector preparesTransactedAck(javax.transaction.xa.Xid xid,
					   long messageJMSMOMID) throws JMSException {
	Vector messageToAckVector;
	Hashtable ackTable = new Hashtable();
	MessageTopicDeliverMOMExtern msgFromMOM;
	javax.jms.Message message;
	TopicNaming destination;
	
	try {
	    messageToAckVector = xidTable.getMessageToAckXid(xid);
	} catch (Exception e) {
	    throw new JMSException("Internal error");
	}
	if (messageToAckVector == null) return null;
	int index = messageToAckVector.size() - 1; // The last element of the vector
	while (index >= 0) {
	    msgFromMOM = (MessageTopicDeliverMOMExtern) messageToAckVector.elementAt(index);
	    message = msgFromMOM.message;
	    destination = (TopicNaming) message.getJMSDestination();

	    if (!ackTable.containsKey(destination)) {
		ackTable.put(message.getJMSDestination(),
			     new AckTopicMessageMOMExtern(messageJMSMOMID, destination,
							  msgFromMOM.nameSubscription,
							  message.getJMSMessageID(),
							  CommonClientAAA.TRANSACTED));
	    }
	    index--;
	}
	return new Vector(ackTable.values());
    }


    protected RecoverObject[] preparesRecover() throws JMSException {
	throw new JMSException("Not implemented");
    }


    public Vector getMessageToSendVector() {
	return ts.transactedMessageToSendVector;
    }


    public void setMessageToSendVector(Vector v) {
	ts.transactedMessageToSendVector = v;
    }


    public Vector getMessageToAckVector() {
	return ts.transactedMessageToAckVector;
    }


    public void setMessageToAckVector(Vector v) {
	ts.transactedMessageToAckVector = v;
    }

} // XATopicSession
