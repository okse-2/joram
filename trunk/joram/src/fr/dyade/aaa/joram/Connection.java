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
import java.lang.*; 
import java.util.*;
import java.net.*;
import java.io.*;

/**
 * A JMS Connection is a client's active connection to its JMS provider.
 * It will typically allocate provider resources outside the Java virtual
 * machine.
 *
 * @author Richard Mathis
 * @version 1.1
 */
public abstract class Connection implements javax.jms.Connection {
  
    /** The identifier of the proxy agent */
    protected String agentClient;
    /** The IP address of the proxy agent */
    protected InetAddress proxyAddress;
    /** The port of the proxy agent */
    protected int proxyPort;
  
    /** The socket that connects the client to the proxy agent */
    protected Socket socket;
  
    /** The security reference */
    private String userName; 
    private String password; 
  
    /** The clientId of the current client */
    private String clientID;
  
    /** The output stream to write data to the proxy */
    protected ObjectOutputStream oos;
    /** The input stream to read data from the proxy */
    protected ObjectInputStream ois;
  
    /** Whether or not the connection is closed */
    protected boolean isClosed;

    /** The state of the connection */
    private boolean started;
    private Boolean startedObject;
  
    /** The exceptionListener of the Connection */
    protected javax.jms.ExceptionListener exceptionListener = null;
  
    /** the counter of messages 2^63-1*/
    private long messageCounter;
    /** counter for message identifier */
    protected long sessionCounter;
 
    /**
     * Hashtable with all threads waiting for a response.<br>
     * key : Long requestID.<br>
     * Object : the object on which the Thread is blocking or a ResponseAckObject.
     */
    public Hashtable waitThreadTable;
	
    /**
     * Hashtable containing the message received by the client
     * so as to the corresponding Thread retrieves it.<br>
     *	key : long requestID.<br>
     *	Object : MessageMOMExtern.
     */
    public Hashtable messageJMSMOMTable;
  
    /** the thread listening the socket */
    private Driver driver;
  
    /**
     * Hashtable with all Session Threads waiting a response.<br>
     * key : fr.dyade.aaa.mom.KeyConnectionSubscription.<br>
     * Object : fr.dyade.aaa.joram.Session.<br>
     * Optimisation for non durable subscription not yet implemented because the
     * using is not proved.
     */
    public Hashtable subscriptionListenerTable;
	

    public Connection(String agentClient,
		      java.net.InetAddress proxyAddress, int proxyPort,
		      String userName, String password) throws javax.jms.JMSException {
	try {
      
	    /* parameters of the connection */
	    this.agentClient = agentClient;
	    this.proxyAddress = proxyAddress;
	    this.proxyPort = proxyPort;
	    this.userName = userName; 
	    this.password = password;
      
	    /* initialization of the counter of messages */
	    messageCounter = 0;
	    /* initialization of the hashtable with objects waiting */
	    waitThreadTable = new Hashtable();
	    /* initialization of the hashtable with messageJMS */
	    messageJMSMOMTable = new Hashtable();
	    /* initialisation of the subscriptionListenerTable */
	    subscriptionListenerTable = new Hashtable();
	    /* initialisation of the sesion counter */
	    sessionCounter = 1;

	    /* stop mode */
	    started = false;
	    startedObject = new Boolean(true);
	    /* Connection opened */
	    isClosed = false;

	    socket = new Socket(proxyAddress, proxyPort);
	    socket.setTcpNoDelay(true);
	    socket.setSoTimeout(0);
	    socket.setSoLinger(true,1000);
			
	    /* send the name of the agentClient */
	    DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); 
	    dos.writeUTF(agentClient);
	    dos.flush();		

	    /* creation of the objectinputStream and ObjectOutputStream */
	    oos = new ObjectOutputStream(socket.getOutputStream()); 
	    ois = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

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
  
    /**
     * Close the connection and run garbage collection.
     */
    public void close() throws javax.jms.JMSException {
	try {
	    if (!isClosed) {
		driver.stop();
		oos.close();
		ois.close();
		socket.close();
		socket = null;
		messageJMSMOMTable.clear();
		waitThreadTable.clear();
		subscriptionListenerTable.clear();
		System.gc();
		isClosed = true;
	    }
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
	
    /**
     * Get the client identifier for this connection.
     * @return the unique client identifier.
     */
    public java.lang.String getClientID() throws javax.jms.JMSException {
	try {
	    return clientID;
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
 
    /**
     * Set the client identifier for this connection.
     * @param clientID the unique client identifier.
     */
    public void setClientID(java.lang.String clientID) throws javax.jms.JMSException {
	try {
	    this.clientID = clientID;
	} catch (Exception exc) {
	    javax.jms.JMSException except = new javax.jms.JMSException("internal Error");
	    except.setLinkedException(exc);
	    throw(except);
	}
    }
	
    /**
     * Get the meta data for this connection.
     * @return the connection meta data.
     */
    public javax.jms.ConnectionMetaData getMetaData() throws javax.jms.JMSException {
	if (isClosed) throw new JMSException("Connection closed");
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

    /**
     * Get the ExceptionListener for this Connection.<br>
     * NOT IMPLEMENTED.
     */
    public javax.jms.ExceptionListener getExceptionListener() throws javax.jms.JMSException {
	throw (new fr.dyade.aaa.joram.JMSAAAException("Not yet available",JMSAAAException.NOT_YET_AVAILABLE));
    }
	
    /**
     * Set an exception listener for this connection.
     * @param listener the exception listener.
     */
    public void setExceptionListener(javax.jms.ExceptionListener listener) throws javax.jms.JMSException {
	if (isClosed) throw new JMSException("Connection closed");
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
	
    /**
     * Start (or restart) a <code>Connection</code>'s delivery of incoming
     * messages.
     */
    public void start() throws javax.jms.JMSException {
	try {
	    /* to avoid conflict with start and stop mode in concurrency levels */
	    synchronized(startedObject) {
		if(!started) {
		    started = true;
	  
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
	
    /**
     * Used to temporarily stop a <code>Connection</code>'s delivery of
     * incoming messages.
     */
    public void stop() throws javax.jms.JMSException {
	try {
	    /* to avoid conflict with start and stop mode in concurrency levels */
	    synchronized(startedObject) {
		if(started) {
		    started = false;
	  
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

    /**
     * Send a message to the proxy agent through the socket.
     */
    public synchronized void sendMsgToAgentClient(fr.dyade.aaa.mom.MessageMOMExtern msgMOM) throws javax.jms.JMSException{
	if (isClosed) throw new JMSException("Connection closed");
	try {
	    /* add the id of the messageJMSMOM */
	    if(Debug.debug)
		if(Debug.connect)
		    System.out.println("METHOD sendMsgToAgentClient "+msgMOM.getMessageMOMExternID()+" "+msgMOM.getClass().getName());	 
		
			
	    if (msgMOM instanceof fr.dyade.aaa.mom.SendingMessageQueueMOMExtern) {
		fr.dyade.aaa.mom.SendingMessageQueueMOMExtern msgSend = (fr.dyade.aaa.mom.SendingMessageQueueMOMExtern) msgMOM;
		/* update the timeStamp */
		msgSend.message.setJMSTimestamp(System.currentTimeMillis() - msgSend.message.getJMSTimestamp());
	    } else if(msgMOM instanceof fr.dyade.aaa.mom.SendingMessageTopicMOMExtern) {
		fr.dyade.aaa.mom.SendingMessageTopicMOMExtern msgSend = (fr.dyade.aaa.mom.SendingMessageTopicMOMExtern) msgMOM;
		/* update the timeStamp */
		msgSend.message.setJMSTimestamp(System.currentTimeMillis() - msgSend.message.getJMSTimestamp());
	    }

	    /* send the message to the socket */
	    oos.writeObject(msgMOM);
	    oos.flush();
	    oos.reset();
			
	} catch (IOException exc) {
	    exc.printStackTrace();
	    if ( msgMOM instanceof fr.dyade.aaa.mom.AckTopicMessageMOMExtern )
		System.out.println("Socket close : can't write Ack");
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
    
    /**
     * Extract message from the socket
     */
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
	    } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageAckXAPrepare) {
		deliveryRequestAgree(msgMOM);
	    } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageAckXACommit) {
		deliveryRequestAgree(msgMOM);
	    } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageAckXARollback) {
		deliveryRequestAgree(msgMOM);
	    } else if (msgMOM instanceof fr.dyade.aaa.mom.MessageAckXARecover) {
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
	
    /**
     * Receives the ExceptionMessageMOMExtern, checks if the associated
     * method exists, checks if its multiple acknowledgments, puts the message
     * received in a queue so as to the client is able to get it, finally
     * wakes up the client.
     */
    private void deliveryException(fr.dyade.aaa.mom.ExceptionMessageMOMExtern msgMOM) throws Exception {
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
	
    /**
     * Deliver a message from a Topic to the appropriated Session.
     */
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


    /**
     * Get the next message ID and increase the counter.
     */
    protected synchronized long getMessageMOMID() {
	if (messageCounter < Long.MAX_VALUE) messageCounter++;
	else messageCounter = 0;
	return messageCounter;
    }

    
    /**
     * Get the next session ID and increase the counter.
     */
    public synchronized long getNextSessionID() {
	if (sessionCounter < Long.MAX_VALUE) sessionCounter++;
	else sessionCounter = 0;
	return sessionCounter;
    }
} 


/**
 * A driver listens to the socket for incoming messages from the proxy.
 */
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
