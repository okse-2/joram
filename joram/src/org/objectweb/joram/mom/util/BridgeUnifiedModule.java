/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Contributor(s):
 */
package org.objectweb.joram.mom.util;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.util.Daemon;
import org.objectweb.joram.shared.messages.Message;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.jms.*;
import javax.jms.IllegalStateException;


/**
 * The <code>BridgeUnifiedModule</code> class is a bridge module based on the
 * JMS 1.1 unified semantics and classes.
 */
public class BridgeUnifiedModule implements javax.jms.ExceptionListener,
                                            javax.jms.MessageListener,
                                            java.io.Serializable
{
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
  protected transient Connection cnx;
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


  /** Constructs a <code>BridgeUnifiedModule</code> module. */
  public BridgeUnifiedModule()
  {} 


  /**
   * Initializes the module's parameters.
   *
   * @param agentId  Identifier of the agent using the module.
   * @param prop  JMS properties required for establishing the link with the
   *          foreign JMS server.
   *
   * @exception IllegalArgumentException  If the provided properties are
   *              invalid.
   */
  public void init(AgentId agentId, Properties prop) {
    this.agentId = agentId;

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
  }

  /**
   * Launches the connection process to the foreign JMS server.
   *
   * @exception javax.jms.IllegalStateException  If the module can't access
   *              the foreign JMS server.
   * @exception javax.jms.JMSException  If the needed JMS resources can't be
   *              created.
   */
  public void connect() throws JMSException
  {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    listener = false;
    // Creating the module's daemons.
    consumerDaemon = new ConsumerDaemon();
    reconnectionDaemon = new ReconnectionDaemon();

    // Administered objects have not been retrieved: launching the startup
    // daemon.
    if (cnxFact == null || dest == null) {
      StartupDaemon startup = new StartupDaemon();
      startup.start();
    }
    // Administered objects have been retrieved: connecting.
    else {
      try {
        doConnect();
      }
      catch (JMSException exc) {
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
  public void setMessageListener() throws IllegalStateException
  {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    listener = true;
    try {
      setConsumer();
      consumer.setMessageListener(this);
      cnx.start();
    }
    catch (JMSException exc) {}
  } 

  /**
   * Unsets the set message listener on the foreign JMS destination.
   */
  public void unsetMessageListener()
  {
    try {
      cnx.stop();
      consumer.setMessageListener(null);
      unsetConsumer();
    }
    catch (JMSException exc) {}
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
  public Message receiveNoWait() throws IllegalStateException
  {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    Message momMessage = null;
    try {
      setConsumer();
      cnx.start();
      try {
        momMessage = MessageConverterModule.convert(consumer.receiveNoWait());
        consumerSession.commit();
      }
      // Conversion error: denying the message.
      catch (MessageFormatException exc) {
        consumerSession.rollback();
      }
    }
    // Connection start, or session commit/rollback failed:
    // setting the message to null.
    catch (JMSException commitExc) {
      momMessage = null;
    }
    return momMessage;
  }

  /**
   * Asynchronous method requesting a delivery from the foreign
   * JMS destination.
   *
   * @exception javax.jms.IllegalStateException  If the module state does
   *              not allow to request a message.
   */
  public void receive() throws IllegalStateException
  {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

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
              throws JMSException
  {
    if (! usable)
      throw new IllegalStateException(notUsableMessage);

    try {
      producer.send(MessageConverterModule.convert(producerSession, message));
      acknowledge(message);
    }
    catch (javax.jms.MessageFormatException exc) {
      throw exc;
    }
    // Connection failure? Keeping the message for later delivery.
    catch (javax.jms.JMSException exc) {
      qout.add(message);
    }
  }

  /** 
   * Interrupts the daemons and closes the connection.
   */
  public void close()
  {
    try {
      cnx.stop();
    }
    catch (JMSException exc) {}

    unsetMessageListener();

    try {
      consumerDaemon.interrupt();
    }
    catch (Exception exc) {}
    try {
      reconnectionDaemon.interrupt();
    }
    catch (Exception exc) {}

    try {
      cnx.close();
    } 
    catch (JMSException exc) {}
  }

  /**
   * Implements the <code>javax.jms.ExceptionListener</code> interface for
   * catching the failures of the connection to the remote JMS server.
   * <p>
   * Reacts by launching a reconnection process.
   */
  public void onException(JMSException exc)
  {
    reconnectionDaemon.reconnect();
  }

  /**
   * Implements the <code>javax.jms.MessageListener</code> interface for
   * processing the asynchronous deliveries coming from the foreign JMS
   * server.
   */
  public void onMessage(javax.jms.Message jmsMessage)
  {
    try {
      try {
        Message momMessage = MessageConverterModule.convert(jmsMessage);
        consumerSession.commit();
        Channel.sendTo(agentId, new BridgeDeliveryNot(momMessage));
      }
      // Conversion error: denying the message.
      catch (MessageFormatException conversionExc) {
        consumerSession.rollback();
      } 
    }
    // Commit or rollback failed: nothing to do.
    catch (JMSException exc) {}
  }

  /**
   * Opens a connection with the foreign JMS server and creates the
   * JMS resources for interacting with the foreign JMS destination. 
   *
   * @exception JMSException  If the needed JMS resources could not be created.
   */
  protected void doConnect() throws JMSException
  {
    if (userName != null && password != null)
      cnx = cnxFact.createConnection(userName, password);
    else
      cnx = cnxFact.createConnection();
    cnx.setExceptionListener(this);

    if (clientID != null)
      cnx.setClientID(clientID);

    producerSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = producerSession.createProducer(dest);

    consumerSession = cnx.createSession(true, 0);
  }

  /**
   * Sets the JMS consumer on the foreign destination. 
   *
   * @exception JMSException  If the JMS consumer could not be created.
   */
  protected void setConsumer() throws JMSException
  {
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
    }
    catch (JMSException exc) {
      throw exc;
    }
    catch (Exception exc) {
      throw new JMSException("JMS resources do not allow to create consumer: "
                             + exc);
    }
  }

  /**
   * Unsets the JMS consumer. 
   */
  protected void unsetConsumer()
  {
    try {
      if (dest instanceof Topic)
        consumerSession.unsubscribe(agentId.toString());

      consumer.close();
    }
    catch (Exception exc) {}

    consumer = null;
  }

  /**
   * Acknowledges a message successfuly delivered to the foreign JMS server.
   */
  protected void acknowledge(Message message)
  {
    Channel.sendTo(agentId, new BridgeAckNot(message.getIdentifier()));
  }


  /** 
   * The <code>StartupDaemon</code> thread is responsible for retrieving
   * the needed JMS administered objects from the JNDI server.
   */
  protected class StartupDaemon extends Daemon
  {
    /** Constructs a <code>StartupDaemon</code> thread. */
    protected StartupDaemon()
    {
      super(agentId.toString() + ":StartupDaemon");
      setDaemon(false);
    }

    /** The daemon's loop. */
    public void run()
    {
      javax.naming.Context jndiCtx = null;
      try {
        canStop = true;

        // Administered objects still to be retrieved: getting them from
        // JNDI.
        if (cnxFact == null || dest == null) {
          if (jndiFactory == null || jndiUrl == null)
            jndiCtx = new javax.naming.InitialContext();
          else {
            java.util.Hashtable env = new java.util.Hashtable();
            env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, jndiFactory);
            env.put(javax.naming.Context.PROVIDER_URL, jndiUrl);
            jndiCtx = new javax.naming.InitialContext(env);
          }
          cnxFact = (ConnectionFactory) jndiCtx.lookup(cnxFactName);
          dest = (Destination) jndiCtx.lookup(destName);
        }
        try {
          doConnect();
        }
        catch (AbstractMethodError exc) {
          usable = false;
          notUsableMessage = "Retrieved administered objects types not "
                           + "compatible with the 'unified' communication "
                           + " mode: " + exc;
        }
        catch (ClassCastException exc) {
          usable = false;
          notUsableMessage = "Retrieved administered objects types not "
                           + "compatible with the chosen communication mode: "
                           + exc;
        }
        catch (JMSSecurityException exc) {
          usable = false;
          notUsableMessage = "Provided user identification does not allow "
                           + "to connect to the foreign JMS server: "
                           + exc;
        }
        catch (JMSException exc) {
          reconnectionDaemon.reconnect();
        }
        catch (Throwable exc) {
          usable = false;
          notUsableMessage = "" + exc;
        }
      }
      catch (javax.naming.NameNotFoundException exc) {
        usable = false;
        if (cnxFact == null)
          notUsableMessage = "Could not retrieve ConnectionFactory ["
                             + cnxFactName
                             + "] from JNDI: " + exc;
        else if (dest == null)
          notUsableMessage = "Could not retrieve Destination ["
                             + destName
                             + "] from JNDI: " + exc;
      }
      catch (javax.naming.NamingException exc) {
        usable = false;
        notUsableMessage = "Could not access JNDI: " + exc;
      }
      catch (ClassCastException exc) {
        usable = false;
        notUsableMessage = "Error while retrieving administered objects "
                           + "through JNDI possibly because of missing "
                           + "foreign JMS client libraries in classpath: "
                           + exc;
      }
      catch (Exception exc) {
        usable = false;
        notUsableMessage = "Error while retrieving administered objects "
                           + "through JNDI: " 
                           + exc;
      }
      finally {
        // Closing the JNDI context.
        try {
          jndiCtx.close();
        }
        catch (Exception exc) {}

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
  protected class ReconnectionDaemon extends Daemon
  {
    /** Number of reconnection trials of the first step. */
    private int attempts1 = 30;
    /** Retry interval (in milliseconds) of the first step. */
    private long interval1 = 1000L;
    /** Number of reconnection trials of the second step. */
    private int attempts2 = 55;
    /** Retry interval (in milliseconds) of the second step. */
    private long interval2 = 5000L;
    /** Retry interval (in milliseconds) of the third step. */
    private long interval3 = 60000L;

    /** Constructs a <code>ReconnectionDaemon</code> thread. */
    protected ReconnectionDaemon()
    {
      super(agentId.toString() + ":ReconnectionDaemon");
      setDaemon(false);
    }

    /** Notifies the daemon to start reconnecting. */
    protected void reconnect() {
      if (running)
        return;

      consumer = null;
      start();
    } 

    /** The daemon's loop. */
    public void run()
    {
      int attempts = 0;
      long interval;
      Message msg;

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
            doConnect();
            
            // Setting the listener, if any.
            if (listener)
              setMessageListener();
            // Starting the consumer daemon:
            consumerDaemon.start();
            // Sending the pending messages, if any:
            while (! qout.isEmpty())
              send((Message) qout.remove(0));
          }
          catch (Exception exc) {
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
  protected class ConsumerDaemon extends Daemon
  {
    /** Counter of pending "receive" requests. */
    private int requests = 0;


    /** Constructs a <code>ReceiverDaemon</code> thread. */
    protected ConsumerDaemon()
    {
      super(agentId.toString() + ":ConsumerDaemon");
      setDaemon(false);
    }

    /** Notifies the daemon of a new "receive" request. */
    protected synchronized void receive()
    {
      requests++;

      if (running)
        return;

      start();
    }

    /** The daemon's loop. */
    public void run()
    {
      try {
        Message momMessage;
        BridgeDeliveryNot notif;

        setConsumer();
        cnx.start();

        while (requests > 0 && running) {
          canStop = true; 

          // Expecting a message:
          try {
            momMessage = MessageConverterModule.convert(consumer.receive());
            consumerSession.commit();
          }
          // Conversion error: denying the message.
          catch (MessageFormatException messageExc) {
            consumerSession.rollback();
            continue;
          }
          // Processing the delivery.
          canStop = false;
          notif = new BridgeDeliveryNot(momMessage);
          Channel.sendTo(agentId, notif);
          requests--;
        }
      }
      // Connection loss?
      catch (JMSException exc) {}
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


  /** Deserializes a <code>BridgeUnifiedModule</code> instance. */
  private void readObject(java.io.ObjectInputStream in)
               throws java.io.IOException, ClassNotFoundException
  {
    in.defaultReadObject();
    qout = new Vector();
  }
}
