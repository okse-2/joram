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
 *	a TopicPublisher is as JMS specifications 
 *	a TopicPublisher is an object associated with a real Topic
 *	in the MOM 
 * 
 *	@see fr.dyade.aaa.joram.TopicSession
 *	@see javax.jms.MessageProducer 
 *	@see javax.jms.TopicPublisher 
 */ 
 
public class TopicPublisher extends fr.dyade.aaa.joram.MessageProducer implements javax.jms.TopicPublisher { 
	
  /** the Topic associated to the TopicPublisher */
    javax.jms.Topic topic;
	
    public TopicPublisher(fr.dyade.aaa.joram.Connection refConnectionNew, fr.dyade.aaa.joram.Session refSessionNew, javax.jms.Topic topicNew) {
    super(refConnectionNew, refSessionNew);
    topic = topicNew;
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.Topic getTopic() throws javax.jms.JMSException {
    try {
      if(topic==null)
	throw (new fr.dyade.aaa.joram.JMSAAAException("Topic name Unknown",JMSAAAException.DEFAULT_JMSAAA_ERROR));
      else
	return topic;
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void publish(javax.jms.Message message) throws javax.jms.JMSException {
    this.publish(this.topic, message, super.deliveryMode, super.priority, super.timeToLive); 		 
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void publish(javax.jms.Message message, int deliveryMode, int priority, long timeToLive) throws javax.jms.JMSException {
    this.publish(this.topic, message, deliveryMode, priority, timeToLive);
  }									  
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void publish(javax.jms.Topic topic, javax.jms.Message message)  throws javax.jms.JMSException {
    this.publish(topic, message, super.deliveryMode, super.priority, super.timeToLive);
  }

  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void publish(javax.jms.Topic topic, javax.jms.Message message, int deliveryMode, int priority, long timeToLive) throws javax.jms.JMSException {
    try {
      /* padding the fields of the message */
      message.setJMSDestination(topic);
      message.setJMSDeliveryMode(deliveryMode);
      message.setJMSPriority(priority);
		
      if(timeToLive>0)
	message.setJMSExpiration(System.currentTimeMillis()+((long) timeToLive));
      else
	message.setJMSExpiration((long) 0);
		
      /* set the timestamp which will be updated by "Connection" */
      message.setJMSTimestamp(System.currentTimeMillis());
			
      /*	reset the message to put the mode in readOnly and to
       *	destroy the transient attributes
       */
      refSession.resetMessage(message);

      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      /* construction of the MessageJMSMOM */
      fr.dyade.aaa.mom.SendingMessageTopicMOMExtern msgSend = new fr.dyade.aaa.mom.SendingMessageTopicMOMExtern(messageJMSMOMID, (fr.dyade.aaa.mom.Message) message);
		
      if(refSession.transacted) {
	/* Topic not ack this msg, this msg is add in the vector and 
	 * CommonClientAAA acknoledge the vector */
	msgSend.message.setJMSDeliveryMode(1);
	/* add the message in the vector waiting for the commit */
	refSession.transactedMessageToSendVector.addElement(msgSend);		
      } else if(message.getJMSDeliveryMode()==fr.dyade.aaa.mom.Message.PERSISTENT) {
				/* deliver an agreement to the client if Persistent */
			
				/*	synchronization because it could arrive that the notify was
				 *	called before the wait 
				 */
	synchronized(obj) {
	  /* the processus of the client waits the response */
	  refConnection.waitThreadTable.put(longMsgID,obj);
	  /* sends the messageJMSMOM r */
	  refSession.sendToConnection(msgSend);
					
	  obj.wait();
	}
				/* the clients wakes up */
	fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
				/* tests if the key exists 
				 * dissociates the enumeration null and internal error
				 */
	if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	  throw (new fr.dyade.aaa.joram.JMSAAAException("No back Message received ",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
		
				/* get the the message back or the exception*/
	msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
	if(msgMOM instanceof fr.dyade.aaa.mom.SendingBackMessageMOMExtern) {
	  /* update the fields of the message */
	  fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgSendBack = (fr.dyade.aaa.mom.SendingBackMessageMOMExtern) msgMOM;
	  message = msgSendBack.message;
	} else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern) {
	  /* exception sent back to the client */
	  fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern) msgMOM;
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  except.setLinkedException(msgExc.exception);
	  message = msgExc.message;
	  throw(except);
	} else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern) {
	  /* exception sent back to the client */
	  fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern) msgMOM;
	  javax.jms.InvalidDestinationException except = new javax.jms.InvalidDestinationException("Invalid Topic :  ",String.valueOf(JMSAAAException.MOM_INTERNAL_ERROR));
	  except.setLinkedException(msgExc.exception);
	  throw(except);
	} else {
	  /* unknown message */
	  /* should never arrived */
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  throw(except);
	}
      } else {
	/* sends the messageJMSMOM r */
	refSession.sendToConnection(msgSend);
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (ClassCastException exc) {
      /* TO CHECK : I'm not sure */
      javax.jms.JMSException except = new javax.jms.MessageFormatException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
}
