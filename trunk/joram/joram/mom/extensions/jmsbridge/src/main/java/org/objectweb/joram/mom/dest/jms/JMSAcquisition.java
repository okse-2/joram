/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.joram.mom.dest.jms;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.objectweb.joram.mom.dest.AcquisitionDaemon;
import org.objectweb.joram.mom.dest.ReliableTransmitter;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

/**
 * Acquisition daemon for the JMS acquisition bridge.
 */
public class JMSAcquisition implements AcquisitionDaemon {

  private static final Logger logger = Debug.getLogger(JMSAcquisition.class.getName());

  private static final String DESTINATION_NAME_PROP = "jms.DestinationName";

  private static final String UPDATE_PERIOD_PROP = "jms.ConnectionUpdatePeriod";

  private static final String ROUTING_PROP = "jms.Routing";
  
  private static final String DURABLE_SUBSCRIPTION_PROP = "jms.DurableSubscriptionName";
  
  private static final String SELECTOR_PROP = "jms.Selector";

  private static ConnectionUpdater connectionUpdater;

  private ReliableTransmitter transmitter;

  // Use a vector as it is used by 2 different threads
  private Map<String, Session> sessions = new Hashtable<String, Session>();

  /** If routing prop has been set, it defines a list of connection to use. */
  private List<String> connectionNames = null;

  private volatile boolean closing = false;

  /** The JNDI name of the foreign JMS destination. */
  private String destName;

  /** Foreign JMS destination object. */
  private Destination dest = null;

  /** Selector for filtering messages. */
  protected String selector;
  
  private String durableSubscriptionName = null;

  public void start(Properties properties, ReliableTransmitter transmitter) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Start JMSAcquisition.");
    
    this.transmitter = transmitter;

    destName = properties.getProperty(DESTINATION_NAME_PROP);
    if (destName == null) {
      throw new IllegalArgumentException("Missing Destination JNDI name.");
    }

    long updatePeriod = 5000L;
    try {
      if (properties.containsKey(UPDATE_PERIOD_PROP)) {
        updatePeriod = Long.parseLong(properties.getProperty(UPDATE_PERIOD_PROP));
      }
    } catch (NumberFormatException nfe) {
      logger.log(BasicLevel.ERROR, "Property " + UPDATE_PERIOD_PROP
          + "could not be parsed properly, use default value.", nfe);
    }

    connectionNames = null;
    if (properties.containsKey(ROUTING_PROP)) {
      connectionNames = JMSConnectionService.convertToList(properties.getProperty(ROUTING_PROP));
    }

    durableSubscriptionName = null;
    if (properties.containsKey(DURABLE_SUBSCRIPTION_PROP)) {
      durableSubscriptionName = properties.getProperty(DURABLE_SUBSCRIPTION_PROP);
    }

    selector = null;
    if (properties.containsKey(SELECTOR_PROP)) {
      selector = properties.getProperty(SELECTOR_PROP);
    }

    if (connectionUpdater == null) {
      connectionUpdater = new ConnectionUpdater(updatePeriod);
    }
    connectionUpdater.addUpdateListener(this);
  }

  public void stop() {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Stop JMSAcquisition.");

    connectionUpdater.removeUpdateListener(this);
    
    closing = true;
    synchronized (sessions) {
      for (Session session : sessions.values()) {
        try {
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "Close JMS session: " + session);

          session.close();
          
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG, "JMS session closed: " + session);
        } catch (JMSException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "Error while stopping JmsAcquisition.", exc);
          }
        }
      }
      
      sessions.clear();
      closing = false;
    }
  }

  /**
   * Create a new JMS consumer for each connection available.
   */
  public synchronized void updateConnections(List<JMSModule> connections) {
    for (JMSModule connection : connections) {
      if (!sessions.containsKey(connection.getCnxFactName())) {
        if (connectionNames == null || connectionNames.contains(connection.getCnxFactName())) {
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG,
                       "Creating a new consumer for connection: " + connection.getCnxFactName(), new Exception());
          }
          try {
            dest = (Destination) connection.retrieveJndiObject(destName);
  
            Session session = connection.getCnx().createSession(true, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer;
            if (dest instanceof Queue) {
              consumer = session.createConsumer(dest, selector);
            } else {
              if (durableSubscriptionName != null) {
                consumer = session.createDurableSubscriber((Topic) dest, durableSubscriptionName, selector, false);
              } else {
                consumer = session.createConsumer(dest, selector);
              }
            }
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "setConsumer: consumer=" + consumer);
            }
  
            JmsListener listener = new JmsListener(session, connection.getCnxFactName());
            consumer.setMessageListener(listener);
            connection.getCnx().start();
            connection.addExceptionListener(listener);
            sessions.put(connection.getCnxFactName(), session);
          } catch (Exception e) {
            logger.log(BasicLevel.ERROR,
                "Error while starting consumer on connection: " + connection.getCnxFactName(), e);
          }
        }
      }
    }
  }

  class JmsListener implements MessageListener {

    private String name;

    private Session session;

    public JmsListener(Session session, String name) {
      this.session = session;
      this.name = name;
    }

    /**
     * Implements the {@link MessageListener} interface for processing the
     * asynchronous deliveries coming from the foreign JMS server.
     */
    public void onMessage(javax.jms.Message jmsMessage) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, name + ".onMessage(" + jmsMessage + ')');

      try {
        org.objectweb.joram.client.jms.Message clientMessage = null;
        try {
          clientMessage = org.objectweb.joram.client.jms.Message.convertJMSMessage(jmsMessage);
        } catch (JMSException conversionExc) {
          // Conversion error: denying the message.
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, name + ".onMessage: rollback, can not convert message.", conversionExc);

          session.rollback();
          return;
        }
        transmitter.transmit(clientMessage.getMomMsg(), jmsMessage.getJMSMessageID());

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, name + ".onMessage: Try to commit.");

        session.commit();
      } catch (JMSException exc) {
        // Commit or rollback failed: nothing to do.
        logger.log(BasicLevel.ERROR, name + ".onMessage(" + jmsMessage + ')', exc);
      } catch (Throwable t) {
        logger.log(BasicLevel.ERROR, name + ".onMessage(" + jmsMessage + ')', t);
      }
    }

    public void onException(JMSException exception) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, name + ": Consumer error for session " + session);
      }
      if (!closing) {
        sessions.remove(name);
      }
    }

  }

  /**
   * Daemon used to periodically update the pool of connections known by the
   * acquisition destinations.
   */
  private static class ConnectionUpdater extends Daemon {

    private List<JMSAcquisition> listeners = new ArrayList<JMSAcquisition>();

    private long period;

    /** Constructs a <code>ConnectionUpdater</code> thread. */
    protected ConnectionUpdater(long updatePeriod) {
      super("JMS_ConnectionUpdater", logger);
      setDaemon(false);
      period = updatePeriod;
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "ConnectionUpdater<init>");
      }
    }

    /** The daemon's loop. */
    public void run() {
      if (logmon.isLoggable(BasicLevel.DEBUG)) {
        logmon.log(BasicLevel.DEBUG, "run()");
      }

      try {
        boolean firstTime = true;
        while (running) {
          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "update connections in " + period + " ms");
          }

          canStop = true;
          if (firstTime) {
            firstTime = false;
          } else {
            try {
              Thread.sleep(period);
            } catch (InterruptedException exc) {
              if (logmon.isLoggable(BasicLevel.DEBUG)) {
                logmon.log(BasicLevel.DEBUG, "Thread interrupted.");
              }
              continue;
            }
          }
          canStop = false;

          if (logmon.isLoggable(BasicLevel.DEBUG)) {
            logmon.log(BasicLevel.DEBUG, "update connections");
          }

          List<JMSModule> currentConnections = JMSConnectionService.getInstance().getConnections();

          synchronized (listeners) {
            if (listeners.size() == 0) stop();

            for (JMSAcquisition listener : listeners) {
              listener.updateConnections(currentConnections);
            }
          }
        }
      } finally {
        finish();
      }
    }

    /** Shuts the daemon down. */
    public void shutdown() {
      interrupt();
    }

    /** Releases the daemon's resources. */
    public void close() {
    }

    protected void addUpdateListener(JMSAcquisition listener) {
      synchronized (listeners) {
        listeners.add(listener);
        if (listeners.size() == 1) {
          start();
        }
      }

      List<JMSModule> existingConnections = JMSConnectionService.getInstance().getConnections();
      listener.updateConnections(existingConnections);
    }

    protected void removeUpdateListener(JMSAcquisition listener) {
      synchronized (listeners) {
        listeners.remove(listener);
        if (listeners.size() == 0) stop();
      }
    }
  }
}
