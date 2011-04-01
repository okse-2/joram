/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2011 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link AcquisitionModule} interfaces between the acquisition destinations
 * and the specified {@link AcquisitionHandler}.
 */
public class AcquisitionModule implements ReliableTransmitter {

  public static Logger logger = Debug.getLogger(AcquisitionModule.class.getName());

  /** The property name for the acquisition period. */
  public static final String PERIOD = "acquisition.period";

  /** The property name for the acquisition handler class name. */
  public static final String CLASS_NAME = "acquisition.className";

  /** Persistent property name: tells if produced messages will be persistent. */
  public static final String PERSISTENT_PROPERTY = "persistent";

  /** Expiration property name: tells the life expectancy of produced messages. */
  public static final String EXPIRATION_PROPERTY = "expiration";

  /** Priority property name: tells the JMS priority of produced messages. */
  public static final String PRIORITY_PROPERTY = "priority";

  public static void checkAcquisitionClass(String className) throws Exception {
    if (className == null) {
      throw new Exception("AcquisitionHandler class not defined: use " + CLASS_NAME
          + " property to chose acquisition class.");
    }
    Class clazz = Class.forName(className);
    boolean isDaemon = false;
    boolean isHandler = false;
    while (clazz != null) {
      Class[] interfaces = clazz.getInterfaces();
      for (int i = 0; i < interfaces.length; i++) {
        if (interfaces[i].equals(AcquisitionDaemon.class)) {
          isDaemon = true;
        } else if (interfaces[i].equals(AcquisitionHandler.class)) {
          isHandler = true;
        }
      }
      clazz = clazz.getSuperclass();
    }
    if (isDaemon && isHandler) {
      throw new Exception("Acquisition class " + className
          + " can't implement both AcquisitionHandler and AcquisitionDaemon interfaces.");
    } else if (!isDaemon && !isHandler) {
      throw new Exception("Acquisition class " + className
          + " must implement either AcquisitionHandler or AcquisitionDaemon interface.");
    }
  }

  private static Properties transform(fr.dyade.aaa.common.stream.Properties properties) {
    if (properties == null)
      return null;
    Properties prop = new Properties();
    Enumeration e = properties.keys();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      prop.setProperty(key, properties.get(key).toString());
    }
    return prop;
  }
  
  /** The acquisition logic. */
  protected Object acquisitionHandler;

  /** The priority of produced messages, default is 4. */
  private int priority;

  /** <code>true</code> if the priority property has been set. */
  private boolean isPrioritySet = false;

  /** Tells if the messages produced are persistent. */
  private boolean isPersistent;

  /** <code>true</code> if the persistence property has been set. */
  private boolean isPersistencySet = false;

  /** The duration of produced messages. */
  private long expiration;

  /** <code>true</code> if the expiration property has been set. */
  private boolean isExpirationSet = false;

  /** The acquisition queue or topic using this module. */
  private final Destination destination;

  /** The period before subsequent acquisition if positive. */
  private long period = 0;

  /** The task used to launch a new acquisition. */
  private AcquisitionTask acquisitionTask;

  /**
   * Tells if acquisition is done on-demand using the acquisition task or with a
   * daemon.
   */
  private boolean isDaemon = false;

  /** <code>true</code> if the acquisition daemon is running. */
  private boolean running;

  public AcquisitionModule(Destination destination, String className, Properties properties) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.<init> prop = " + properties);
    }

    this.destination = destination;

    try {
      Class clazz = Class.forName(className);
      acquisitionHandler = clazz.newInstance();

      // Verify that one and only one correct interface is implemented.
      if (acquisitionHandler instanceof AcquisitionDaemon) {
        isDaemon = true;
      }
      setProperties(properties);

    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "AcquisitionModule: can't create acquisition handler.", exc);
    }
  }

  /**
   * Returns true if the messages produced are persistent.
   * 
   * @return true if the messages produced are persistent.
   */
  public boolean isMessagePersistent() {
    return isPersistent;
  }

  /**
   * Sets the DeliveryMode value for the produced messages. If the parameter is
   * true the messages produced are persistent.
   * 
   * @param isPersistent
   *          if true the messages produced are persistent.
   */
  public void setMessagePersistent(boolean isPersistent) {
    this.isPersistent = isPersistent;
  }

  /**
   * Returns the priority of produced messages.
   * 
   * @return the priority of produced messages.
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Sets the priority of produced messages.
   * 
   * @param priority
   *          the priority to set.
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }

  /**
   * Returns the expiration value for produced messages.
   * 
   * @return the expiration value for produced messages.
   */
  public long getExpiration() {
    return expiration;
  }

  /**
   * Sets the expiration value for produced messages.
   * 
   * @param expiration
   *          the expiration to set.
   */
  public void setExpiration(long expiration) {
    this.expiration = expiration;
  }

  /**
   * Resets the acquisition properties.
   */
  private void setProperties(Properties properties) {
    if (isDaemon && running) {
      ((AcquisitionDaemon) acquisitionHandler).stop();
      running = false;
    }

    // Clone properties as it is modified before setting handler properties
    // and we want to keep all properties in destinations to persist them
    Properties props = (Properties) properties.clone();

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.setProperties = " + props + " daemon = " + isDaemon);
    }

    // Restore defaults
    isPrioritySet = false;
    isPersistencySet = false;
    isExpirationSet = false;
    period = 0;
    if (acquisitionTask != null) {
      acquisitionTask.cancel();
    }

    if (props.containsKey(PERIOD)) {
      try {
        period = ConversionHelper.toLong(props.get(PERIOD));
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, "AcquisitionModule: can't parse defined period property.");
      }
      props.remove(PERIOD);
    }
    if (!isDaemon && period > 0) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "schedule acquisition every " + period + "ms.");
      }
      acquisitionTask = new AcquisitionTask();
      AgentServer.getTimer().schedule(acquisitionTask, period, period);
    }

    if (props.containsKey(PERSISTENT_PROPERTY)) {
      try {
        isPersistent = ConversionHelper.toBoolean(props.get(PERSISTENT_PROPERTY));
        isPersistencySet = true;
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, "AcquisitionModule: can't parse defined message persistence property.");
      }
      props.remove(PERSISTENT_PROPERTY);
    }

    if (props.containsKey(PRIORITY_PROPERTY)) {
      try {
        priority = ConversionHelper.toInt(props.get(PRIORITY_PROPERTY));
        isPrioritySet = true;
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, "AcquisitionModule: can't parse defined message priority property.");
      }
      props.remove(PRIORITY_PROPERTY);
    }

    if (props.containsKey(EXPIRATION_PROPERTY)) {
      try {
        expiration = ConversionHelper.toLong(props.get(EXPIRATION_PROPERTY));
        isExpirationSet = true;
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, "AcquisitionModule: can't parse defined message expiration property.");
      }
      props.remove(EXPIRATION_PROPERTY);
    }

    if (props.containsKey(CLASS_NAME)
        && !props.get(CLASS_NAME).equals(acquisitionHandler.getClass().getName())) {
      logger.log(BasicLevel.ERROR,
          "AcquisitionModule: Changing dynamically the acquisition class is not allowed.");
      props.remove(CLASS_NAME);
    }

    if (isDaemon) {
      ((AcquisitionDaemon) acquisitionHandler).start(props, this);
      running = true;
    } else {
      ((AcquisitionHandler) acquisitionHandler).setProperties(props);
    }
  }
  
  /**
   * In <b>periodic mode</b> (period > 0), a message with non-null properties
   * transmit properties to the handler.<br>
   * <br>
   * In <b>request mode</b>, a message received will launch an acquisition
   * process with the given message properties or use the last known properties
   * if empty.<br>
   * <br>
   */
  public void processMessages(ClientMessages cm) {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.processMessages(" + cm + ')');
  	}
    Iterator msgs = cm.getMessages().iterator();
    Properties msgProperties = null;
    while (msgs.hasNext()) {
      Message msg = (Message) msgs.next();
      // If non-empty, sets the new properties
      if (msg.properties != null) {
      	msgProperties = AcquisitionModule.transform(msg.properties);
        setProperties(msgProperties);
      }
      if (!isDaemon && period <= 0) {
        AgentServer.getTimer().schedule(new AcquisitionTask(), 0);
      }
    }
  }
  
  
  /**
   * Update the properties.
   * If daemon stop before resets the acquisition properties.
   * 
   * @param properties new properties
   * @throws Exception
   */
  public void updateProperties(Properties properties) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.updateProperties(" + properties + ')');
    }
  	// If non-empty, sets the new properties
    if (properties != null) {
      setProperties(properties);
    }
  }

  /**
   * Start the daemon.
   * 
   * @param prop properties for start if needed
   * @return properties for the reply.
   * @throws Exception
   */
  public Properties startHandler(Properties prop) throws Exception { 
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.startHandler(" + prop + ')');
    }
    if (isDaemon && !running) {
      ((AcquisitionDaemon) acquisitionHandler).start(prop, this);
      running = true;
    }
  	return null;
  }

  /**
   * Stop the daemon.
   * 
   * @param prop properties for stop if needed
   * @return properties for the reply.
   * @throws Exception
   */
  protected Properties stopHandler(Properties prop) throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.stopHandler(" + prop + ')');
    }
    if (isDaemon && running) {
      ((AcquisitionDaemon) acquisitionHandler).stop();
      running = false;
    }
    return null;
  }
  
  public ClientMessages acquisitionNot(AcquisitionNot not, long msgCount) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionModule.acquisitionNot(" + not + ")");
    }
    ClientMessages acquiredCM = not.getAcquiredMessages();
    List messages = acquiredCM.getMessages();
    if (messages.size() == 0) {
      return null;
    }
    setMessagesInfo(messages, msgCount);
    return acquiredCM;
  }

  private void setMessagesInfo(List messages, long msgCount) {
    long currentTime = System.currentTimeMillis();
    for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
      Message message = (Message) iterator.next();

      message.id = "ID:" + destination.getDestinationId() + '_' + msgCount;
      message.setDestination(destination.getId().toString(), destination.getType());

      if (message.timestamp == 0) {
        message.timestamp = currentTime;
      }
      if (isExpirationSet) {
        if (expiration > 0) {
          message.expiration = currentTime + expiration;
        } else {
          message.expiration = 0;
        }
      }
      if (isPrioritySet) {
        message.priority = priority;
      }
      if (isPersistencySet) {
        message.persistent = isPersistent;
      }
      msgCount++;
    }
  }

  /**
   * Closes the handler.
   */
  public void close() {
    if (isDaemon && running) {
      ((AcquisitionDaemon) acquisitionHandler).stop();
      running = false;
    } else {
      if (acquisitionTask != null) {
        acquisitionTask.cancel();
      }
      ((AcquisitionHandler) acquisitionHandler).close();
    }
  }

  public void transmit(Message message, String messageId) {
    if (message != null) {
      Channel.sendTo(destination.getId(), new AcquisitionNot(new ClientMessages(-1, -1, message),
          isPersistent, messageId));
    }
  }

  public void transmit(List messages, String messagesId) {
    if (messages != null && messages.size() > 0) {
      Channel.sendTo(destination.getId(), new AcquisitionNot(new ClientMessages(-1, -1, messages),
          isPersistent, messagesId));
    }
  }

  public long getPeriod() {
    return period;
  }

  class AcquisitionTask extends TimerTask {
    public void run() {
      try {
        ((AcquisitionHandler) acquisitionHandler).retrieve(AcquisitionModule.this);
      } catch (Throwable exc) {
        logger.log(BasicLevel.ERROR, "Error while doing acquisition.", exc);
      }
    }

  }

}
