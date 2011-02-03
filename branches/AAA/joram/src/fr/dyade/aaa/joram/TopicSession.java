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
 *	Before closing a session or a TopicSubscriber, messages must be acknowledged
 *	or else the are rest in the Queue and put as redelivered
 *	 
 *	a TopicSession is as JMS specifications 
 * 
 *	@see fr.dyade.aaa.joram.Session
 *	@see javax.jms.Session 
 *	@see javax.jms.TopicSession 
 */ 
 
public class TopicSession extends fr.dyade.aaa.joram.Session implements javax.jms.TopicSession { 
	
  /** number of not durable TopicSubscriber
   *	this attribute is used to close the TopicSession
   *	and consenquently to discard all the subscriptions (durable) in the MOM
   */
  private int numberNotDurableTopicSubscriber;
  protected Object synchroNbTemporarySubObject;

	
    public TopicSession(boolean transacted, int acknowledgeMode, long sessionIDNew, fr.dyade.aaa.joram.Connection refConnectionNew) {
    super(transacted, acknowledgeMode, sessionIDNew, refConnectionNew);
		
    /* in the creation no subscription is taken*/
    numberNotDurableTopicSubscriber = 0;
    synchroNbTemporarySubObject = new Object();
		
    /* creation of the Thread to deliver the messages from the Topics */
    threadDeliver = new java.lang.Thread(this);
    threadDeliver.setDaemon(true);
    threadDeliver.start();
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.Topic createTopic(java.lang.String topicName) throws javax.jms.JMSException {
    throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.TopicSubscriber createSubscriber(javax.jms.Topic topic) throws javax.jms.JMSException {
    return this.createSubscriber(topic, "", false);
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.TopicSubscriber createSubscriber(javax.jms.Topic topic, java.lang.String messageSelector, boolean noLocal) throws javax.jms.JMSException {
    /* subscribe to the Topic in the MOM */
      String nameSub = new Long(sessionID).toString() + "_" + new Long(counterConsumerID).toString();
    /* subscribe to the agentClient */
      javax.jms.TopicSubscriber topicSubscriber = createMySubscriber(new Long(counterConsumerID).toString(), nameSub, topic, messageSelector, noLocal, false);
    numberNotDurableTopicSubscriber++;
				
    /* calculate the new TopicSubscriber and increments the counter */
    counterConsumerID = Connection.calculateMessageID(counterConsumerID);

    return topicSubscriber;
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.TopicSubscriber createDurableSubscriber(javax.jms.Topic topic, java.lang.String name) throws javax.jms.JMSException {
    return this.createDurableSubscriber(topic, name, "", false);
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.TopicSubscriber createDurableSubscriber(javax.jms.Topic topic, java.lang.String name, java.lang.String messageSelector, boolean noLocal) throws javax.jms.JMSException {
    /* subscribe to the agentClient */
      javax.jms.TopicSubscriber topicSubscriber = createMySubscriber(new Long(counterConsumerID).toString(), name, topic, messageSelector, noLocal, true);
		
    /* calculate the new TopicSubscriber and increments the counter */
    counterConsumerID = Connection.calculateMessageID(counterConsumerID);

    return topicSubscriber;
  }
	
  /**	The InvalidException is at time never thrown so it could arrive that
   *	the name of the Topic is invalid.
   *	But the exception is thrown during the publish method
   *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
   */
  public javax.jms.TopicPublisher createPublisher(javax.jms.Topic topic) throws javax.jms.JMSException {
      fr.dyade.aaa.joram.TopicPublisher topicPublisher = new fr.dyade.aaa.joram.TopicPublisher(refConnection, this, topic);
    if(topicPublisher==null)
      throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation TopicPublisher",JMSAAAException.ERROR_CREATION_MESSAGECONSUMER));
    else 
      return topicPublisher;
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.TemporaryTopic createTemporaryTopic() throws javax.jms.JMSException {
    try {
      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      fr.dyade.aaa.mom.CreationTemporaryTopicMOMExtern msgCreation = new fr.dyade.aaa.mom.CreationTemporaryTopicMOMExtern(messageJMSMOMID);
      /*	synchronization because it could arrive that the notify was
       *	called before the wait 
       */
      synchronized(obj) {
	/* the processus of the client waits the response */
	refConnection.waitThreadTable.put(longMsgID,obj);
	/* get the messageJMSMOM identifier */
	this.sendToConnection(msgCreation);
				
	obj.wait();
      }
			
      /* the clients wakes up */
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
      /* tests if the key exists 
       * dissociates the message null (on receiveNoWait) and internal error
       */
      if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	
      /* get the the message back or the exception*/
      msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
      if(msgMOM instanceof fr.dyade.aaa.mom.CreationBackDestinationMOMExtern) {
	  /* return the temporaryTopic Object with the name given by the agentClient */
	  fr.dyade.aaa.mom.TopicNaming topic = (fr.dyade.aaa.mom.TopicNaming) ((fr.dyade.aaa.mom.CreationBackDestinationMOMExtern) msgMOM).destination;
	  fr.dyade.aaa.joram.TemporaryTopic tempTopic = new fr.dyade.aaa.joram.TemporaryTopic(refConnection, this, topic.getTopicName(), topic.getTheme());
	  return tempTopic;
      } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
	  /* exception sent back to the client */
	  fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  except.setLinkedException(msgExc.exception);
	  throw(except);
      } else {
	  /* unknown message */
	  /* should never arrived */
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  throw(except);
      }
    } catch (InterruptedException exc) {
	javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
	except.setLinkedException(exc);
	throw(except);
    }
  }
	
  /** the name must respect this syntax :
   *	topicId_theme_ownName
   *	where ownName is the real name given by the client
   *	No other choice because the client can unsubscribe  without 
   *	creating a TopicSubscriber 	
   *
   *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications 
   */
  public void unsubscribe(java.lang.String name)  throws javax.jms.JMSException {
    try {
      /* get the parts of the name */
      java.util.StringTokenizer st = new java.util.StringTokenizer(name,"_",false);
      String topic = st.nextToken();
      String theme = st.nextToken();
      String nameSubClient = st.nextToken();
			
      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      /* marshalling of the message */
      fr.dyade.aaa.mom.UnsubscriptionMessageMOMExtern msgUnsub = new fr.dyade.aaa.mom.UnsubscriptionMessageMOMExtern(messageJMSMOMID, nameSubClient,new fr.dyade.aaa.mom.TopicNaming(topic, theme), new Long(sessionID).toString(), super.acknowledgeMode);
		
      /*	synchronization because it could arrive that the notify was
       *	called before the wait 
       */
      synchronized(obj) {
	/* the processus of the client waits the response */
	refConnection.waitThreadTable.put(longMsgID,obj);
	/* get the messageJMSMOM identifier */
	this.sendToConnection(msgUnsub);
	obj.wait();
      }
		
      /* the clients wakes up */
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
      /* tests if the key exists 
       * dissociates the message null (on receiveNoWait) and internal error
       */
      if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	
      /* get the the message back or the exception*/
      msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
      if(msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
				/* destruction the entry in the Connection table if any */
	fr.dyade.aaa.joram.KeyConnectionSubscription key = new fr.dyade.aaa.joram.KeyConnectionSubscription(nameSubClient, topic, theme);
	refConnection.subscriptionListenerTable.remove(key);
      } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
				/* exception sent back to the client */
	fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
	fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	except.setLinkedException(msgExc.exception);
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
    } catch (InterruptedException exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
      except.setLinkedException(exc);
      throw(except);
    }  catch (java.util.NoSuchElementException exc) {
      fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("the syntax to the unsubscription is \"topicID_theme_nameSubscription\" : ",JMSAAAException.INCORRECT_SYNTAX_UNSUBSCRIBE);
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** creates a  Durable or nonDurable TopicSubscriber */
  public javax.jms.TopicSubscriber
      createMySubscriber(String subscriberID,
			 String nameSubscription,
			 javax.jms.Topic topic,
			 String messageSelector,
			 boolean noLocal,
			 boolean subDurable) throws javax.jms.JMSException {
    try {
      fr.dyade.aaa.joram.TopicSubscriber topicSubscriber = new fr.dyade.aaa.joram.TopicSubscriber(subscriberID, refConnection, this, nameSubscription, topic, messageSelector, noLocal, subDurable);
      if(topicSubscriber==null)
	throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation TopicSubscriber",JMSAAAException.ERROR_CREATION_MESSAGECONSUMER));
      else {
	  /* checks if the name of the subscription is already taken */
	/* System.out.println("nameSubscription=\"" + nameSubscription +
			   "\" ,topic.getTopicName()=\"" + topic.getTopicName() +
			   "\" ,topic.getTheme()=\"" + topic.getTheme() +
			   "\"");
	*/
	  
	  fr.dyade.aaa.joram.KeyConnectionSubscription key = new fr.dyade.aaa.joram.KeyConnectionSubscription(nameSubscription,topic.getTopicName(),((fr.dyade.aaa.mom.TopicNaming) topic).getTheme());
	  //System.out.println("key=\"" + key + "\"");
	if(refConnection.subscriptionListenerTable.containsKey(key))
	  throw (new fr.dyade.aaa.joram.JMSAAAException("Name of the subscription already taken",JMSAAAException.NAME_SUBSCRIPTION_ALREADY_TAKEN));
				
	/* inscription in the Table of the Connection */
	refConnection.subscriptionListenerTable.put(key, this);
				
	/* inscription in the Table of the Session */
	synchronized(messageConsumerTable) {	
	  java.util.Vector v;
	  if((v = (java.util.Vector) messageConsumerTable.get(topic))!=null) {
	    /* already a MessageConsumer of this session subscribed the Topic */
	    topicSubscriber.setDeliveryMessage(false);
	    v.addElement(topicSubscriber);
	  } else {
	    /* first MessageConsumer on the Topic */
	    v = new java.util.Vector();
	    v.addElement(topicSubscriber);
	    messageConsumerTable.put(topic, v);
	  }
	}

	Object obj = new Object();
	long messageJMSMOMID = refConnection.getMessageMOMID();
	Long longMsgID = new Long(messageJMSMOMID);
			
	/* marshalling of the message */
	fr.dyade.aaa.mom.SubscriptionMessageMOMExtern msgSubdurable = null;
	fr.dyade.aaa.mom.SubscriptionNoDurableMOMExtern msgSubNoDurable = null;
	if(subDurable)
	  msgSubdurable = new fr.dyade.aaa.mom.SubscriptionMessageMOMExtern(messageJMSMOMID, nameSubscription, (fr.dyade.aaa.mom.TopicNaming) topic, noLocal, messageSelector, new Long(sessionID).toString(), super.acknowledgeMode);
	else
	  msgSubNoDurable = new fr.dyade.aaa.mom.SubscriptionNoDurableMOMExtern(messageJMSMOMID, nameSubscription, (fr.dyade.aaa.mom.TopicNaming) topic, noLocal, messageSelector, new Long(sessionID).toString(), super.acknowledgeMode);
				
	/* Synchronization because it could arrive that the notify was
	 * called before the wait 
	 */

	synchronized(obj) { 
	  /* the processus of the client waits the response */
	  refConnection.waitThreadTable.put(longMsgID,obj);
	  /* get the messageJMSMOM identifier */
	  if(subDurable)
	    this.sendToConnection(msgSubdurable);
	  else
	    this.sendToConnection(msgSubNoDurable);

	  obj.wait();
	}

	/* the clients wakes up */
	fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
	/* tests if the key exists 
	 * dissociates the message null (on receiveNoWait) and internal error
	 */
	if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	  throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	/* get the the message back or the exception*/
	msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);

	if(msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
	  /* return the message */
	  fr.dyade.aaa.mom.RequestAgreeMOMExtern msgDeliver = (fr.dyade.aaa.mom.RequestAgreeMOMExtern) msgMOM;
	  return topicSubscriber;
	} else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
	  /* exception sent back to the client */
	  fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  except.setLinkedException(msgExc.exception);
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
      }
    } catch (InterruptedException exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
      except.setLinkedException(exc);
      throw(except);
    } 
  }
	
  /**overwrite the methode from MessageConsumer  */
  public void close()  throws javax.jms.JMSException {
    try {
      /* stop the Thread of the Session */
      if(threadDeliver!=null)
	threadDeliver.stop();
			
      /* remove all elements of the messageConsumer table */	
      this.discardEntryTopicSubscriber();
      messageConsumerTable.clear();
			
      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      fr.dyade.aaa.mom.CloseTopicSessionMOMExtern msgMSPClose = new fr.dyade.aaa.mom.CloseTopicSessionMOMExtern(messageJMSMOMID, new Long(sessionID).toString());
      /*	synchronization because it could arrive that the notify was
       *	called before the wait 
       */
      synchronized(obj) {
	/* the processus of the client waits the response */
	if(numberNotDurableTopicSubscriber>0) {
	  /*	mutliple unsubscrptions to wait (for temporary subscriptions) 
	   *	and 1 from the agentClient 
	   */
	  refConnection.waitThreadTable.put(longMsgID,new ResponseAckObject(obj, this.getNbTemporarySub()+1));
	} else {
	  /*	no unbscription to wait, only stop the delivery of the message
	   *	durable subscriptions
	   */
	  refConnection.waitThreadTable.put(longMsgID,obj);
	}
	/* get the messageJMSMOM identifier */
	this.sendToConnection(msgMSPClose);

	obj.wait();
      }
			
      /* the clients wakes up */
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
		
      /* tests if the key exists 
       * dissociates the message null (on receiveNoWait) and internal error
       */
      if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
	
      /* get the the message back or the exception*/
      msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
      if(!(msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern)) {
				/* exception sent back to the client */
	fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM;
	fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	except.setLinkedException(msgExc.exception);
	throw(except);
      }
      numberNotDurableTopicSubscriber=0;
      super.close();
    } catch (InterruptedException exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
      except.setLinkedException(exc);
      throw(except);
    }	
  }
	
  /** discards all the entries of the Connection table (subscriptionListenerTable) 
   *	with any reference to a TopicSubscriber of the session
   */
  private void discardEntryTopicSubscriber() {
    synchronized(messageConsumerTable) {
      Enumeration e = messageConsumerTable.elements();
      Vector v;
      fr.dyade.aaa.joram.TopicSubscriber subscriber;
      /* vector of topicSubscriber for a topic */
      while(e.hasMoreElements()) {
	v = (Vector) e.nextElement();
				/* discard all the topicSubscribers */
	while(v.isEmpty()) {
	  subscriber = (fr.dyade.aaa.joram.TopicSubscriber) v.firstElement();
	  fr.dyade.aaa.joram.KeyConnectionSubscription key = new fr.dyade.aaa.joram.KeyConnectionSubscription(subscriber.nameSubscription, subscriber.topic.getTopicName(), subscriber.topic.getTheme());
	  refConnection.subscriptionListenerTable.remove(key);
	  v.removeElementAt(0);
	}
      }
    }
  }
	
  /** used for asynchronous receptions */
  public void run() {
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgMOM;
    while(true) {
      try {
	msgMOM = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) msgNoDeliveredQueue.get();

	if (recoverySet || XARollback) {
	  try {
	    Thread.currentThread().sleep(1);
	  } catch (InterruptedException exc) {}
	  continue;
	}

	fr.dyade.aaa.mom.TopicNaming topic = new fr.dyade.aaa.mom.TopicNaming(((fr.dyade.aaa.mom.TopicNaming) msgMOM.message.getJMSDestination()).getTopicName(), msgMOM.theme);
	fr.dyade.aaa.mom.AckTopicMessageMOMExtern msgAck;

	/* searchs the correspondant MessageConsumer */
	synchronized(messageConsumerTable) {
	  java.util.Vector v = (java.util.Vector) messageConsumerTable.get(topic);
	  fr.dyade.aaa.joram.MessageConsumer consumer = (fr.dyade.aaa.joram.MessageConsumer) v.firstElement();
					
	  /* delivers the message to the listener */
	  if(consumer.getDeliveryMessage()) {
	      if(consumer.messageListener!=null) {
		  /* add the reference to the session to acknowledge handly */
		  if(acknowledgeMode!=fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE)
		      msgMOM.message.setRefSessionItf(this);
		  
		  /*	reset the message to put the mode in readOnly and to
		   *	prepare the transient attributes dor the reading
		   */
		  resetMessage(msgMOM.message);

		  consumer.messageListener.onMessage(msgMOM.message);
		  
		  if(this.transacted) {
		      /* add the transacted messages to the message to ack */
		      synchronized (XASynchroObject) {
			  XAMessageToAckVector.addElement(msgMOM);
		      }
		  } else { 
		      if(acknowledgeMode!=fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE) {
			  lastNotAckVector.addElement(msgMOM);
		      }
		      else {
			  /* sends an acknowledge */
			  msgAck = new fr.dyade.aaa.mom.AckTopicMessageMOMExtern(refConnection.getMessageMOMID(), topic, msgMOM.nameSubscription, msgMOM.message.getJMSMessageID(), fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE);
			  this.sendToConnection(msgAck);
		      }
		  }
		  msgNoDeliveredQueue.pop();
	      }
	  } else {
	      /*	sends an acknowledge but delivers no message because only 1 MessageConsumer
	       *	is to able to receive a same message 
	       */
	      msgAck = new fr.dyade.aaa.mom.AckTopicMessageMOMExtern(refConnection.getMessageMOMID(), topic, msgMOM.nameSubscription, msgMOM.message.getJMSMessageID(), fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE);
	      this.sendToConnection(msgAck);
	      msgNoDeliveredQueue.pop();
	  }
	}
	
      } catch (javax.jms.JMSException exc) {}	
    }
  }
	
  /** decrements the number of no durable subscription in case of closing
   *	of a TopicSubscriber (temporary)
   */
  protected void decrementNbTemporarySub() {
    synchronized(synchroNbTemporarySubObject) {
      numberNotDurableTopicSubscriber--;
    }
  }
	
  /** get the value of the of no durable subscription */
  private int getNbTemporarySub() {
    synchronized(synchroNbTemporarySubObject) {
      return numberNotDurableTopicSubscriber;
    }
  }
	
  /** prepares the messages to acknowledge so as to decrease the overhead 
   */
  protected Vector preparesHandlyAck(String messageID, long messageJMSMOMID) throws javax.jms.JMSException{
    int i = 0;
    int indexMessage = -1;
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern currentMsg ;
    fr.dyade.aaa.mom.TopicNaming currentTopic;
		
    Vector resultVector = new Vector();
		
    /* first pass to find the index of the message in the vector */
    while(i<lastNotAckVector.size()) {
      currentMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) lastNotAckVector.elementAt(i);
			
      if(messageID.equals(currentMsg.message.getJMSMessageID())) {
	indexMessage = i;
	break;
      }
      i++;
    }
		
    /*	prepares the vector of the message 2nd pass 
     *	begins the indexMessage-1 and continues until the index 0
     */
    javax.jms.Message message;
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern previousMsg ;
    fr.dyade.aaa.mom.TopicNaming previousTopic;
    i = indexMessage;
    while(i>=0) {
      /* add the elment in the ack Vector & removes from the other */
      currentMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) lastNotAckVector.elementAt(i);
      currentTopic = (fr.dyade.aaa.mom.TopicNaming) currentMsg.message.getJMSDestination();
      message = currentMsg.message;
      resultVector.addElement(new fr.dyade.aaa.mom.AckTopicMessageMOMExtern(messageJMSMOMID, currentTopic, currentMsg.nameSubscription, message.getJMSMessageID(), acknowledgeMode));
						
      int j = i-1;
      while(j>=0) {
	previousMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) lastNotAckVector.elementAt(j);
	previousTopic = (fr.dyade.aaa.mom.TopicNaming) previousMsg.message.getJMSDestination();
			
	if(currentTopic.equals(previousTopic) && (currentMsg.nameSubscription).equals(previousMsg.nameSubscription)) {
	  lastNotAckVector.removeElementAt(j);
	  j--;
	  i--;
	} else
	  j--;
      }
			
      /* removes the message from the vector */
      lastNotAckVector.removeElementAt(i);
      i--;
    }
    return resultVector;
  }
	
  /** prepares the messages to acknowledge so as to decrease the overhead  */
  protected Vector preparesTransactedAck(long messageJMSMOMID) throws javax.jms.JMSException{
    int i = XAMessageToAckVector.size()-1;
    Vector resultVector = new Vector();
		
    /*	prepares the vector of the message 2nd pass 
     *	begins the indexMessage-1 and continues until the index 0
     */
    javax.jms.Message message;
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern currentMsg ;
    fr.dyade.aaa.mom.TopicNaming currentTopic;
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern previousMsg ;
    fr.dyade.aaa.mom.TopicNaming previousTopic;
    while(i>=0) {
      /* add the elment in the ack Vector & removes from the other */
      currentMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) XAMessageToAckVector.elementAt(i);
      currentTopic = (fr.dyade.aaa.mom.TopicNaming) currentMsg.message.getJMSDestination();
      message = currentMsg.message;
      resultVector.addElement(new fr.dyade.aaa.mom.AckTopicMessageMOMExtern(messageJMSMOMID, currentTopic, currentMsg.nameSubscription, message.getJMSMessageID(), acknowledgeMode));
						
      int j = i-1;
      while(j>=0) {
	previousMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) XAMessageToAckVector.elementAt(j);
	previousTopic = (fr.dyade.aaa.mom.TopicNaming) previousMsg.message.getJMSDestination();
			
	if(currentTopic.equals(previousTopic) && (currentMsg.nameSubscription).equals(previousMsg.nameSubscription)) {
	  XAMessageToAckVector.removeElementAt(j);
	  j--;
	  i--;
	} else
	  j--;
      }
			
      /* removes the message from the vector */
      XAMessageToAckVector.removeElementAt(i);
      i--;
    }
    return resultVector;
  }
	
  /** prepares the messages to recover the messages of the session */
  protected fr.dyade.aaa.mom.RecoverObject[] preparesRecover() throws javax.jms.JMSException {
    int i = lastNotAckVector.size()-1;
    Vector resultVector = new Vector();
					
    /*	prepares the vector of the message 2nd pass 
     *	begins the indexMessage-1 and continues until the index 0
     */
    javax.jms.Message message;
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern currentMsg ;
    fr.dyade.aaa.mom.TopicNaming currentTopic;
    fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern previousMsg ;
    fr.dyade.aaa.mom.TopicNaming previousTopic;
    while(i>=0) {
      /* add the elment in the ack Vector & removes from the other */
      currentMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) lastNotAckVector.elementAt(i);
      currentTopic = (fr.dyade.aaa.mom.TopicNaming) currentMsg.message.getJMSDestination();
      message = currentMsg.message;
      resultVector.addElement(new fr.dyade.aaa.mom.RecoverTopic(currentTopic, currentMsg.nameSubscription, message.getJMSMessageID()));
						
      int j = i-1;
      while(j>=0) {
	previousMsg = (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) lastNotAckVector.elementAt(j);
	previousTopic = (fr.dyade.aaa.mom.TopicNaming) previousMsg.message.getJMSDestination();
			
	if(currentTopic.equals(previousTopic) && (currentMsg.nameSubscription).equals(previousMsg.nameSubscription)) {
	  lastNotAckVector.removeElementAt(j);
	  j--;
	  i--;
	} else
	  j--;
      }
			
      /* removes the message from the vector */
      lastNotAckVector.removeElementAt(i);
      i--;
    }
    /* build of the XARollbakMOMExtern */
    int size = resultVector.size();
    fr.dyade.aaa.mom.RecoverTopic[] ackTab = new fr.dyade.aaa.mom.RecoverTopic[size];
    resultVector.copyInto(ackTab);
    return ackTab;
  }
	
  /** allows the session to acknowledge messages for the commit */
  protected void rollbackXAMessage() throws javax.jms.JMSException {
    try {
      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      /* sends a rollback message only if a message was delivered */
      if(!XAMessageToAckVector.isEmpty()) {
	fr.dyade.aaa.mom.XARollbackTopicMOMExtern msgRoll = new fr.dyade.aaa.mom.XARollbackTopicMOMExtern(messageJMSMOMID, new Long(this.sessionID).toString());
				/*	synchronization because it could arrive that the notify was
				 *	called before the wait 
				 */
	synchronized(obj) {
	  refConnection.waitThreadTable.put(longMsgID,obj);
	  /* get the messageJMSMOM identifier */
	  this.sendToConnection(msgRoll);
						
	  obj.wait();
	}
				
				/* the clients wakes up */
	fr.dyade.aaa.mom.MessageMOMExtern msgResponse;
			
				/* tests if the key exists 
				 * dissociates the message null (on receiveNoWait) and internal error
				 */
	if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	  throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
				
				/* get the the message back or the exception*/
	msgResponse = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
	if(msgResponse instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
	  /* OK */ 
	} else if(msgResponse instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
	  /* exception sent back to the client */
	  fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgResponse;
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  except.setLinkedException(msgExc.exception); 
	  throw(except);
	} else {
	  /* unknown message */
	  /* should never arrived */
	  fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	  throw(except);
	}
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (InterruptedException exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
      except.setLinkedException(exc);
      throw(except);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

   /** allows the session to rollback a delivery message */
  protected void rollbackDeliveryMsg() throws javax.jms.JMSException {
    try {
      Vector RollbackDeliveryMsg = new Vector();
      while (!XAMessageToAckVector.isEmpty()) {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    }
  }
}