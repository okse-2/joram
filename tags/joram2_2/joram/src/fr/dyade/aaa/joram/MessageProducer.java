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
import java.util.*;  
 
/** 
 *	a MessageProducer is as JMS specifications 
 * 
 *	@see fr.dyade.aaa.joram.TopicPublisher
 *	@see fr.dyade.aaa.joram.QueueReceiver
 *	@see javax.jms.MessageProducer 
  */ 
 
public class MessageProducer implements javax.jms.MessageProducer { 

	/** reference to the COnnection Object to retrieve Message from Connection */
	protected fr.dyade.aaa.joram.Connection refConnection;
	
	/** reference to the Session Object so send Message to the socket */
	protected fr.dyade.aaa.joram.Session refSession;
	
	/** boolean to disable the MessageID */
	protected boolean disableMessageID;
	
	/** boolean to disable the MessageTimeStamp */
	protected boolean disableMessageTimestamp;
	
	/** int to choice the DeliveryMode */
	protected int deliveryMode;
	
	/** int to choice the priority of the message */
	protected int priority;
	
    /** long to choice put the time to live of the message */
    protected long timeToLive;
	
	/** Constructor */
	public MessageProducer(fr.dyade.aaa.joram.Connection refConnectionNew, fr.dyade.aaa.joram.Session refSessionNew) {
		refConnection = refConnectionNew;
		refSession = refSessionNew;
		
		/* initialization of attributes by default */
		disableMessageID = false;
		disableMessageTimestamp = false;
		deliveryMode = fr.dyade.aaa.mom.Message.PERSISTENT;
		priority = 4;
		timeToLive = 0;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications 
	 *	don't make anything because the messageID is always set by the agentClient
	 */
	public void setDisableMessageID(boolean disableMessageID) throws javax.jms.JMSException {
		try {
			this.disableMessageID = disableMessageID;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public boolean getDisableMessageID() throws javax.jms.JMSException {
		try {
			return disableMessageID;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications 
	 *	don't make anything because the timesStamp is used in the MOM
	 *	value must be set
	 */
	public void setDisableMessageTimestamp(boolean disableMessageTimestamp) throws javax.jms.JMSException {
		try {
			this.disableMessageTimestamp = disableMessageTimestamp;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public boolean getDisableMessageTimestamp() throws javax.jms.JMSException {
		try {
			return disableMessageTimestamp;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void setDeliveryMode(int deliveryMode) throws javax.jms.JMSException {
		try {
			this.deliveryMode = deliveryMode;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public int getDeliveryMode() throws javax.jms.JMSException {
		try {
			return deliveryMode;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void setPriority(int priority) throws javax.jms.JMSException {
		try {
			this.priority = priority;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public int getPriority() throws javax.jms.JMSException {
		try {
			return priority;	
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public void setTimeToLive(long timeToLive) throws javax.jms.JMSException {
		try {
			this.timeToLive = timeToLive;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public long getTimeToLive() throws javax.jms.JMSException {
		try {
			return timeToLive;
		} catch (Exception exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
			except.setLinkedException(exc);
			throw(except);
		}
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications 
	 *	methode override by the QueueSender and TopicPublisher
	 */
	public void close() throws javax.jms.JMSException {
	  System.gc();
	}
}
