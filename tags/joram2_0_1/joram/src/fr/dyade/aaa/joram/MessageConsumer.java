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

import java.lang.*; 
 
/** 
 *	a MessageConsumer is as JMS specifications 
 * 
 *	@see         subclasses
 *	javax.jms.MessageConsumer 
 */ 
 
public class MessageConsumer implements javax.jms.MessageConsumer { 
	
	/** reference to the COnnection Object to retrieve Message from Connection */
	protected fr.dyade.aaa.joram.Connection refConnection;
	
	/** reference to the Session Object so send Message to the socket */
	protected fr.dyade.aaa.joram.Session refSession;
	
	/** identifier of MessageConsumer : consumerID */
    protected String consumerID;
	
	/** the selector for the corresponding Queue/Topic */
	protected java.lang.String selector;
	
	/** the Message Listener associated to the MessageConsumer */
	protected javax.jms.MessageListener messageListener;
	
	/** this boolean allows to th session to only deliver a message
	 *	from the same Topic
	 *	false -> no delivery
	 */
	private boolean deliveryMessage = true;
	
	/*	this constructeur is uused due to in error in the super in queueReceiver 
	 *	"println"
	 */
	public MessageConsumer() {}

	/** constructor with selector chosen by the client */
    public MessageConsumer(String  consumerIDNew, fr.dyade.aaa.joram.Connection refConnectionNew, fr.dyade.aaa.joram.Session refSessionNew, String selectorNew) {
		consumerID = consumerIDNew;
		refConnection = refConnectionNew;
		refSession = refSessionNew;
		selector = selectorNew;
		messageListener = null;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public java.lang.String getMessageSelector() throws javax.jms.JMSException {
		return selector;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public javax.jms.MessageListener getMessageListener() throws javax.jms.JMSException {
		return messageListener;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void setMessageListener(javax.jms.MessageListener listener) throws javax.jms.JMSException {
		throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
	}

  public javax.jms.Message receive() throws javax.jms.JMSException
  {
    return receive((long) -1);
  }

  public javax.jms.Message receiveNoWait() throws javax.jms.JMSException
  {
    return receive(0);
  }

  public javax.jms.Message receive(long timeOut) throws javax.jms.JMSException
  {
    throw (new javax.jms.JMSException("Not implemented at this level"));
  }

	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void close()  throws javax.jms.JMSException {
		System.gc();
	}
	
	/** session updates the boolean to allow delivery or not */
	protected void setDeliveryMessage(boolean deliveryMessageNew) {
		deliveryMessage = deliveryMessageNew;
	}
	
	/** session reads the boolean to allow delivery or not */
	protected boolean getDeliveryMessage() {
		return deliveryMessage ;
	}
}
