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
import java.net.*;
import java.io.*;

/**	an AAA object Connection implements 2 interfaces so as to use the same
 *	agentClient in the MOM.
 *
 */
public class Connection implements javax.jms.Connection, javax.jms.QueueConnection, javax.jms.TopicConnection {
  
  /** - addrProxy : adresse de la machine o* est positionn* le TCP Proxy */ 
  private java.net.InetAddress addrProxy;  
  
  /** - portProxy : port d'*coute du TCP Proxy */ 
  private int portProxy; 
  
  /** the socket of the connection */
  protected Socket sock ;
  
  /** the name of tha agentClient in the mom */
  private String agentClient; 
  
  /** the security reference */	 
  private String login ; 
  private String passwd ; 
  
  /** ObjectOutputStream */
  protected ObjectOutputStream oos;
  /** ObjectInputStream */
  protected ObjectInputStream ois;
  
  /** test if the startMode is activated */
  private boolean startMode;
  private Boolean startModeObject;
  
  /** the clientId of the current client */
  private java.lang.String clientID;
  
  /** the exceptionListener of the Connection */
  protected javax.jms.ExceptionListener exceptionListener = null;
  
  /** the counter of messages 2^63-1*/
  private long counterMessage;
  
  /**	hashtable with all threads waiting a response 
   *	key : Long requestID
   *	Object : the object where the Thread stopped or a ResponseAckObject
   */
  public Hashtable waitThreadTable;
	
  /** hashtable containing the message received by the Object
   *	so as to the corresponding Thread retrieves it
   *	key : long requestID
   *	Object : MessageMOMExtern
   */
  public Hashtable messageJMSMOMTable;
  
  /** counter for message identifier */
  private long counterSessionID;
 
  /** the thread listening the socket */
  private Driver driver;
  
  /**	hashtable with all Session Threads waiting a response 
   *	key : fr.dyade.aaa.mom.KeyConnectionSubscription
   *	Object : fr.dyade.aaa.joram.Session
   *	optimisation for non durable subscription not yet implemented because the 
   *	using is not proved
   */
  public Hashtable subscriptionListenerTable;
	

  public Connection(String agentClient, java.net.InetAddress addrProxy, int portProxy, String login, String passwd) throws javax.jms.JMSException{
    try {
      
      /* parameters of the connection */
      this.agentClient = agentClient;
      this.addrProxy = addrProxy;
      this.portProxy = portProxy;
      this.login = login; 
      this.passwd = passwd;
      
      /* initialization of the counter of messages */
      counterMessage = 0;
      /* initialization of the hashtable with objects waiting */
      waitThreadTable = new Hashtable();
      /* initialization of the hashtable with messageJMS */
      messageJMSMOMTable = new Hashtable();
      /* initialisation of the subscriptionListenerTable */
      subscriptionListenerTable = new Hashtable();
      /* initialisation of the sesion counter */
      counterSessionID = 1;

      /* stop mode */
      startMode = false;
      startModeObject = new Boolean(true);

      sock = new Socket(addrProxy, portProxy);
      sock.setTcpNoDelay(true);
      sock.setSoTimeout(0);
      sock.setSoLinger(true,1000);
			
      /* send the name of the agentClient */
      DataOutputStream dos = new DataOutputStream(sock.getOutputStream()); 
      dos.writeUTF(agentClient);
      dos.flush();		

      /* creation of the objectinputStream and ObjectOutputStream */
      oos = new ObjectOutputStream(sock.getOutputStream()); 
      ois = new ObjectInputStream(new BufferedInputStream(sock.getInputStream()));

      /* creation of the thread listening */
      driver = new Driver(this, ois);
    } catch (IOException exc) {
      fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("IOException Error Sending Message: ",JMSAAAException.ERROR_CONNECTION_MOM);
      except.setLinkedException(exc);
      throw(except);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
  
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void close() throws javax.jms.JMSException {
    try {
      driver.stop();
      oos.close();
      ois.close();
      sock.close();
      sock = null;
      messageJMSMOMTable.clear();
      waitThreadTable.clear();
      subscriptionListenerTable.clear();
      System.gc();
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public java.lang.String getClientID() throws javax.jms.JMSException {
    try {
      return clientID;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
 
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void setClientID(java.lang.String clientID) throws javax.jms.JMSException {
    try {
      this.clientID = clientID;
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.ConnectionMetaData getMetaData() throws javax.jms.JMSException {
    try {
      Object obj = new Object();
      long messageJMSMOMID = this.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
			
      /* construction of the REquest MetaDAta MessageJMSMOM */
      fr.dyade.aaa.mom.MetaDataRequestMOMExtern msgMetaDataReq = new fr.dyade.aaa.mom.MetaDataRequestMOMExtern(messageJMSMOMID);
		
      /*	synchronization because it could arrive that the notify was
       *	called before the wait 
       */
      synchronized(obj) {
	/* the processus of the client waits the response */
	waitThreadTable.put(longMsgID, obj);
	/* get the messageJMSMOM identifier */
	this.sendMsgToAgentClient(msgMetaDataReq);
				
	obj.wait();
      }
					
      /* the clients wakes up */
      fr.dyade.aaa.mom.MessageMOMExtern msgResponse;
		
      /* tests if the key exists 
       * dissociates the message null (on receiveNoWait) and internal error
       */
      if(!messageJMSMOMTable.containsKey(longMsgID))
	throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
				
      /* get the the message back or the exception*/
      msgResponse = (fr.dyade.aaa.mom.MessageMOMExtern) messageJMSMOMTable.remove(longMsgID);
      if(msgResponse instanceof fr.dyade.aaa.mom.MetaDataMOMExtern) {
	fr.dyade.aaa.mom.MetaData metaData = ((fr.dyade.aaa.mom.MetaDataMOMExtern)msgResponse).metaData;
	return(new fr.dyade.aaa.joram.ConnectionMetaData(metaData.getJMSVersion(), metaData.getJMSMajorVersion(), metaData.getJMSMinorVersion(), metaData.getJMSProviderName(), metaData.getProviderVersion(), metaData.getProviderMajorVersion() ,metaData.getProviderMinorVersion()));
			
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
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }

    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public javax.jms.ExceptionListener getExceptionListener() throws javax.jms.JMSException {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
    }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void setExceptionListener(javax.jms.ExceptionListener listener) throws javax.jms.JMSException {
    try {
      /* set the exceptionListener*/
      this.exceptionListener = listener;
      
      Object obj = new Object();
      long messageJMSMOMID = this.getMessageMOMID();
      Long longMsgID = new Long(messageJMSMOMID);
      
      /* warns the agentClient of the set of the ExcpetionListener */
      fr.dyade.aaa.mom.SettingExcListenerMOMExtern msgExcListen;
      if(exceptionListener==null)
	msgExcListen = new fr.dyade.aaa.mom.SettingExcListenerMOMExtern(messageJMSMOMID, false);
      else
	msgExcListen = new fr.dyade.aaa.mom.SettingExcListenerMOMExtern(messageJMSMOMID, true);
			
      /*	synchronization because it could arrive that the notify was
       *	called before the wait 
       */
      synchronized(obj) {
	/* the processus of the client waits the response */
	waitThreadTable.put(longMsgID,obj);
	/* get the messageJMSMOM identifier */
	this.sendMsgToAgentClient(msgExcListen);
				
	obj.wait();
      }
					
      /* the clients wakes up */
      fr.dyade.aaa.mom.MessageMOMExtern msgResponse;
		
      /* tests if the key exists 
       * dissociates the message null (on receiveNoWait) and internal error
       */
      if(!messageJMSMOMTable.containsKey(longMsgID))
	throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
				
      /* get the the message back or the exception*/
      msgResponse = (fr.dyade.aaa.mom.MessageMOMExtern) messageJMSMOMTable.remove(longMsgID);
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
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void start() throws javax.jms.JMSException {
    try {
      /* to avoid conflict with start and stop mode in concurrency levels */
      synchronized(startModeObject) {
	if(!startMode) {
	  startMode = true;
	  
	  Object obj = new Object();
	  long messageJMSMOMID = this.getMessageMOMID();
	  Long longMsgID = new Long(messageJMSMOMID);
			
	  /* send to the agentClient to start delivery of the messages  */
	  fr.dyade.aaa.mom.StateListenMessageMOMExtern msgListen = new fr.dyade.aaa.mom.StateListenMessageMOMExtern(messageJMSMOMID, true);
		
	  /*	synchronization because it could arrive that the notify was
	   *	called before the wait 
	   */
	  synchronized(obj) {
	    /* the processus of the client waits the response */
	    waitThreadTable.put(longMsgID,obj);
	    /* get the messageJMSMOM identifier */
	    this.sendMsgToAgentClient(msgListen);
						
	    obj.wait();
	  }
					
	  /* the clients wakes up */
	  fr.dyade.aaa.mom.MessageMOMExtern msgResponse;
		
	  /* tests if the key exists 
	   * dissociates the message null (on receiveNoWait) and internal error
	   */
	  if(!messageJMSMOMTable.containsKey(longMsgID))
	    throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
				
	  /* get the the message back or the exception*/
	  msgResponse = (fr.dyade.aaa.mom.MessageMOMExtern) messageJMSMOMTable.remove(longMsgID);
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
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public void stop() throws javax.jms.JMSException {
    try {
      /* to avoid conflict with start and stop mode in concurrency levels */
      synchronized(startModeObject) {
	if(startMode) {
	  startMode = false;
	  
	  Object obj = new Object();
	  long messageJMSMOMID = this.getMessageMOMID();
	  Long longMsgID = new Long(messageJMSMOMID);
			
	  /* send to the agentClient to stop delivery of the messages  */
	  fr.dyade.aaa.mom.StateListenMessageMOMExtern msgListen = new fr.dyade.aaa.mom.StateListenMessageMOMExtern(messageJMSMOMID, false);
		
	  /*	synchronization because it could arrive that the notify was
	   *	called before the wait 
	   */
	  synchronized(obj) {
	    /* the processus of the client waits the response */
	    waitThreadTable.put(longMsgID,obj);
	    /* get the messageJMSMOM identifier */
	    this.sendMsgToAgentClient(msgListen);
						
	    obj.wait();
	  }
					
	  /* the clients wakes up */
	  fr.dyade.aaa.mom.MessageMOMExtern msgResponse;
		
	  /* tests if the key exists 
	   * dissociates the message null (on receiveNoWait) and internal error
	   */
	  if(!messageJMSMOMTable.containsKey(longMsgID))
	    throw (new fr.dyade.aaa.joram.JMSAAAException("No Request Agree received",JMSAAAException.ERROR_NO_MESSAGE_AVAILABLE));
				
	  /* get the the message back or the exception*/
	  msgResponse = (fr.dyade.aaa.mom.MessageMOMExtern) messageJMSMOMTable.remove(longMsgID);
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
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.QueueSession createQueueSession(boolean transacted, int acknowledgeMode) throws javax.jms.JMSException { 
    try {
	long counterSessionIDNew = counterSessionID;
	counterSessionID = calculateMessageID(counterSessionID);
	fr.dyade.aaa.joram.QueueSession session = new fr.dyade.aaa.joram.QueueSession(transacted, acknowledgeMode, counterSessionID,this);
	if(session==null) {
	    counterSessionID = counterSessionID - 1;
	throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation QueueSession",JMSAAAException.ERROR_CREATION_SESSION));
	} else {
	return session;
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  } 
	 
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Queue queue, java.lang.String messageSelector, javax.jms.ServerSessionPool sessionPool, int maxMessages) throws javax.jms.JMSException { 
    throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
  }

    /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
    public javax.jms.ConnectionConsumer createDurableConnectionConsumer(javax.jms.Topic topic,
						       String subscriptionName,
						       String messageSelector,
						       javax.jms.ServerSessionPool sessionPool,
						       int maxMessages) throws javax.jms.JMSException {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
    }
  
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.TopicSession createTopicSession(boolean transacted, int acknowledgeMode) throws javax.jms.JMSException { 
    try {
	long counterSessionIDNew = counterSessionID;
	counterSessionID = calculateMessageID(counterSessionID);
	fr.dyade.aaa.joram.TopicSession session = new fr.dyade.aaa.joram.TopicSession(transacted, acknowledgeMode, counterSessionIDNew, this);
	if(session==null) {
	    counterSessionID = counterSessionID - 1;
	    throw (new fr.dyade.aaa.joram.JMSAAAException("Internal Error during creation TopicSession",JMSAAAException.ERROR_CREATION_SESSION));
	} else {
	return session;
      }
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.ConnectionConsumer createConnectionConsumer(javax.jms.Topic queue, java.lang.String messageSelector, javax.jms.ServerSessionPool sessionPool, int maxMessages) throws javax.jms.JMSException { 
    throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
  }
	
  /** @see <a href="http://java.sun.com/products/jms/index.html"> JMS_Specifications */
  public javax.jms.ConnectionConsumer createDurableConnectionConsumer(javax.jms.Topic topic, java.lang.String messageSelector, javax.jms.ServerSessionPool sessionPool, int maxMessages) throws javax.jms.JMSException { 
    throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
  }
	
  /** sendMessage to agentClient thanks to the socket
   *	return the messageJMSMOMID to retrieve the messageJMSMOM
   */
  public synchronized void sendMsgToAgentClient(fr.dyade.aaa.mom.MessageMOMExtern msgMOM) throws javax.jms.JMSException{
    try {
      /* add the id of the messageJMSMOM */
      if(Debug.debug)
	  if(Debug.connect)
	      System.out.println("METHOD sendMsgToAgentClient "+msgMOM.getMessageMOMExternID()+" "+msgMOM.getClass().getName());	 
		
			
      if(msgMOM instanceof fr.dyade.aaa.mom.SendingMessageQueueMOMExtern) {
	fr.dyade.aaa.mom.SendingMessageQueueMOMExtern msgSend = (fr.dyade.aaa.mom.SendingMessageQueueMOMExtern) msgMOM;
	/* update the timeStamp */
	msgSend.message.setJMSTimestamp(System.currentTimeMillis()-msgSend.message.getJMSTimestamp());
      } else if(msgMOM instanceof fr.dyade.aaa.mom.SendingMessageTopicMOMExtern) {
	fr.dyade.aaa.mom.SendingMessageTopicMOMExtern msgSend = (fr.dyade.aaa.mom.SendingMessageTopicMOMExtern) msgMOM;
	/* update the timeStamp */
	msgSend.message.setJMSTimestamp(System.currentTimeMillis()-msgSend.message.getJMSTimestamp());
      } 

      /* send the message to the socket */
      oos.writeObject(msgMOM);
      oos.flush();
      oos.reset();
			
    } catch (IOException exc) {
	if ( msgMOM instanceof fr.dyade.aaa.mom.AckTopicMessageMOMExtern )
	    System.out.println("Socket close : can't write Ack");
	//exc.printStackTrace();
	else {
	    fr.dyade.aaa.joram.JMSAAAException except = new fr.dyade.aaa.joram.JMSAAAException("IOException Error Sending Message: ",JMSAAAException.ERROR_CONNECTION_MOM);
	    except.setLinkedException(exc);
	    throw(except);
	}
    } catch (javax.jms.JMSException exc) {
      throw(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      throw(except);
    }
  }
	
  /**  extract message from the socket */
  protected void extractMessage(fr.dyade.aaa.mom.MessageMOMExtern msgMOM) throws Exception {
    try {
      if (msgMOM instanceof fr.dyade.aaa.mom.MessageQueueDeliverMOMExtern) {
	deliveryRequestAgree(msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) {
	deliveryMessageTopic((fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern) msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.RequestAgreeMOMExtern) {
	deliveryRequestAgree(msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.SendingBackMessageMOMExtern) {
	deliveryRequestAgree(msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.ReadDeliverMessageMOMExtern) {
	deliveryRequestAgree(msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.ExceptionMessageMOMExtern) {
	deliveryException((fr.dyade.aaa.mom.ExceptionMessageMOMExtern) msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageAckTransactedVector) {
	deliveryRequestAgree(msgMOM);
      } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageAckTransactedRollback) {
	deliveryRequestAgree(msgMOM);
      } else {
	/* if no messageID presents in the hashtable, it's an exception 
	 *	to deliver to the ExceptionListener
	 */
//      if (exceptionListener!=null)
//	  exceptionListener.onException(( javax.jms.JMSException) exc);
      }
    } catch (javax.jms.JMSException exc) {
      /* delivery to the ExceptionListener */
      if(exceptionListener!=null)
	exceptionListener.onException(exc);
    } catch (Exception exc) {
      javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
      except.setLinkedException(exc);
      /* delivery to the ExceptionListener */ 
      if(exceptionListener!=null)
	exceptionListener.onException(except);
    }
  }
	
  /** receives the ExceptionMessageMOMExtern
   *	checks if the associated method exists
   *	checks if its multiple acknowledgments
   *	puts the message received in a queue so as to the client is able to
   *	get it 
   *	finally wakes up the client
   */
  private void deliveryException(fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgMOM) throws Exception{
    Object objWait;
    Long longMsgID = new Long(msgMOM.getMessageMOMExternID());
		
    /* search of the correspondant request */
    if((objWait = waitThreadTable.get(longMsgID))==null)
      throw new fr.dyade.aaa.joram.JMSAAAException("No request for this message",
					 JMSAAAException.NO_SUCH_REQUEST_SENT);
    
    /* case of multiple acknowledgments*/
    if(objWait instanceof ResponseAckObject) {
      ResponseAckObject responseObj = (ResponseAckObject) objWait;
      /* add the messageJMSMOM in the hashtable so as to the corresponding 
       *	Thread retrieves it
       */
      messageJMSMOMTable.put(longMsgID, msgMOM);
		
      /* wake up the Thread */
      synchronized(responseObj.obj) {
	(responseObj.obj).notify();
      }
    } else {
      /* add the messageJMSMOM in the hashtable so as to the corresponding 
       *	Thread retrieves it
       */
      messageJMSMOMTable.put(longMsgID, msgMOM);
		
      /* wake up the Thread */
      synchronized(objWait) {
	objWait.notify();
      }
    }
  }
	
  /** receives the MessageJMSMOM
   *	checks if the associated method exists
   *	checks if its multiple acknowledgments
   *	puts the message received in a queue so as to the client is able to
   *	get it 
   *	finally wakes up the client
   */
  private void deliveryRequestAgree(fr.dyade.aaa.mom.MessageMOMExtern msgMOM) throws Exception{
    Object objWait;
    Long longMsgID = new Long(msgMOM.getMessageMOMExternID());

    /* search of the correspondant request */
    if((objWait = waitThreadTable.get(longMsgID))==null) {
      throw new fr.dyade.aaa.joram.JMSAAAException("No request for this message",
					JMSAAAException.NO_SUCH_REQUEST_SENT);
    }

    /* case of multiple acknowledgments*/
    if(objWait instanceof ResponseAckObject) {
      /* decrementation of the counter */
      ResponseAckObject responseObj = (ResponseAckObject) objWait;
      responseObj.responseCounter--;
			
      /* wake up the Thread if all the responses are arrived */
      if(responseObj.responseCounter==0) {
	/* Add the messageJMSMOM in the hashtable so as to the corresponding 
	 * thread retrieves it
	 */
	messageJMSMOMTable.put(longMsgID, msgMOM);
	/* wake up the Thread */
	synchronized(responseObj.obj) {
	  (responseObj.obj).notify();
	}
      }
    } else {
      /* add the messageJMSMOM in the hashtable so as to the corresponding 
       *	Thread retrieves it
       */
      messageJMSMOMTable.put(longMsgID, msgMOM);
		
      /* wake up the Thread */
      synchronized(objWait) {
	objWait.notify();
      }
    }
  }
	
  /** delivery a message from a Topic to the appropriated Session */
  private void deliveryMessageTopic(fr.dyade.aaa.mom.MessageTopicDeliverMOMExtern msgMOM) throws Exception {
    fr.dyade.aaa.mom.TopicNaming topic = (fr.dyade.aaa.mom.TopicNaming) msgMOM.message.getJMSDestination();
    KeyConnectionSubscription key = new KeyConnectionSubscription(msgMOM.nameSubscription, topic.getTopicName(), msgMOM.theme);

    fr.dyade.aaa.joram.Session session = (fr.dyade.aaa.joram.Session) subscriptionListenerTable.get(key); 

    /* search of the correspondant request */
    if(session == null)
      throw (new fr.dyade.aaa.joram.JMSAAAException("No Session is listening for this message",JMSAAAException.NO_SESSION_LISTENING));

    /* put the message in the Sesion object*/
    session.addNewMessage(msgMOM);
  }
	
  /** incrementation of the counter with the syntax
   *	a,b,c,...,z,aa,ab,ac,...,az,ba,... 
   * this counter serves to give a name to not durable subscription
   *
  public static String calculateMessageID(String objectID) {
    char[] chtmp;
    int i=0;
    int length = objectID.length();

    chtmp = objectID.toCharArray();
    while(i<length) {
      if(chtmp[length-1-i]=='z') {
	chtmp[length-1-i] = 'a';
	i++;
      }
      else {
	chtmp[length-1-i] = (char) (chtmp[length-1-i] + 1);
	break;
      }
    } 		
    if(i==length)
      objectID = (new String(chtmp))+"a";
    else
      objectID = (new String(chtmp));
		
    return objectID;
  }
  */
  /** incrementation of the counter */
  public static long calculateMessageID(long objectID) {
    return objectID + 1;
    }

  /** returns the counter of the messageMOMID 
   *	and then increments it
   */
  public synchronized long getMessageMOMID() {
    /* increments the counter */
    if(counterMessage<Long.MAX_VALUE)
      counterMessage++;
    else
      counterMessage = (long) 0;
		
    /* return the requestID of the message */
    return counterMessage;	
  }
   /** decrease the counter of the messageMOMID */
  synchronized long decreaseMessageMOMID() {
    long counter =0;
    /* decrease the counter */
    if(counterMessage>0) {
      counter = counterMessage--;
    }
    return counter;
  }
} 
 
/** class driver to create the listener on the socket */
class Driver implements java.lang.Runnable {
  boolean stopDriver;

  /* reference to the connection object to */
  protected fr.dyade.aaa.joram.Connection refConnection;
	
  /*  inputStream of the socket */
  protected ObjectInputStream ois;
	
  /* The effecient thread */
  protected java.lang.Thread processListening;
	
  Driver(fr.dyade.aaa.joram.Connection refConnection, ObjectInputStream ois) {
    this.refConnection = refConnection;
    this.ois = ois;
    processListening = new Thread(this);
    processListening.setDaemon(true);
    processListening.start();
    stopDriver = false;
  }

  public void run() {
    while(true) {
      try {
	if(stopDriver)
	  break;
	Object obj = ois.readObject();
	if(obj instanceof fr.dyade.aaa.mom.MessageMOMExtern) {
	  fr.dyade.aaa.mom.MessageMOMExtern msgMOM = (fr.dyade.aaa.mom.MessageMOMExtern) obj;
	  refConnection.extractMessage(msgMOM);
	}
      } catch (Exception exc) {
	javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	except.setLinkedException(exc);
	if(refConnection.exceptionListener!=null)
	  refConnection.exceptionListener.onException(except);
      }
    }
  }

  public void stop() {
    stopDriver = true;
  }
}
