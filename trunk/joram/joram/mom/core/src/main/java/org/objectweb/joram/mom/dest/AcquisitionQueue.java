/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2012 ScalAgent Distributed Technologies
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

import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link AcquisitionQueue} class implements the MOM acquisition queue
 * behavior, basically acquiring messages periodically or on client request,
 * using an {@link AcquisitionModule}.
 */
public class AcquisitionQueue extends Queue implements AcquisitionQueueMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(AcquisitionQueue.class.getName());

  /** The acquisition module. */
  private transient AcquisitionModule acquisitionModule;

  /** Stores the last set of properties defined. */
  private Properties properties;

  /** Stores the id of the last message received to avoid duplicates. */
  private String lastMessageId;

  /** The number of produced messages. */
  private long msgCount = 0;

  /**
   * Returns the number of acquired messages processed by the destination.
   * 
   * @return the number of acquired messages processed by the destination.
   */
  public final long getHandledMsgCount() {
    return msgCount;
  }
  
  /** The threshold of messages send by the handler in the engine */
  private long diff_max = 20;
  private long diff_min = 10;
  
  private String ACQ_QUEUE_MAX_MSG = "acquisition.max_msg";
  private String ACQ_QUEUE_MIN_MSG = "acquisition.min_msg";
  
  /**
   * Returns the maximum number of acquired messages waiting to be handled by
   * the destination. When the number of messages waiting to be handled is greater
   * the acquisition handler is temporarily stopped.
   * <p>
   * A value lesser or equal to 0 disables the mechanism.
   * 
   * @return the maximum number of acquired messages waiting to be handled by
   * the destination.
   */
  public final long getDiffMax() {
    return diff_max;
  }
  
  /**
   * Returns the minimum threshold of acquired messages waiting to be handled by
   * the destination for restarting the acquisition handler.
   * 
   * @return the minimum threshold of acquired messages waiting to be handled by
   * the destination.
   */
  public final long getDiffMin() {
    return diff_min;
  }
  
  /** The threshold of pending messages in the queue */
  private long pending_max = 20;
  private long pending_min = 10;
  
  private String ACQ_QUEUE_MAX_PND = "acquisition.max_pnd";
  private String ACQ_QUEUE_MIN_PND = "acquisition.min_pnd";
  
  /**
   * Returns the maximum number of waiting messages in the destination. When the number
   * of waiting messages is greater the acquisition handler is temporarily stopped.
   * <p>
   * A value lesser or equal to 0 disables the mechanism.
   * 
   * @return the maximum number of waiting messages in the destination.
   */
  public final long getPendingMax() {
    return pending_max;
  }
  
  /**
   * Returns the minimum threshold of waiting messages in the destination for restarting
   * the acquisition handler.
   * 
   * @return the minimum threshold of waiting messages in the destination.
   */
  public final long getPendingMin() {
    return pending_min;
  }
  
  private boolean pause = false;

  /** The acquisition class name. */
  private String acquisitionClassName;

  public String getAcquisitionClassName() {
    return acquisitionClassName;
  }

  public AcquisitionQueue() {
    fixed = true;
  }

  /**
   * Configures an {@link AcquisitionQueue} instance.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void setProperties(Properties properties, boolean firstTime) throws Exception {
    super.setProperties(properties, firstTime);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionQueue.setProperties prop = " + properties);
    }
    this.properties = properties;
    
    diff_max = Long.parseLong(properties.getProperty(ACQ_QUEUE_MAX_MSG, String.valueOf(diff_max)));
    diff_min = Long.parseLong(properties.getProperty(ACQ_QUEUE_MIN_MSG, String.valueOf(diff_min)));
    pending_max = Long.parseLong(properties.getProperty(ACQ_QUEUE_MAX_PND, String.valueOf(pending_max)));
    pending_min = Long.parseLong(properties.getProperty(ACQ_QUEUE_MIN_PND, String.valueOf(pending_min)));

    // Acquisition class name can only be set the first time.
    if (firstTime) {
      if (properties != null) {
        acquisitionClassName = properties.getProperty(AcquisitionModule.CLASS_NAME);
        properties.remove(AcquisitionModule.CLASS_NAME);
      }
      if (acquisitionClassName == null) {
        throw new RequestException("Acquisition class name not found: " + AcquisitionModule.CLASS_NAME
            + " property must be set on queue creation.");
      }
      try {
        AcquisitionModule.checkAcquisitionClass(acquisitionClassName);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "AcquisitionQueue: error with acquisition class.", exc);
        throw new RequestException(exc.getMessage());
      }
    } else {
      acquisitionModule.setProperties(properties);
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (acquisitionModule == null) {
      acquisitionModule = new AcquisitionModule(this, acquisitionClassName, properties);
    }
  }
  
  /**
   * Returns the number of messages acquired by the acquisition handler.
   * Be careful this counter is reseted at each time the server starts.
   * 
   * @return the number of messages acquired by the acquisition handler.
   */
  public final long getAcquiredMsgCount() {
    return AcquisitionModule.getCount();
  }

  private transient long acquisitionNotNb = 0;
  
  public void react(AgentId from, Notification not) throws Exception {
    try {
      long diff = AcquisitionModule.getCount() - acquisitionNotNb;
      int pending = getPendingMessageCount();

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.ERROR, "AcquisitionQueue.react: " + pause + ", " + diff + ", " + pending);

      if (!pause && 
          (((diff_max > 0) && (diff >= diff_max)) || 
           ((pending_max > 0) && (pending >= pending_max)))) {
        stopHandler(properties);
        pause = true;
      } else if (pause && (diff <= diff_min) && (pending <= pending_min)){
        startHandler(properties);
        pause = false;
      }

      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.ERROR, "AcquisitionQueue.react: " + pause + ", " + diff + ", " + pending);
    } catch (Throwable t) {
      logger.log(BasicLevel.ERROR, "AcquisitionQueue: error in react.", t);
    }
    if (not instanceof AcquisitionNot) {
      acquisitionNotNb += 1;
      acquisitionNot((AcquisitionNot) not);
    } else {
      super.react(from, not);
    }
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    close();
  }
  
  /**
   * Incoming JMS messages are processed by the acquisition module 
   * and a null ClientMessages is always returned to the base
   * implementation.
   * 
   * @see AcquisitionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionQueue.preProcess(" + from + ", " + cm + ')');
    }
    acquisitionModule.processMessages(cm);
    return null;
  }
  
  /**
   * Start the handler.
   * 
   * @param prop properties for start if needed
   * @return properties for the reply.
   * @throws Exception
   */
  protected Properties startHandler(Properties prop) throws Exception { 
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionQueue.startHandler(" + prop + ')');
    }
  	Properties p = prop;
  	if (p == null)
  		p = properties;
  	return acquisitionModule.startHandler(p);
  }

  /**
   * Stop the handler.
   * 
   * @param prop properties for stop if needed
   * @return properties for the reply.
   * @throws Exception
   */
  protected Properties stopHandler(Properties prop) throws Exception {
  	if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionQueue.stopHandler(" + prop + ')');
    }
  	Properties p = prop;
  	if (p == null)
  		p = properties;
  	return acquisitionModule.stopHandler(p); 
  }

  /**
   * This method process messages from the acquisition module.
   * The method addClientMessages of base implementation is used to handle
   * incoming messages.
   * 
   * @param not
   */
  private void acquisitionNot(AcquisitionNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "acquisitionNot(" + not + ")");
    }
    // Test if the message has already been received to avoid duplicates
    if (lastMessageId != null && lastMessageId.equals(not.getId())) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Message already received, drop the message " + not);
      return;
    }
    lastMessageId = not.getId();
    ClientMessages clientMessages = acquisitionModule.acquisitionNot(not, msgCount);
    if (clientMessages != null) {
      msgCount += clientMessages.getMessageCount();
      try {
        addClientMessages(clientMessages, false);
      } catch (AccessException e) {/* never happens */}
    }
  }

  public String toString() {
    return "AcquisitionQueue:" + getId().toString();
  }

  public long getExpiration() {
    return acquisitionModule.getExpiration();
  }

  public int getPriority() {
    return acquisitionModule.getPriority();
  }

  public boolean isMessagePersistent() {
    return acquisitionModule.isMessagePersistent();
  }

  public void setExpiration(long expiration) {
    acquisitionModule.setExpiration(expiration);
  }

  public void setMessagePersistent(boolean isPersistent) {
    acquisitionModule.setMessagePersistent(isPersistent);
  }

  public void setPriority(int priority) {
    acquisitionModule.setPriority(priority);
  }

  private void close() {
    if (acquisitionModule != null) {
      acquisitionModule.close();
    }
  }

  public long getAcquisitionPeriod() {
    return acquisitionModule.getPeriod();
  }
  
  public int getEncodableClassId() {
    // Not defined: still not encodable
    return -1;
  }

}
