/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2003 - 2004 Bull SA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest.jmsbridge;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.objectweb.joram.client.jms.XidImpl;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Debug;
import fr.dyade.aaa.common.Daemon;

/**
 * The <code>BridgeUnifiedModule</code> class is a bridge module based on the
 * JMS 1.1 unified semantics and classes.
 */
@Deprecated
public class JMSBridgeModule implements javax.jms.ExceptionListener,
                                            javax.jms.MessageListener,
                                            java.io.Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** logger */
  public static Logger logger = Debug.getLogger(JMSBridgeModule.class.getName());
  
  /** Identifier of the agent using this module. */
  protected AgentId agentId;

  /** Name of the JNDI factory class to use. */
  protected String jndiFactory = null;
  /** JNDI URL. */
  protected String jndiUrl = null;
  /** ConnectionFactory JNDI name. */
  protected String cnxFactName;
  /** Destination JNDI name. */
  protected String destName;
  /** Connection factory object for connecting to the foreign JMS server. */
  protected ConnectionFactory cnxFact = null;
  /** Foreign JMS destination object. */
  protected Destination dest = null;
  /** User identification for connecting to the foreign JMS server. */
  protected String userName = null;
  /** User password for connecting to the foreign JMS server. */
  protected String password = null;
  /** JMS clientID field. */
  protected String clientID = null;
  /** Selector for filtering messages. */
  protected String selector;

  /** <code>true</code> if the module is fully usable. */
  protected boolean usable = true;
  /** Message explaining why the module is not usable. */
  protected String notUsableMessage;

  /** Connection to the foreign JMS server. */
  protected transient Connection producerCnx;
  protected transient Connection consumerCnx;
  /** Session for sending messages to the foreign JMS destination. */
  protected transient Session producerSession;
  /** Session for getting messages from the foreign JMS destination. */
  protected transient Session consumerSession;
  /** Producer object. */
  protected transient MessageProducer producer;
  /** Consumer object. */
  protected transient MessageConsumer consumer;

  /** <code>true</code> if a listener has been set on the JMS consumer. */
  protected transient boolean listener;
  /** Vector holding the pending messages to send after reconnection. */
  protected transient Vector qout;

  /** Daemon used for requesting messages. */
  protected transient ConsumerDaemon consumerDaemon;
  /** Daemon used for the reconnection process. */
  protected transient ReconnectionDaemon reconnectionDaemon;

  /** 
   * Automatic receive for a bridge Queue.
   * The foreign messages are transfer in bridge queue, 
   * without client request.
   */
  private boolean automaticRequest = false;
  /** Indicates to use an XAConnection. Default is false. */
  private boolean isXA = false;
  /** producer XAResource */
  private transient XAResource producerRes = null;
  /** consumer XAResource */
  private transient XAResource consumerRes = null;
  /** serializable object for synchronization */
  private Object lock = new String();

  /**
   * Constructs a <code>BridgeUnifiedModule</code> module.
   * 
   * @param prop  JMS properties required for establishing the link with the
   *          foreign JMS server.
   */
  public JMSBridgeModule(Properties prop) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "<init>(" + prop + ')');

    jndiFactory = prop.getProperty("jndiFactory");
    jndiUrl = prop.getProperty("jndiUrl");
    
    cnxFactName = prop.getProperty("connectionFactoryName");
    if (cnxFactName == null)
      throw new IllegalArgumentException("Missing ConnectionFactory JNDI name.");

    destName = prop.getProperty("destinationName");
    if (destName == null)
      throw new IllegalArgumentException("Missing Destination JNDI name.");

    String userName = prop.getProperty("userName");
    String password = prop.getProperty("password");

    if (userName != null && password != null) {
      this.userName = userName;
      this.password = password;
    }

    clientID = prop.getProperty("clientId");
    selector = prop.getProperty("selector");
    automaticRequest = Boolean.valueOf(
          prop.getProperty("automaticRequest","false")).booleanValue();
    isXA = Boolean.valueOf(prop.getProperty("useXAConnection", "false")).booleanValue();
  } 


  /**
   * Initializes the module's parameters.
   *
   * @param agentId  Identifier of the agent using the module.
   *
   * @exception IllegalArgumentException  If the provided properties are
   *              invalid.
   */
  public void init(AgentId agentId) {
    this.agentId = agentId;
  }
  
  /**
   * Launches the connection process to the foreign JMS server.
   *
   * @exception javax.jms.IllegalStateException  If the module can't access
   *              the foreign JMS server.
   * @exception javax.jms.JMSException  If the needed JMS resources can't be
   *              created.
   */
  public void connect() throws JMSException {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "connect()");

    listener = false;
    // Creating the module's daemons.
    consumerDaemon = new ConsumerDaemon();
    reconnectionDaemon = new ReconnectionDaemon();
    
    // Administered objects have not been retrieved: launching the startup
    // daemon.
    if (cnxFact == null || dest == null) {
      StartupDaemon startup = new StartupDaemon();
      startup.start();
    } else {
      // Administered objects have been retrieved: connecting.
      try {
        if (isXA) {
          doXAConnect();
        } else {
          doConnect();
        }
        // start daemon.
        consumerDaemon.start();
      } catch (JMSException exc) {
        reconnectionDaemon.reconnect();
      }
    }
  }

  /**
   * Sets a message listener on the foreign JMS destination.
   *
   * @exception javax.jms.IllegalStateException  If the module state does
   *              not allow to set a listener.
   */
  public void setMessageListener() throws IllegalStateException {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "setMessageListener()");
    
    listener = true;
    try {
      setConsumer();
      consumer.setMessageListener(this);
      consumerCnx.start();
    } catch (JMSException exc) {}
  } 

  /**
   * Unsets the set message listener on the foreign JMS destination.
   */
  public void unsetMessageListener() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JMSBridgeModule.unsetMessageListener()");
    
    try {
      consumerCnx.stop();
      consumer.setMessageListener(null);
      unsetConsumer();
    } catch (JMSException exc) {}
    listener = false;
  }

  /**
   * Synchronous method requesting an immediate delivery from the foreign
   * JMS destination.
   *
   * @return  The JMS message formatted into a JORAM MOM message, or
   *          <code>null</code> if no message is available or if the request
   *          fails.
   *
   * @exception javax.jms.IllegalStateException  If the module state does
   *              not allow to request a message.
   */
  public Message receiveNoWait() throws IllegalStateException {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "receiveNoWait()");
    
    Message momMessage = null;
    
    if (! automaticRequest) {
      // Be careful, with automaticRequest set to true this code causes a dead-lock.
      // In this case if there is no available message return null.
      
      synchronized (lock) {
        try {
          setConsumer();
          consumerCnx.start();
          Xid xid = null;
          try {
            if (isXA) {
              xid = new XidImpl(new byte[0], 1, (agentId.toString() + System.currentTimeMillis()).getBytes());
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "receiveNoWait: XA xid=" + xid);

              try {
                consumerRes.start(xid, XAResource.TMNOFLAGS);
              } catch (XAException e) {
                if (logger.isLoggable(BasicLevel.WARN))
                  logger.log(BasicLevel.WARN, "Exception:: XA can't start resource : " + consumerRes, e);
              }
            }
            javax.jms.Message msg = consumer.receiveNoWait();
            if (msg != null) {
              org.objectweb.joram.client.jms.Message clientMessage = 
                org.objectweb.joram.client.jms.Message.convertJMSMessage(msg);
              org.objectweb.joram.client.jms.Message.prepareJMSMessage(clientMessage);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "receiveNoWait: clientMessage=" + clientMessage);

              momMessage = clientMessage.getMomMsg();
            } else {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "receiveNoWait: no message available");
            }

            if (isXA) {
              try {
                consumerRes.end(xid, XAResource.TMSUCCESS);
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "receiveNoWait: XA end " + consumerRes);

              } catch (XAException e) {
                throw new JMSException("XA resource end(...) failed: " + consumerRes + " :: " + e.getMessage());
              }
              try {
                int ret = consumerRes.prepare(xid);
                if (ret == XAResource.XA_OK)
                  consumerRes.commit(xid, false);
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "receiveNoWait: XA commit " + consumerRes);
              } catch (XAException e) {
                try {
                  consumerRes.rollback(xid);
                  if (logger.isLoggable(BasicLevel.DEBUG))
                    logger.log(BasicLevel.DEBUG, "receiveNoWait: XA rollback" + consumerRes);
                } catch (XAException e1) { }
                throw new JMSException("XA resource rollback(" + xid + ") failed: " + 
                                       consumerRes + " :: " + e.getMessage());
              }
            } else {
              consumerSession.commit();
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "receiveNoWait: commit " + consumerSession);
            }
          } catch (MessageFormatException exc) {
            // Conversion error: denying the message.
            if (isXA) {
              try {
                consumerRes.rollback(xid);
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "receiveNoWait: XA rollback " + consumerRes);
              } catch (XAException e1) { }
            } else {
              consumerSession.rollback();
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "receiveNoWait: rollback " + consumerSession);
            }
          }
        } catch (JMSException commitExc) {
          // Connection start, or session commit/rollback failed:
          // setting the message to null.
          momMessage = null;
        }
      }
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "receiveNoWait: momMessage=" + momMessage);

    return momMessage;
  }

  /**
   * Asynchronous method requesting a delivery from the foreign
   * JMS destination.
   *
   * @exception javax.jms.IllegalStateException  If the module state does
   *              not allow to request a message.
   */
  public void receive() throws IllegalStateException {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "receive()");
    
    consumerDaemon.receive();
  }
  
  /**
   * Sends a message to the foreign JMS destination.
   *
   * @exception javax.jms.IllegalStateException  If the module's state does
   *              not permit message sendings.
   * @exception javax.jms.MessageFormatException  If the MOM message could not
   *              be converted into a foreign JMS message.
   */
  public void send(org.objectweb.joram.shared.messages.Message message)
  throws JMSException {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "send(" + message + ')');

    synchronized (lock) {
      try {
        Xid xid = null;
        if (isXA) {
          xid = new XidImpl(new byte[0], 1, message.id.getBytes());
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "send: xid=" + xid);

          try {
            producerRes.start(xid, XAResource.TMNOFLAGS);
          } catch (XAException e) {
            if (logger.isLoggable(BasicLevel.WARN))
              logger.log(BasicLevel.WARN, "Exception:: XA can't start resource : " + producerRes, e);
          }
        }
        producer.send(org.objectweb.joram.client.jms.Message.wrapMomMessage(null, message));
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "send: " + producer + " send.");
        acknowledge(message);
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "send: acknowledge.");
        if (isXA) {
          try {
            producerRes.end(xid, XAResource.TMSUCCESS);
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "send: XA end " + producerRes);
          } catch (XAException e) {
            throw new JMSException("resource end(...) failed: " + producerRes + " :: " + e.getMessage());
          }
          try {
            int ret = producerRes.prepare(xid);
            if (ret == XAResource.XA_OK)
              producerRes.commit(xid, false);
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "send: XA commit " + producerRes);
          } catch (XAException e) {
            try {
              producerRes.rollback(xid);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "send: XA rollback " + producerRes);
            } catch (XAException e1) { }
            throw new JMSException("XA resource rollback(" + xid + ") failed: " + 
                producerRes + " :: " + e.getMessage());
          }
        }
      } catch (javax.jms.JMSException exc) {
        // Connection failure? Keeps the message for later delivery.
        qout.add(message);
        // Try to reconnect?
        onException(exc);
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "send: Exception qout=" + qout);
      }
    }
  }

  /** 
   * Interrupts the daemons and closes the connection.
   */
  public void close() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JMSBridgeModule.close()");

    try {
      if (consumerCnx != null)
        consumerCnx.setExceptionListener(null);
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }
    try {
      if (producerCnx != null)
        producerCnx.setExceptionListener(null);
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }

    try {
      if (producerCnx != null)
        producerCnx.stop();
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }
    try {
      if (consumerCnx != null)
        consumerCnx.stop();
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }

    unsetMessageListener();

    try {
      consumerDaemon.stop();
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }
    try {
      reconnectionDaemon.stop();
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }

    try {
      if (producerCnx != null)
        producerCnx.close();
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }
    try {
      if (consumerCnx != null)
        consumerCnx.close();
    } catch (Exception exc) {
      logger.log(BasicLevel.WARN, "JMSBridgeModule.close", exc);
    }
  }

  /**
   * Implements the <code>javax.jms.ExceptionListener</code> interface for
   * catching the failures of the connection to the remote JMS server.
   * <p>
   * Reacts by launching a reconnection process.
   */
  public void onException(JMSException exc) {
    if (logger.isLoggable(BasicLevel.WARN))
      logger.log(BasicLevel.WARN, "onException(" + exc + ')');
    reconnectionDaemon.reconnect();
  }

  /**
   * Implements the <code>javax.jms.MessageListener</code> interface for
   * processing the asynchronous deliveries coming from the foreign JMS
   * server.
   */
  public void onMessage(javax.jms.Message jmsMessage) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage(" + jmsMessage + ')');
    try {
      Xid xid = null;
      synchronized (lock) {
        try {
          if (isXA) {
            xid = new XidImpl(new byte[0], 1, (agentId.toString() + System.currentTimeMillis()).getBytes());
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: xid=" + xid);

            try {
              consumerRes.start(xid, XAResource.TMNOFLAGS);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.WARN))
                logger.log(BasicLevel.WARN, "JMSBridgeModule.onMessage: XA can't start resource : " + consumerRes, e);
            }
          }
          org.objectweb.joram.client.jms.Message clientMessage = 
            org.objectweb.joram.client.jms.Message.convertJMSMessage(jmsMessage);
          org.objectweb.joram.client.jms.Message.prepareJMSMessage(clientMessage);
          Message momMessage = clientMessage.getMomMsg();
          if (isXA) {
            try {
              consumerRes.end(xid, XAResource.TMSUCCESS);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: XA end " + consumerRes);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: XA resource end(...) failed: " + consumerRes, e);
              throw new JMSException("onMessage: XA resource end(...) failed: " + consumerRes + " :: " + e.getMessage());
            }
            try {
              int ret = consumerRes.prepare(xid);
              if (ret == XAResource.XA_OK)
                consumerRes.commit(xid, false);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: XA commit " + consumerRes);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: XA resource rollback(" + xid + ")", e);
              try {
                consumerRes.rollback(xid);
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: XA rollback " + consumerRes);
              } catch (XAException e1) { }
              throw new JMSException("onMessage: XA resource rollback(" + xid + ") failed: " + 
                  consumerRes + " :: " + e.getMessage());
            }

          } else {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: commit.");
            consumerSession.commit();
          }
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: send JMSBridgeDeliveryNot.");
          Channel.sendTo(agentId, new JMSBridgeDeliveryNot(momMessage));

        } catch (MessageFormatException conversionExc) {
          // Conversion error: denying the message.
          if (isXA) {
            try {
              consumerRes.rollback(xid);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: XA rollback " + consumerRes);
            } catch (XAException e1) { }
          } else {
            consumerSession.rollback();
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "JMSBridgeModule.onMessage: rollback.");
          }
        }
      }
    } catch (JMSException exc) {
      // Commit or rollback failed: nothing to do.
      if (logger.isLoggable(BasicLevel.WARN))
        logger.log(BasicLevel.WARN, "JMSBridgeModule.onMessage: ", exc);
    }
  }

  /**
   * Opens a XA connection with the foreign JMS server and creates the
   * XA JMS resources for interacting with the foreign JMS destination. 
   *
   * @exception JMSException  If the needed JMS resources could not be created.
   */
  protected void doXAConnect() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doXAConnect()");
    
    if (userName != null && password != null) {
      producerCnx = ((XAConnectionFactory) cnxFact).createXAConnection(userName, password);
      consumerCnx = ((XAConnectionFactory) cnxFact).createXAConnection(userName, password);
    } else {
      producerCnx = ((XAConnectionFactory) cnxFact).createXAConnection();
      consumerCnx = ((XAConnectionFactory) cnxFact).createXAConnection();
    }
    producerCnx.setExceptionListener(this);
    consumerCnx.setExceptionListener(this);

    if (clientID != null) {
      producerCnx.setClientID(clientID);
      consumerCnx.setClientID(clientID);
    }

    producerCnx.start();
    consumerCnx.start();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doXAConnect: cnx=" + producerCnx + ", consumerCnx=" + consumerCnx);

    producerSession = ((XAConnection) producerCnx).createXASession();
    producer = producerSession.createProducer(dest);
    consumerSession = ((XAConnection) consumerCnx).createXASession();
    
    producerRes = ((XASession) producerSession).getXAResource();
    consumerRes = ((XASession) consumerSession).getXAResource();
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doXAConnect: producerRes=" + producerRes + ", consumerRes=" + consumerRes);
    
    // Recover if needed.
    new XARecoverDaemon(producerRes).start();
    new XARecoverDaemon(consumerRes).start();
  }

  /**
   * Opens a connection with the foreign JMS server and creates the
   * JMS resources for interacting with the foreign JMS destination. 
   *
   * @exception JMSException  If the needed JMS resources could not be created.
   */
  protected void doConnect() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doConnect()");
    
    if (userName != null && password != null) {
      producerCnx = cnxFact.createConnection(userName, password);
      consumerCnx = cnxFact.createConnection(userName, password);
    } else {
      producerCnx = cnxFact.createConnection();
      consumerCnx = cnxFact.createConnection();
    }
    producerCnx.setExceptionListener(this);
    consumerCnx.setExceptionListener(this);

    if (clientID != null) {
      producerCnx.setClientID(clientID);
      consumerCnx.setClientID(clientID);
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "doConnect: cnx=" + producerCnx + ", consumerCnx=" + consumerCnx);
    
    producerSession = producerCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = producerSession.createProducer(dest);

    consumerSession = consumerCnx.createSession(true, 0);
  }
  
  /**
   * Sets the JMS consumer on the foreign destination. 
   *
   * @exception JMSException  If the JMS consumer could not be created.
   */
  protected void setConsumer() throws JMSException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "setConsumer()");
    
    if (consumer != null)
      return;

    try {
      if (dest instanceof Queue)
        consumer = consumerSession.createConsumer(dest, selector);
      else
        consumer = consumerSession.createDurableSubscriber((Topic) dest,
                                                           agentId.toString(),
                                                           selector,
                                                           false);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "setConsumer: consumer=" + consumer);
      
    } catch (JMSException exc) {
      throw exc;
    } catch (Exception exc) {
      throw new JMSException("JMS resources do not allow to create consumer: " + exc);
    }
  }

  /**
   * Unsets the JMS consumer. 
   */
  protected void unsetConsumer() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "unsetConsumer()");
    try {
      if (dest instanceof Topic)
        consumerSession.unsubscribe(agentId.toString());

      consumer.close();
    }
    catch (Exception exc) {}

    consumer = null;
  }

  /**
   * Acknowledges a message successfully delivered to the foreign JMS server.
   */
  protected void acknowledge(Message message) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "acknowledge(" + message + ')');
    Channel.sendTo(agentId, new JMSBridgeAckNot(message.id));
  }

  /** 
   * The <code>StartupDaemon</code> thread is responsible for retrieving
   * the needed JMS administered objects from the JNDI server.
   */
  protected class StartupDaemon extends Daemon {
    /** Constructs a <code>StartupDaemon</code> thread. */
    protected StartupDaemon() {
      super(agentId.toString() + ":StartupDaemon", logger);
      setDaemon(false);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "StartupDaemon<init> " + agentId);
    }

    
    protected Object retrieveJndiObject(String jndiName) throws Exception {

      Context jndiCtx = null;
      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();

      try {
        jndiCtx = getInitialContext();
        return jndiCtx.lookup(jndiName);

      } catch (Exception exc) {
        throw exc;
      } finally {
        // Closing the JNDI context.
        if (jndiCtx != null) {
          jndiCtx.close();
        }
        Thread.currentThread().setContextClassLoader(oldClassLoader);
      }
    }

    protected Context getInitialContext() throws IOException, NamingException {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "getInitialContext() - Load jndi.properties file");
      }
      Context jndiCtx;
      Properties props = new Properties();
      InputStream in = Class.class.getResourceAsStream("/jndi.properties");
      
      if (in == null) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "jndi.properties not found.");
        }
      } else {
        props.load(in);
      }

      // Override jndi.properties with properties given at initialization if present
      if (jndiFactory != null) {
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY, jndiFactory);
      }
      if (jndiUrl != null) {
        props.setProperty(Context.PROVIDER_URL, jndiUrl);
      }

      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      jndiCtx = new InitialContext(props);
      return jndiCtx;
    }
    
    /** The daemon's loop. */
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "run()");

      try {
        canStop = true;

        // Administered objects still to be retrieved: getting them from
        // JNDI.
        if (cnxFact == null || dest == null) {

          cnxFact = (ConnectionFactory) retrieveJndiObject(cnxFactName);
          dest = (Destination) retrieveJndiObject(destName);
          
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "run: factory=" + cnxFact + ", destination=" + dest);
          
          if (dest instanceof Topic) {
            automaticRequest = false;
          }
        }
        try {
          if (isXA) {
            doXAConnect();
          } else {
            doConnect();
          }
          
          // start consumer daemon
          consumerDaemon.start();
        } catch (AbstractMethodError exc) {
          usable = false;
          notUsableMessage = "Retrieved administered objects types not "
                           + "compatible with the 'unified' communication "
                           + " mode: " + exc;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        } catch (ClassCastException exc) {
          usable = false;
          notUsableMessage = "Retrieved administered objects types not "
                           + "compatible with the chosen communication mode: "
                           + exc;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        } catch (JMSSecurityException exc) {
          usable = false;
          notUsableMessage = "Provided user identification does not allow "
                           + "to connect to the foreign JMS server: "
                           + exc;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        } catch (JMSException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Exception:: ", exc);
          reconnectionDaemon.reconnect();
        } catch (Throwable exc) {
          usable = false;
          notUsableMessage = "" + exc;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
        }
      } catch (javax.naming.NameNotFoundException exc) {
        usable = false;
        if (cnxFact == null)
          notUsableMessage = "Could not retrieve ConnectionFactory ["
                             + cnxFactName
                             + "] from JNDI: " + exc;
        else if (dest == null)
          notUsableMessage = "Could not retrieve Destination ["
                             + destName
                             + "] from JNDI: " + exc;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
      } catch (javax.naming.NamingException exc) {
        usable = false;
        notUsableMessage = "Could not access JNDI: " + exc;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
      } catch (ClassCastException exc) {
        usable = false;
        notUsableMessage = "Error while retrieving administered objects "
                           + "through JNDI possibly because of missing "
                           + "foreign JMS client libraries in classpath: "
                           + exc;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
      } catch (Exception exc) {
        usable = false;
        notUsableMessage = "Error while retrieving administered objects "
                           + "through JNDI: " 
                           + exc;
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "Exception:: notUsableMessage=" + notUsableMessage, exc);
      } finally {
        finish();
      }
    }

    /** Shuts the daemon down. */
    public void shutdown()
    {}

    /** Releases the daemon's resources. */
    public void close()
    {}
  }

  /** 
   * The <code>ReconnectionDaemon</code> thread is responsible for reconnecting
   * the bridge module with the foreign JMS server in case of disconnection.
   */
  protected class ReconnectionDaemon extends Daemon {
//    /** Number of reconnection trials of the first step. */
//    private int attempts1 = 30;
    /** Retry interval (in milliseconds) of the first step. */
    private long interval1 = 1000L;
//    /** Number of reconnection trials of the second step. */
//    private int attempts2 = 55;
    /** Retry interval (in milliseconds) of the second step. */
    private long interval2 = 5000L;
    /** Retry interval (in milliseconds) of the third step. */
    private long interval3 = 60000L;

    /** Constructs a <code>ReconnectionDaemon</code> thread. */
    protected ReconnectionDaemon() {
      super(agentId.toString() + ":ReconnectionDaemon", logger);
      setDaemon(false);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ReconnectionDaemon<init> " + agentId);
    }

    /** Notifies the daemon to start reconnecting. */
    protected void reconnect() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "reconnect() running=" + running);

      if (running)
        return;

      consumer = null;
      start();
    } 

    /** The daemon's loop. */
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "run()");
      
      int attempts = 0;
      long interval;

      try {
        while (running) {
          canStop = true; 

          attempts++;

          if (attempts <= 30)
            interval = interval1;
          else if (attempts <= 55)         
            interval = interval2;
          else
            interval = interval3;

          try {
            Thread.sleep(interval);
            if (isXA) {
              doXAConnect();
            } else {
              doConnect();
            }

            // Setting the listener, if any.
            if (listener)
              setMessageListener();
            // Starting the consumer daemon:
            consumerDaemon.start();
            // Sending the pending messages, if any:
            while (! qout.isEmpty())
              send((Message) qout.remove(0));
          } catch (Exception exc) {
            continue;
          }
          canStop = false;
          break;
        }
      }
      finally {
        finish();
      }
    }

    /** Shuts the daemon down. */
    public void shutdown()
    {}

    /** Releases the daemon's resources. */
    public void close()
    {}
  } 

  /** 
   * The <code>ConsumerDaemon</code> thread allows to call
   * <code>MessageConsumer.receive()</code> for requesting a foreign JMS
   * message without blocking the JORAM server.
   */
  protected class ConsumerDaemon extends Daemon {
    /** Counter of pending "receive" requests. */
    private int requests = 0;

    /** object for synchronization */
    private Object consumerLock = new Object();
    /** true if new "receive" request */
    boolean receiveRequest = false;

    /** Constructs a <code>ReceiverDaemon</code> thread. */
    protected ConsumerDaemon() {
      super(agentId.toString() + ":ConsumerDaemon", logger);
      setDaemon(false);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "ConsumerDaemon<init> " + agentId);
    }

    /** Notifies the daemon of a new "receive" request. */
    protected void receive() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
            "receive() automaticRequest = " + automaticRequest +
            ", receiveRequest = " + receiveRequest);
      if (!automaticRequest) {
        synchronized (consumerLock) {
          requests++;
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "receive(): notify");
          consumerLock.notify();
          receiveRequest = true;
        }
      }
    }

    /**
     * @see fr.dyade.aaa.common.Daemon#start()
     */
    public void start() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
            "start() running =  " + running +
            ", automaticRequest = " + automaticRequest);
      
      if (running)
        return;
      
      super.start();
    }
    
    /** The daemon's loop. */
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "run()");

      try {
        setConsumer();
        consumerCnx.start();
        while (running) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,
                       "run: receiveRequest=" + receiveRequest + ", automaticRequest=" + automaticRequest);
          
          synchronized (consumerLock) {
            if (automaticRequest || receiveRequest) {
              process(); 
            } else {
              try {
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "run(): wait");
                canStop = true;
                consumerLock.wait();
                canStop = false;
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "run(): after wait");
                if (running)
                  process();
              } catch (InterruptedException e2) {
                break;
              }
            }
            receiveRequest = false;
          }
        }
      } catch (JMSException exc) {
        // Connection loss? Try to reconnect?
        onException(exc);
      } finally {
        finish();
      }
    }

    private void process() throws JMSException {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "process()");

      Message momMessage;
      JMSBridgeDeliveryNot notif;

      canStop = true; 
      Xid xid = null;
      synchronized (lock) {
        try {
          if (isXA) {
            xid = new XidImpl(new byte[0], 1, (agentId.toString() + System.currentTimeMillis()).getBytes());
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "process: xid=" + xid);

            try {
              consumerRes.start(xid, XAResource.TMNOFLAGS);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.WARN))
                logger.log(BasicLevel.WARN, 
                           "Exception:: XA can't start resource : " + consumerRes + ", xid = " + xid, e);
            }
          }
          org.objectweb.joram.client.jms.Message clientMessage = 
            org.objectweb.joram.client.jms.Message.convertJMSMessage(consumer.receive());

          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "process: clientMessage=" + clientMessage);

          momMessage = clientMessage.getMomMsg();
          if (isXA) {
            try {
              consumerRes.end(xid, XAResource.TMSUCCESS);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "process: XA end " + consumerRes);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, 
                    "Exception:: XA resource end(...) failed: " + consumerRes + ", xid = " + xid, e);
              throw new JMSException("XA resource end(...) failed: " + consumerRes + " :: " + e.getMessage());
            }
            try {
              int ret = consumerRes.prepare(xid);
              if (ret == XAResource.XA_OK)
                consumerRes.commit(xid, false);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "process: XA commit " + consumerRes);
            } catch (XAException e) {
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "Exception:: XA resource rollback(" + xid + ")", e);
              try {
                consumerRes.rollback(xid);
                if (logger.isLoggable(BasicLevel.DEBUG))
                  logger.log(BasicLevel.DEBUG, "process: XA rollback " + consumerRes);
              } catch (XAException e1) { }
              throw new JMSException("XA resource rollback(" + xid + ") failed: " +  consumerRes + " :: " + e.getMessage());
            }
          } else {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "process: session commit");
            consumerSession.commit();
          }
        } catch (MessageFormatException messageExc) {
          // Conversion error: denying the message.
          if (isXA) {
            try {
              consumerRes.rollback(xid);
              if (logger.isLoggable(BasicLevel.DEBUG))
                logger.log(BasicLevel.DEBUG, "run: XA rollback " + consumerRes);
            } catch (XAException e1) { }
          } else {
            consumerSession.rollback();
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "run: rollback " + consumerSession);
          }
          return;
        }
      }
      // Processing the delivery.
      canStop = false;
      notif = new JMSBridgeDeliveryNot(momMessage);
      Channel.sendTo(agentId, notif);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "run: sendTo momMessage=" + momMessage);
      if (!automaticRequest)
        requests--;
    }
    
    /** Shuts the daemon down. */
    public void shutdown() {
      consumerLock.notify();
    }

    /** Releases the daemon's resources. */
    public void close() {
    }
  }

  protected class XARecoverDaemon extends Daemon {
    private XAResource resource = null;
    
    /** Constructs a <code>XARecoverDaemon</code> thread. */
    protected XARecoverDaemon(XAResource resource) {
      super(agentId.toString() + ":XARecoverDaemon", logger);
      this.resource = resource;
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "XARecoverDaemon<init> " + agentId);
    }

    /** Releases the daemon's resources. */
    protected void close() {
    }

    /** Shuts the daemon down. */
    protected void shutdown() {
    }

    /** The daemon's loop. */
    public void run() {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "run()");

      synchronized (lock) {
        Xid xid = new XidImpl(new byte[0], 1, (agentId.toString() + System.currentTimeMillis()).getBytes());
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "run: xid = " + xid);

        try {
          resource.start(xid, XAResource.TMNOFLAGS);
        } catch (XAException exc) {
          if(logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "Exception:: XA can't start resource : " + resource, exc);
        }

        try {
          Xid[] xids = resource.recover(XAResource.TMNOFLAGS);
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "run: XA xid.length=" + xids.length);
          // if needed recover this resource, and commit.
          for (int i = 0; i < xids.length; i++) {
            if (logger.isLoggable(BasicLevel.INFO))
              logger.log(BasicLevel.INFO, "XARecoverDaemon : commit this " + xids[i].getGlobalTransactionId());
            resource.commit(xids[i], false);
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "run: XA commit xid=" + xids[i]);
          }

          // ended the recover.
          resource.end(xid, XAResource.TMSUCCESS);
        } catch (XAException e) {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Exception:: run", e);
        }
      }
    }
  }

  /** Deserializes a <code>BridgeUnifiedModule</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws java.io.IOException, ClassNotFoundException {
    in.defaultReadObject();
    qout = new Vector();
  }
}
