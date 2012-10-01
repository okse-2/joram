/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest.amqp;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.objectweb.joram.mom.dest.DistributionHandler;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;

import fr.dyade.aaa.common.Debug;

/**
 * Distribution handler for the AMQP distribution bridge.
 */
public class AmqpDistribution implements DistributionHandler {

  private static final Logger logger = Debug.getLogger(AmqpDistribution.class.getName());

  private static final String QUEUE_NAME_PROP = "amqp.QueueName";

  private static final String UPDATE_PERIOD_PROP = "amqp.ConnectionUpdatePeriod";

  private static final String ROUTING_PROP = "amqp.Routing";

  // LRU (Least Recently Used) Map.
  private LinkedHashMap<String, Channel> channels = new LinkedHashMap<String, Channel>(16, 0.75f, true);

  private List<String> connectionNames = null;

  private String amqpQueue = null;

  private long lastUpdate = 0;
  
  private long updatePeriod = 5000L;

  public void init(Properties properties) {
    amqpQueue = properties.getProperty(QUEUE_NAME_PROP);
    if (amqpQueue == null) {
      logger.log(BasicLevel.ERROR, "The amqp queue name property " + QUEUE_NAME_PROP + " must be specified.");
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
      connectionNames = AmqpConnectionService.convertToList(properties.getProperty(ROUTING_PROP));
    }
  }

  public void distribute(Message message) throws Exception {

    List<String> connectionNames = this.connectionNames;

    // Convert message properties
    AMQP.BasicProperties props = new AMQP.BasicProperties();
    if (message.persistent) {
      props.setDeliveryMode(Integer.valueOf(2));
    } else {
      props.setDeliveryMode(Integer.valueOf(1));
    }
    props.setCorrelationId(message.correlationId);
    props.setPriority(Integer.valueOf(message.priority));
    props.setTimestamp(new Date(message.timestamp));
    props.setMessageId(message.id);
    props.setType(String.valueOf(message.type));
    props.setExpiration(String.valueOf(message.expiration));

    if (message.properties != null) {
      Map<String, Object> headers = new HashMap<String, Object>();
      message.properties.copyInto(headers);
      props.setHeaders(headers);

      Object customRouting = message.properties.get(ROUTING_PROP);
      if (customRouting != null && customRouting instanceof String) {
        connectionNames = AmqpConnectionService.convertToList((String) customRouting);
      }
    }

    // Update channels if necessary
    long now = System.currentTimeMillis();
    if (now - lastUpdate > updatePeriod) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Updating channels.");
      }
      List<LiveServerConnection> connections = AmqpConnectionService.getInstance().getConnections();
      for (LiveServerConnection connection : connections) {
        if (!channels.containsKey(connection.getName())) {
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, connection.getName() + ": New channel available for distribution.");
          }
          try {
            Channel chan = connection.getConnection().createChannel();
            chan.queueDeclarePassive(amqpQueue);
            channels.put(connection.getName(), chan);
          } catch (IOException exc) {
            if (logger.isLoggable(BasicLevel.DEBUG)) {
              logger.log(BasicLevel.DEBUG, "Channel is not usable.", exc);
            }
          }
        }
      }
      lastUpdate = now;
    }

    // Send the message
    Iterator<Map.Entry<String, Channel>> iter = channels.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, Channel> entry = iter.next();
      try {
        Channel chan = entry.getValue();
        String cnxName = entry.getKey();
        if (!chan.isOpen()) {
          iter.remove();
          continue;
        }
        if (connectionNames != null && !connectionNames.contains(cnxName)) {
          continue;
        }
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Sending message on " + cnxName);
        }
        chan.basicPublish("", amqpQueue, props, message.body);
        channels.get(cnxName); // Access the used connection to update the LRU map
        return;
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Channel is not usable, remove from table.", exc);
        }
        iter.remove();
      } catch (AlreadyClosedException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Channel is not usable, remove from table.", exc);
        }
        iter.remove();
      }
    }

    throw new Exception("Message could not be sent, no usable channel found.");
  }

  public void close() {
    for (Channel channel : channels.values()) {
      try {
        channel.close();
      } catch (IOException exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Error while stopping AmqpDistribution.", exc);
        }
      }
    }
    channels.clear();
  }

}
