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
 *	a Session is as JMS specifications 
 *
 *	In this implementation any attributes are only used by TopicSession and 
 *	another by TopicSession. However, I put this attribute in the Session
 *	Object because later, il will have to be possible to have synchronous
 *	and asynchonous reception on Queue and Topic. 
 * 
 *	@see         subclasses
 *	javax.jms.Session 
 */ 
 
public abstract class Session implements javax.jms.Session, fr.dyade.aaa.mom.SessionItf { 
 
  /** reference to the COnnection Object so send Message to the socket */
  protected fr.dyade.aaa.joram.Connection refConnection;
	
  /** the mode of transaction 
   * for present time only not transated mode is implemented
   */
  protected boolean transacted;

  /** the mode of acknowledge */
  protected int acknowledgeMode ;
	
  /** identifier of session : sessionID */
    protected long sessionID;
	
  /** counter of consumerId for identify the not durable subscription */
    protected long counterConsumerID;
	
  /** The efficient thread */
  protected java.lang.Thread threadDeliver;
	
  /** object to do the synchonization between a threadDeliver and the manual
   *	acknowledgment of the client
   */
  protected Object synchroLastNotAckVector;

  /** the vector of the last delivered messages for implicit reception */
  protected Vector lastNotAckVector;
	
  /** vector of no delivered messages for implicit reception */
  protected fr.dyade.aaa.joram.FifoQueue msgNoDeliveredQueue;
	
  /**	hashtable which contains all the MessageConsumer creates by the Session
   *	For a TOPICSESION
   *	the key is TopicNaming because, only ONE delivery is allowed for each 
   *	Message in the same Session (we don't care about the name of the subscription 
   *	The vector should be contained most of time only one element
   *	key : fr.dyade.aaa.mom.TopicNaming
   *	Object : vector of fr.dyade.aaa.joram.MessageConsumer 
   *	FOR a QUEUESESSION
   *	key : fr.dyade.aaa.mom.QueueNaming
   *	Object : vector of fr.dyade.aaa.joram.MessageConsumer 
   */
  protected Hashtable messageConsumerTable;
	
  /** vector to acknowledge message in auto_acknowledge mode
   *	used for explicit reception
   *	before to give the message to the client, ackMessage is prepared and put
   *	in this vector that the current Thread reads and sends
   */
  protected Vector autoMessageToAckVector;
	
  /** autoAck only : the vector containing the request of the session */
  protected Vector explicitRequestVector = null;
	
  /** indicates that a recover methods is working */
  protected boolean recoverySet = false ;
	
  /** vector of message to send when the clients will dothe commit */
  protected Vector XAMessageToSendVector = null;
	
  /** vector of message delivered to the client but waiting for the commit */
  protected Vector XAMessageToAckVector = null;
	
  /**	synchro to avoid the client commits before the message
   *	be added to the vector of delivered messages
   */
  protected Object XASynchroObject;
	
  /** allows to stop the delivery the delivery of the message during
   *	the rollback
   */
  protected boolean XARollback = false;
									
  /** Constructor */
    public Session(boolean transacted, int acknowlegdeMode, long sessionID, fr.dyade.aaa.joram.Connection refConnection) {
    this.refConnection = refConnection;
    this.transacted = transacted;
    this.sessionID = sessionID;
		
    /* initialisation of the consumer counter */
    counterConsumerID = 1;

    messageConsumerTable = new Hashtable();
		
    /* initialisation of object using for delivery */
    msgNoDeliveredQueue = new fr.dyade.aaa.joram.FifoQueue();
		
    /* this object must be always initialized because it avoids a test 
     * if the session is transacted or not in the RUN of the TopicSession
     */
    XASynchroObject = new Object();
		
    if(!this.transacted) {
      this.acknowledgeMode = acknowlegdeMode;
		
      /* initialisation of object using for delivery and acknowledgement*/
      lastNotAckVector = new Vector();
			
      /* initialisation of the synchronized object */
      synchroLastNotAckVector = new Object();
			
      /*initialisation of the autoMessageToAckVector */
      autoMessageToAckVector = new Vector();
		
      /* the request for the explicit requests (Queue session at time) */
      explicitRequestVector = new Vector();
		
    } else {
      /* XA : the message to send */
      XAMessageToSendVector = new Vector();
      XAMessageToAckVector = new Vector();		
      this.acknowledgeMode = fr.dyade.aaa.mom.CommonClientAAA.TRANSACTED;
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.BytesMessage createBytesMessage() throws javax.jms.JMSException {
    try {
      return (new fr.dyade.aaa.mom.BytesMessage());
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.MapMessage createMapMessage() throws javax.jms.JMSException {
    try {
      return (new fr.dyade.aaa.mom.MapMessage());
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.Message createMessage() throws javax.jms.JMSException {
    try {
      return (new fr.dyade.aaa.mom.Message());
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.ObjectMessage createObjectMessage() throws javax.jms.JMSException {
    try {
      return (new fr.dyade.aaa.mom.ObjectMessage());
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.ObjectMessage createObjectMessage(java.io.Serializable object) throws javax.jms.JMSException {
    try {
      fr.dyade.aaa.mom.ObjectMessage objMsg = new fr.dyade.aaa.mom.ObjectMessage();
      objMsg.setObject(object);
      return objMsg;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.StreamMessage createStreamMessage() throws javax.jms.JMSException {
    try {
      return (new fr.dyade.aaa.mom.StreamMessage());
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public  javax.jms.TextMessage createTextMessage() throws javax.jms.JMSException {
    try {
      return (new fr.dyade.aaa.mom.TextMessage());
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications*/
  public  javax.jms.TextMessage createTextMessage(java.lang.String string) throws javax.jms.JMSException {
    try {
      fr.dyade.aaa.mom.TextMessage txtMsg = new fr.dyade.aaa.mom.TextMessage();
      txtMsg.setText(string.toString());
      return txtMsg;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications*/
  public  javax.jms.TextMessage createTextMessage(java.lang.StringBuffer stringBuffer) throws javax.jms.JMSException {
    try {
      fr.dyade.aaa.mom.TextMessage txtMsg = new fr.dyade.aaa.mom.TextMessage();
      txtMsg.setText(stringBuffer.toString());
      return txtMsg;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public boolean getTransacted() throws javax.jms.JMSException {
    try {
      return this.transacted;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void commit() throws javax.jms.JMSException {
    try {
      if(this.transacted) {
	/* sends the acknowledgments */
	synchronized (XASynchroObject) {
	  this.acknowledgeXAMessage();
	}
	/* send vector of message, vector contain all messages to send */
	if(!XAMessageToSendVector.isEmpty()) {
	  long messageJMSMOMID = refConnection.getMessageMOMID();
	  fr.dyade.aaa.mom.MessageMOMExtern msgSend = new fr.dyade.aaa.mom.MessageTransactedVector(messageJMSMOMID,XAMessageToSendVector);
	  sendMessage(msgSend);
	  XAMessageToSendVector.removeAllElements();
	}
      } else 
	throw(new fr.dyade.aaa.joram.JMSAAAException("The session is not transacted : ",JMSAAAException.SESSION_NOT_TRANSACTED));
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except); 
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void rollback() throws javax.jms.JMSException {
    try {
      if(this.transacted) {

	/* rollback  the no-acknowledged messages */
	synchronized (XASynchroObject) {
	  /* lock the delivery of the message */
	  this.XARollback = true;
	  /* rollback delivery message */
	  rollbackDeliveryMsg();
	}

	/* destroys the message waiting to send */
	XAMessageToSendVector.removeAllElements();
	
	XARollback = false;
      } else 
	throw(new fr.dyade.aaa.joram.JMSAAAException("The session is not transacted : ",JMSAAAException.SESSION_NOT_TRANSACTED));
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except); 
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void close() throws javax.jms.JMSException {
    System.gc();
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void recover() throws javax.jms.JMSException {
    try {
      if(this.transacted)
	throw(new fr.dyade.aaa.joram.JMSAAAException("The session is transacted : ",JMSAAAException.SESSION_TRANSACTED));
			
      this.recoverySet = true;
			
      /* in auto_ack mode, only 1 message can be recovered, stupid to
       * send a message of recovery to the agentClient
       * moreover, the 1 message is immediatly ack => most time no msg
       * to recover
       */
      if(acknowledgeMode!=fr.dyade.aaa.mom.CommonClientAAA.AUTO_ACKNOWLEDGE) {
				
	Object obj = new Object();
	long messageJMSMOMID = refConnection.getMessageMOMID();
	Long longMsgID = new Long(messageJMSMOMID);
			
	/* preparing the ackMSP  */
	fr.dyade.aaa.mom.RecoverObject[] ackTab = this.preparesRecover();
				
	if(ackTab.length>0) {
	  fr.dyade.aaa.mom.RecoverMsgMOMExtern msgRoll = new fr.dyade.aaa.mom.RecoverMsgMOMExtern(messageJMSMOMID, ackTab);
	  /*	synchronization because it could arrive that the notify was
	   *	called before the wait 
	   */
	  synchronized(obj) {
	    /* the processus of the client waits the response 
	       1 response for Topic 
	       size for Queue */
	    //refConnection.waitThreadTable.put(longMsgID,new ResponseAckObject(obj, size));
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
	    /* delivery nox possible */
	    this.recoverySet = false;
	  } else if(msgResponse instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
	    /* exception sent back to the client */
	    fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgResponse;
	    fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	    except.setLinkedException(msgExc.exception); 
	    this.recoverySet = false;
	    throw(except);
	  } else {
	    /* unknown message */
	    /* should never arrived */
	    fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	    throw(except);
	  }
	}	
      } 
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except); 
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications 
   *	for serverSessionPool
   */
  public javax.jms.MessageListener getMessageListener() throws javax.jms.JMSException {
    throw (new fr.dyade.aaa.joram.JMSAAAException("available in TopicSubscriber",JMSAAAException.NOT_YET_AVAILABLE));
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications  
   *	for serverSessionPool
   */
  public void setMessageListener(javax.jms.MessageListener listener) throws javax.jms.JMSException {
    throw (new fr.dyade.aaa.joram.JMSAAAException("available in TopicSubscriber",JMSAAAException.NOT_YET_AVAILABLE));
  }
	
  /** a session defines a serial order do the messages it consumes and the
   *	messages it produces
   *	@see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications
   *	return the messageJMSMOMID to retrieve the messageJMSMOM
   */
  public synchronized void sendToConnection(fr.dyade.aaa.mom.MessageMOMExtern msgMOM)  throws javax.jms.JMSException{
    refConnection.sendMsgToAgentClient(msgMOM);
  }

  /** allows a client to acknowledge handly the message */
  public void acknowledgeMessage(String messageID) throws javax.jms.JMSException {
    try {
      if(this.transacted) 
	throw(new fr.dyade.aaa.joram.JMSAAAException("The session is transacted : Handly acknowledge not Possible",JMSAAAException.SESSION_TRANSACTED));
		
      if(messageID==null)
	throw (new fr.dyade.aaa.joram.JMSAAAException("MessageId to acknowledged is null",JMSAAAException.MESSAGEID_NULL));
			
      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      /*	preparing the ackMSP */
      Vector ackVector = preparesHandlyAck(messageID, messageJMSMOMID);
      if(!ackVector.isEmpty()) {
	int size = ackVector.size();
	fr.dyade.aaa.mom.MessageMOMExtern[] ackTab = new fr.dyade.aaa.mom.MessageMOMExtern[size];
	ackVector.copyInto(ackTab);
			
			
	/* send the ack outside the synchronized object to avoid to stand
	 * a long time in this portion of code
	 */
		
	fr.dyade.aaa.mom.AckMSPMessageMOMExtern msgAck = new fr.dyade.aaa.mom.AckMSPMessageMOMExtern(messageJMSMOMID, ackTab);
	/* synchronization because it could arrive that the notify was
	 * called before the wait 
	 */
	synchronized(obj) {
	  /* the processus of the client waits the response */
	  refConnection.waitThreadTable.put(longMsgID,new ResponseAckObject(obj, size));
	  /* get the messageJMSMOM identifier */
	  this.sendToConnection(msgAck);
					
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
      /*	else : acknowledge of a message already acknowledged
       *	=> no send
       */
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
	
  /** allows the session to acknowledge messages for the commit */
  private void acknowledgeXAMessage() throws javax.jms.JMSException {
    try {
      Object obj = new Object();
      long messageJMSMOMID = refConnection.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      /* preparing the ackMSP */
      Vector ackVector = preparesTransactedAck(messageJMSMOMID);
      if(!ackVector.isEmpty()) {
	int size = ackVector.size();
	fr.dyade.aaa.mom.MessageMOMExtern[] ackTab = new fr.dyade.aaa.mom.MessageMOMExtern[size];
	ackVector.copyInto(ackTab);
						
	/* send the ack outside the synchronized object to avoid to stand
	 * a long time in this portion of code
	 */
	fr.dyade.aaa.mom.AckMSPMessageMOMExtern msgAck = new fr.dyade.aaa.mom.AckMSPMessageMOMExtern(messageJMSMOMID, ackTab);

	/* synchronization because it could arrive that the notify was
	 * called before the wait 
	 */
	synchronized(obj) {
	  /* the processus of the client waits the response */
	  refConnection.waitThreadTable.put(longMsgID,new ResponseAckObject(obj, size));
	  /* get the messageJMSMOM identifier */
	  this.sendToConnection(msgAck);
					
	  obj.wait();
	}
				
	/* the clients wakes up */
	fr.dyade.aaa.mom.MessageMOMExtern msgResponse;
		
	/* tests if the key exists 
	 * dissociates the message null (on receiveNoWait) and internal error
	 */
	if(!refConnection.messageJMSMOMTable.containsKey(longMsgID))
	  throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
				
	/* get the message back or the exception*/
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
      /*	else : acknowledge of a message already acknowledged
       *	=> no send
       */
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
	
  /** allows the session to acknowledge messages for the commit */
  protected abstract void rollbackXAMessage() throws javax.jms.JMSException ;

  /** allows the session to rollback a delivery message */
  protected abstract void rollbackDeliveryMsg() throws javax.jms.JMSException;

  /** prepares the messages to acknowledge so as to decrease the overhead */
  protected abstract Vector preparesHandlyAck(String messageID, long messageJMSMOMID) throws javax.jms.JMSException;
	
  /** prepares the messages to acknowledge so as to decrease the overhead  */
  protected abstract Vector preparesTransactedAck(long messageJMSMOMID) throws javax.jms.JMSException;
	
  /** prepares the messages to recover the messages of the session */
  protected abstract fr.dyade.aaa.mom.RecoverObject[] preparesRecover() throws javax.jms.JMSException;
	
  /** sends a fr.dyade.aaa.mom.SendQueueMessage or SendTopicMessage 
   *	and waits the requestAgree or the Exception
   */
  protected void sendMessage(fr.dyade.aaa.mom.MessageMOMExtern msgSend) throws javax.jms.JMSException{
    try {
      long messageJMSMOMID = msgSend.getMessageMOMExternID() ;
      Long longMsgID = new Long(messageJMSMOMID);
      Object obj = new Object();
		
      /* synchronization because it could arrive that the notify was
       * called before the wait 
       */
      synchronized(obj) {
	/* the processus of the client waits the response */
	refConnection.waitThreadTable.put(longMsgID,obj);
	/* sends the messageJMSMOM r */
	this.sendToConnection(msgSend);
						
	obj.wait();
      }

      /* the clients wakes up */
      fr.dyade.aaa.mom.MessageMOMExtern msgMOM;
			
      /* get the the message back or the exception*/
      msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) refConnection.messageJMSMOMTable.remove(longMsgID);
      if(msgMOM instanceof fr.dyade.aaa.mom.SendingBackMessageMOMExtern) {
	/* update the fields of the message */
	fr.dyade.aaa.mom.SendingBackMessageMOMExtern msgSendBack = (fr.dyade.aaa.mom.SendingBackMessageMOMExtern) msgMOM;
      } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern) {
	/* exception sent back to the client */
	fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionSendMessageMOMExtern) msgMOM;
	fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	except.setLinkedException(msgExc.exception);
	throw(except);
      } else if(msgMOM instanceof fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern) {
	/* exception sent back to the client */
	fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern msgExc = (fr.dyade.aaa.mom.ExceptionUnknownObjMOMExtern) msgMOM;
	javax.jms.InvalidDestinationException except = new javax.jms.InvalidDestinationException("Invalid Queue :  ",String.valueOf(JMSAAAException.MOM_INTERNAL_ERROR));
	except.setLinkedException(msgExc.exception);
	throw(except);
      } else if(msgMOM instanceof fr.dyade.aaa.mom.MessageAckTransactedVector) {
	/* OK */
      } else if(msgMOM instanceof fr.dyade.aaa.mom.MessageAckTransactedRollback) {
	/* OK */
      } else {
	/* unknown message */
	/* should never arrived */
	fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("MOM Internal Error : ",JMSAAAException.MOM_INTERNAL_ERROR);
	throw(except);
      }
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }	
  }
	
  /*	reset the message to put the mode in readOnly and to
   *	destroy the transient attributes
   */
  protected void resetMessage(javax.jms.Message msg) throws javax.jms.JMSException {
    if(msg instanceof fr.dyade.aaa.mom.TextMessage)
      ((fr.dyade.aaa.mom.TextMessage) msg).reset();
    else if(msg instanceof fr.dyade.aaa.mom.ObjectMessage)
      ((fr.dyade.aaa.mom.ObjectMessage) msg).reset();
    else if(msg instanceof fr.dyade.aaa.mom.BytesMessage) 
      ((fr.dyade.aaa.mom.BytesMessage) msg).reset();
    else if(msg instanceof fr.dyade.aaa.mom.StreamMessage)
      ((fr.dyade.aaa.mom.StreamMessage) msg).reset();
    else if(msg instanceof fr.dyade.aaa.mom.MapMessage)
      ((fr.dyade.aaa.mom.MapMessage) msg).reset();
		
  }
	
  /** add a new message in the queue of the session for implicit receptions */
  protected void addNewMessage(fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgMOM) {
    if(!this.XARollback) {
      msgNoDeliveredQueue.push(msgMOM);
    }
  }
}

