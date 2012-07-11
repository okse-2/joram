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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.mom.dest.DistributionHandler;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * Distribution handler for the JMS distribution bridge.
 */
public class JMSDistribution implements DistributionHandler {

  private static final Logger logger = Debug.getLogger(JMSDistribution.class.getName());

  private static final String DESTINATION_NAME_PROP = "jms.DestinationName";

  private static final String UPDATE_PERIOD_PROP = "jms.ConnectionUpdatePeriod";

  private static final String ROUTING_PROP = "jms.Routing";

  // LRU (Least Recently Used) Map.
  private LinkedHashMap<String, SessionAndProducer> sessions = new LinkedHashMap<String, SessionAndProducer>(
      16, 0.75f, true);

  private List<String> connectionNames = null;

  /** Destination JNDI name. */
  private String destName;

  private long lastUpdate = 0;
  
  private long updatePeriod = 5000L;

  public void init(Properties properties, boolean firstTime) {
    destName = properties.getProperty(DESTINATION_NAME_PROP);
    if (destName == null) {
      throw new IllegalArgumentException("Missing Destination JNDI name.");
    }
    try {
      if (properties.containsKey(UPDATE_PERIOD_PROP)) {
        updatePeriod = Long.parseLong(properties.getProperty(UPDATE_PERIOD_PROP));
      }
    } catch (NumberFormatException nfe) {
      logger.log(BasicLevel.ERROR, "Property " + UPDATE_PERIOD_PROP
          + "could not be parsed properly, use default value.", nfe);
    }
    if (properties.containsKey(ROUTING_PROP)) {
      connectionNames = JMSConnectionService.convertToList(properties.getProperty(ROUTING_PROP));
    }
  }

  public void distribute(Message message) throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "JMSDistribution.distribute(" + message + ')');
  	}
  	
    List<String> connectionNames = this.connectionNames;
    if (message.properties != null) {
      Object customRouting = message.properties.get(ROUTING_PROP);
      if (customRouting != null && customRouting instanceof String) {
        connectionNames = JMSConnectionService.convertToList((String) customRouting);
      }
    }
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "JMSDistribution.distribute: connectionNames = " + connectionNames);
    }
      
    // Update sessions if necessary
    long now = System.currentTimeMillis();
    if (now - lastUpdate > updatePeriod) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Updating sessions.");
      }
      List<JMSModule> connections = JMSConnectionService.getInstance().getConnections();
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "JMSDistribution.distribute: connections = " + connections);
      }
            
      for (final JMSModule connection : connections) {
        SessionAndProducer sap = sessions.get(connection.getCnxFactName());
        if (sap != null) {
          // Verify that the connection still valid
          if (sap.connection != connection.getCnx())  {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG,
                         "JMSDistribution.distribute: remove outdated connection " + connection.getCnxFactName());
            sessions.remove(connection.getCnxFactName());
            sap = null;
          }
        }
        
        if (sap == null) { // !sessions.containsKey(connection.getCnxFactName()))
          if (logger.isLoggable(BasicLevel.DEBUG))
            logger.log(BasicLevel.DEBUG,
                       connection.getCnxFactName() + ": New connection factory available for distribution.");
          try {
            Session session = connection.getCnx().createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = (Destination) connection.retrieveJndiObject(destName);
            MessageProducer producer = session.createProducer(dest);
            sessions.put(connection.getCnxFactName(), new SessionAndProducer(connection.getCnx(), session, producer));
          } catch (Exception exc) {
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "Connection is not usable.", exc);
            }
          }
        }
      }
      lastUpdate = now;
    }

    // Send the message
    Iterator<Map.Entry<String, SessionAndProducer>> iter = sessions.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, SessionAndProducer> entry = iter.next();
      try {
        SessionAndProducer session = entry.getValue();
        String cnxName = entry.getKey();
        if (connectionNames != null && !connectionNames.contains(cnxName)) {
          continue;
        }
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Sending message using " + cnxName);
        }
        if (session.isJoramSession()) {
        	// convert a Joram message because this message is modified on session send.
        	// And if we have an exception we must keep the original message.
        	session.producer.send(org.objectweb.joram.client.jms.Message.convertJMSMessage(org.objectweb.joram.client.jms.Message.wrapMomMessage(null, message)));
        } else
        	session.producer.send(org.objectweb.joram.client.jms.Message.wrapMomMessage(null, message));
        sessions.get(cnxName); // Access the used connection to update the LRU map
        return;
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Session is not usable, remove from table.", exc);
        }
        // TODO (AF): We should indicate that the corresponding connection is no longer
        // available calling JMSModule.onException().
        iter.remove();
      }
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Message could not be sent, no usable connection/session found.");

    throw new Exception("Message could not be sent, no usable connection/session found.");
  }

  public void close() {
    for (SessionAndProducer session : sessions.values()) {
      try {
        session.producer.close();
        session.session.close();
      } catch (JMSException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Error while stopping JmsDistribution.", exc);
        }
      }
    }
    sessions.clear();
  }
  
  private class SessionAndProducer {
    Connection connection;
    Session session;
    MessageProducer producer;

    public SessionAndProducer(Connection connection, Session session, MessageProducer producer) {
      super();
      this.connection = connection;
      this.session = session;
      this.producer = producer;
    }
    
    public boolean isJoramSession() {
    	return (session instanceof org.objectweb.joram.client.jms.Session);
    }
  }

}
