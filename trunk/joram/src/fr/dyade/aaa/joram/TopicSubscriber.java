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
 *	Before closing a session or a TopicSubscriber, messages must be acknowledged
 *	or else the are rest in the Queue and put as redelivered
 *
 *	a TopicPublisher is an object associated with a real Topic
 *	in the MOM 
 * 
 *	@see javax.jms.MessageConsumer
 *	@see javax.jms.TopicSubscriber
 *	@see fr.dyade.aaa.mom.Topic 
 */ 
 
public class TopicSubscriber extends fr.dyade.aaa.joram.MessageConsumer implements javax.jms.TopicSubscriber { 

	/** the Topic associated to the TopicSubscriber */
	fr.dyade.aaa.mom.TopicNaming topic;
	
	/** the noLocal attribute of the TopicPublisher */
	private boolean noLocal;
	
	/** the name of the subscription */
	protected String nameSubscription;
	
	/** gets if the TopicSubscriber concerns a durable subscription or not */
	protected boolean subDurable;

	/** constructor with nolocal default value = false and no selector */
    public TopicSubscriber(String consumerID, fr.dyade.aaa.joram.Connection refConnection, fr.dyade.aaa.joram.Session refSession, String nameSubscription, javax.jms.Topic topic, String selector, boolean noLocal, boolean subDurable) {
		super(consumerID, refConnection, refSession, selector);
		this.topic = (fr.dyade.aaa.mom.TopicNaming) topic;
		this.noLocal = noLocal;
		this.nameSubscription = nameSubscription;
		this.subDurable = subDurable;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public javax.jms.Topic getTopic() throws javax.jms.JMSException {
		if(topic==null)
			throw (new fr.dyade.aaa.joram.JMSAAAException("Topic name Unknown",JMSAAAException.DEFAULT_JMSAAA_ERROR));
		else
			return topic;
	}
	
	/** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
	public boolean getNoLocal() throws javax.jms.JMSException {
		return noLocal;
	}
	
	/**overwrite the methode from MessageConsumer  */
	public void close()  throws javax.jms.JMSException {
		try {
			/* decrements the counter of no durable ubscription if yes */
			if(!subDurable)
				((fr.dyade.aaa.joram.TopicSession)refSession).decrementNbTemporarySub();
			
			synchronized(refSession.messageConsumerTable) {	
				/* cancel the entry to the Connection table*/
				fr.dyade.aaa.joram.KeyConnectionSubscription key = new fr.dyade.aaa.joram.KeyConnectionSubscription(nameSubscription, topic.getTopicName(), topic.getTheme());
				refConnection.subscriptionListenerTable.remove(key);
				
				/* cancel the inscription in the Table of the Session */
				java.util.Vector v;
				if((v = (java.util.Vector) refSession.messageConsumerTable.get(topic))==null)
					throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during close",JMSAAAException.DEFAULT_JMSAAA_ERROR));
				if(v.size()==1) {
					/* remove the entry of the hahtable */
					refSession.messageConsumerTable.remove(topic);
				} else {
					/* others MessageConsumer waiting the messages */
					/* discard this elment of the vector */
					v.removeElementAt(0);
				
					/* set the reception OK in the firstElement of the vector */
					fr.dyade.aaa.joram.TopicSubscriber topicSubscriber = (fr.dyade.aaa.joram.TopicSubscriber) v.firstElement();
					topicSubscriber.setDeliveryMessage(true);
				}
			}
			
			Object obj = new Object();
			long messageJMSMOMID = refConnection.getMessageMOMID();
			Long longMsgID = new Long(messageJMSMOMID);
				
			fr.dyade.aaa.mom.CloseSubscriberMOMExtern msgClose = new fr.dyade.aaa.mom.CloseSubscriberMOMExtern(messageJMSMOMID, nameSubscription, topic, new Long(refSession.sessionID).toString(), subDurable);
				
			/*	synchronization because it could arrive that the notify was
			 *	called before the wait 
			 */
			synchronized(obj) {
			  /* the processus of the client waits the response */
			  refConnection.waitThreadTable.put(longMsgID,obj);
			  /* get the messageJMSMOM identifier */
			  refSession.sendToConnection(msgClose);

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
		} catch (InterruptedException exc) {
			javax.jms.JMSException except = new javax.jms.JMSException("Internal Error : ",String.valueOf(JMSAAAException.DEFAULT_JMSAAA_ERROR));
			except.setLinkedException(exc);
			throw(except);
		}
		super.close();
	}
	
	/** overwrite the methode  */
	public void setMessageListener(javax.jms.MessageListener listener) throws javax.jms.JMSException {
     if (listener != null && messageListener != null)
      throw (new javax.jms.JMSException("A listener has already been set"));

		try {	
			Object obj = new Object();
			long messageJMSMOMID = refConnection.getMessageMOMID();
			Long longMsgID = new Long(messageJMSMOMID);
			
			fr.dyade.aaa.mom.SettingListenerMOMExtern msgSet = new fr.dyade.aaa.mom.SettingListenerMOMExtern(messageJMSMOMID, nameSubscription, topic, true);
				
			/*	synchronization because it could arrive that the notify was
			 *	called before the wait 
			 */
			synchronized(obj) {
			  /* the processus of the client waits the response */
			  refConnection.waitThreadTable.put(longMsgID,obj);
			  /* get the messageJMSMOM identifier */
			  refSession.sendToConnection(msgSet);
				
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
				/* keep the reference to the MesageListener */
				messageListener = listener;
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


  /**
   * Method used for synchronous reception. 
   *
   * @param timeOut  Time-to-live attribute of the reception request in MOM
   * (ms).
   *
   * @author Frederic Maistre
   */
  public javax.jms.Message receive(long timeOut) throws javax.jms.JMSException
  {
    long requestID = refConnection.getMessageMOMID();
    Long longRequestID = new Long(requestID);

    // Building the request.
    fr.dyade.aaa.mom.SynchronousReceptionRequestMsg requestMsg = 
      new fr.dyade.aaa.mom.SynchronousReceptionRequestMsg(requestID, timeOut);
    requestMsg.setSubName(nameSubscription);

    Object lock = new Object();

    // Sending the request and waiting for an answer.
    try {
      synchronized (lock) {
        refConnection.waitThreadTable.put(longRequestID, lock);
        refSession.sendToConnection(requestMsg);
        lock.wait();
      }
    } catch (InterruptedException iE) {
      javax.jms.JMSException jE =
        new javax.jms.JMSException("Error while waiting for MOM's answer.");
      jE.setLinkedException(iE);
      throw(jE);
    }
    
    if (!refConnection.messageJMSMOMTable.containsKey(longRequestID))
      throw new javax.jms.JMSException("MOM's answer does not match the request!");

    fr.dyade.aaa.mom.MessageMOMExtern momMsg = (fr.dyade.aaa.mom.MessageMOMExtern)
      refConnection.messageJMSMOMTable.remove(longRequestID);

    // Acknowledging the answer.
    if (momMsg instanceof fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) {
      fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msg =
        (fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) momMsg;
      if (msg.message != null) {
        if (!refSession.transacted) {
          if (refSession.acknowledgeMode ==
            fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE) {
            
            fr.dyade.aaa.mom.AckTopicMessageMOMExtern ackMsg =
              new fr.dyade.aaa.mom.AckTopicMessageMOMExtern(refConnection.getMessageMOMID(),
              topic, nameSubscription, msg.message.getJMSMessageID(),
              fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE);
            
            refSession.sendToConnection(ackMsg);
          }
          else {
            refSession.lastNotAckVector.addElement(msg);
            msg.message.setRefSessionItf(refSession);
          }
        }
        else {
          synchronized(refSession.transactedSynchroObject) {
            refSession.transactedMessageToAckVector.addElement(msg);
          }
        }
      }
      refSession.resetMessage(msg.message);
      return msg.message;
    }

    // Processing the exception cases.
    else if (momMsg instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
      fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc =
        (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) momMsg;

      javax.jms.JMSException except = new javax.jms.JMSException("MOM internal error"); 
      except.setLinkedException(msgExc.exception);
      throw(except);
    }
    else {
      javax.jms.JMSException except = new javax.jms.JMSException("MOM internal error"); 
      throw(except);
    }
  }

}
