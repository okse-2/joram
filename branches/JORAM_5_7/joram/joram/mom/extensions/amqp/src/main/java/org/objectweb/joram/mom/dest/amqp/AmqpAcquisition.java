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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.objectweb.joram.mom.dest.AcquisitionDaemon;
import org.objectweb.joram.mom.dest.ReliableTransmitter;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.impl.LongString;

import fr.dyade.aaa.common.Daemon;
import fr.dyade.aaa.common.Debug;

/**
 * Acquisition daemon for the AMQP acquisition bridge.
 */
public class AmqpAcquisition implements AcquisitionDaemon {

  private static final Logger logger = Debug.getLogger(AmqpAcquisition.class.getName());

  private static final String QUEUE_NAME_PROP = "amqp.QueueName";

  private static final String UPDATE_PERIOD_PROP = "amqp.ConnectionUpdatePeriod";

  private static final String ROUTING_PROP = "amqp.Routing";

  private static ConnectionUpdater connectionUpdater;

  private ReliableTransmitter transmitter;

  // Use a hashtable as it is used by 2 different threads
  private Map<String, Channel> channels = new Hashtable<String, Channel>();

  /** If routing prop has been set, it defines a list of connection to use. */
  private List<String> connectionNames = null;

  /** The name of the foreign AMQP queue. */
  private String amqpQueue = null;

  private volatile boolean closing = false;

  public void start(Properties properties, ReliableTransmitter transmitter) {
    this.transmitter = transmitter;

    amqpQueue = properties.getProperty(QUEUE_NAME_PROP);
    if (amqpQueue == null) {
      logger.log(BasicLevel.ERROR, "The amqp queue name property " + QUEUE_NAME_PROP + " must be specified.");
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
      connectionNames = AmqpConnectionService.convertToList(properties.getProperty(ROUTING_PROP));
    }

    if (connectionUpdater == null) {
      connectionUpdater = new ConnectionUpdater(updatePeriod);
    }
    connectionUpdater.addUpdateListener(this);
  }

  public void stop() {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "Stop AmqpAcquisition.");
    }

    connectionUpdater.removeUpdateListener(this);
    
    closing = true;
    synchronized (channels) {
      for (Channel channel : channels.values()) {
        try {
          channel.close();
        } catch (IOException exc) {
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "Error while stopping AmqpAcquisition.", exc);
          }
        }
      }
      channels.clear();
    }
  }

  /**
   * Create a new AMQP consumer for each connection available.
   */
  public void updateConnections(List<LiveServerConnection> connections) {
    for (LiveServerConnection connection : connections) {
      if (!channels.containsKey(connection.getName())) {
        if (connectionNames == null || connectionNames.contains(connection.getName())) {
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "Creating a new consumer on queue " + amqpQueue + " for connection "
                + connection.getName());
          }
          try {
            Channel chan = connection.getConnection().createChannel();
            chan.queueDeclarePassive(amqpQueue);
            AmqpConsumer consumer = new AmqpConsumer(chan, connection.getName());
            chan.basicConsume(amqpQueue, false, consumer);
            channels.put(connection.getName(), chan);
          } catch (Exception e) {
            logger.log(BasicLevel.ERROR,
                "Error while starting consumer on connection: " + connection.getName(), e);
          }
        }
      }
    }
  }

  private class AmqpConsumer extends DefaultConsumer {

    private String name;

    public AmqpConsumer(Channel channel, String name) {
      super(channel);
      this.name = name;
    }

    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
        throws IOException {
      Message message = new Message();
      message.body = body;
      try {
        message.type = Byte.parseByte(properties.getType());
      } catch (NumberFormatException nfe) {
        if (logger.isLoggable(BasicLevel.WARN)) {
          logger.log(BasicLevel.WARN, "Message Type field could not be parsed.", nfe);
        }
        message.type = Message.BYTES;
      }
      message.correlationId = properties.getCorrelationId();
      Integer deliveryMode = properties.getDeliveryMode();
      if (deliveryMode != null) {
        if (deliveryMode.intValue() == 1) {
          message.persistent = false;
        } else if (deliveryMode.intValue() == 2) {
          message.persistent = true;
        }
      }
      if (properties.getPriority() != null)
        message.priority = properties.getPriority().intValue();
      if (properties.getTimestamp() != null)
        message.timestamp = properties.getTimestamp().getTime();

      try {
        if (properties.getExpiration() != null)
          message.expiration = Long.parseLong(properties.getExpiration());
      } catch (NumberFormatException nfe) {
        if (logger.isLoggable(BasicLevel.WARN)) {
          logger.log(BasicLevel.WARN, "Expiration field could not be parsed.", nfe);
        }
      }

      if (properties.getHeaders() != null) {
        Map<String, Object> props = properties.getHeaders();
        for (Map.Entry<String, Object> prop : props.entrySet()) {
          try {
            if (prop.getValue() instanceof LongString) {
              message.setProperty(prop.getKey(), prop.getValue().toString());
            } else {
              message.setProperty(prop.getKey(), prop.getValue());
            }
          } catch (ClassCastException exc) {
            if (logger.isLoggable(BasicLevel.ERROR)) {
              logger.log(BasicLevel.ERROR, "Property can't be mapped to JMS message property.", exc);
            }
          }
        }
      }
      
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, name + ": New incoming message : " + message);
      }

      transmitter.transmit(message, properties.getMessageId());

      getChannel().basicAck(envelope.getDeliveryTag(), false);
    }

    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, name + ": Consumer error for connection " + getChannel().getConnection());
      }
      if (!closing) {
        channels.remove(name);
      }
    }

  }

  /**
   * Daemon used to periodically update the pool of connections known by the
   * acquisition destinations.
   */
  private static class ConnectionUpdater extends Daemon {

    private List<AmqpAcquisition> listeners = new ArrayList<AmqpAcquisition>();

    private long period;

    /** Constructs a <code>ConnectionUpdater</code> thread. */
    protected ConnectionUpdater(long updatePeriod) {
      super("AMQP_ConnectionUpdater", logger);
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

          List<LiveServerConnection> currentConnections = AmqpConnectionService.getInstance().getConnections();

          synchronized (listeners) {
            if (listeners.size() == 0) {
              stop();
            }
            for (AmqpAcquisition listener : listeners) {
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

    protected void addUpdateListener(AmqpAcquisition listener) {
      synchronized (listeners) {
        listeners.add(listener);
        if (listeners.size() == 1) {
          start();
        }
      }

      List<LiveServerConnection> currentConnections = AmqpConnectionService.getInstance().getConnections();
      listener.updateConnections(currentConnections);

    }

    protected void removeUpdateListener(AmqpAcquisition listener) {
      synchronized (listeners) {
        listeners.remove(listener);
        if (listeners.size() == 0) {
          stop();
        }
      }
    }
  }

}
