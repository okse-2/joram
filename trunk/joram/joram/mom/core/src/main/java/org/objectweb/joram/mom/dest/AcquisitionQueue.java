/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.Message;
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
  public void setProperties(Properties properties) throws RequestException {
    super.setProperties(properties);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionQueue.<init> prop = " + properties);
    }
    if (properties == null) {
      throw new RequestException("No property found: At least " + AcquisitionModule.CLASS_NAME
          + " property must be defined on queue creation.");
    }
    this.properties = properties;

    acquisitionClassName = properties.getProperty(AcquisitionModule.CLASS_NAME);
    properties.remove(AcquisitionModule.CLASS_NAME);
    try {
      AcquisitionModule.checkAcquisitionClass(acquisitionClassName);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "AcquisitionQueue: error with acquisition class.", exc);
      throw new RequestException(exc.getMessage());
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (acquisitionModule == null) {
      acquisitionModule = new AcquisitionModule(this, acquisitionClassName, properties, Message.QUEUE_TYPE);
    }
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof AcquisitionNot) {
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
   * Incoming JMS messages are used for configuration, they are processed by the
   * acquisition module and a null ClientMessages is always returned to the base
   * implementation.
   * 
   * @see AcquisitionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionQueue. preProcess(" + from + ", " + cm + ')');
    }
    properties = acquisitionModule.processMessages(cm);
    return null;
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
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Message already received, drop the message " + not);
      }
      return;
    }
    lastMessageId = not.getId();
    ClientMessages clientMessages = acquisitionModule.acquisitionNot(not, msgCount);
    if (clientMessages != null) {
      msgCount += clientMessages.getMessageCount();
      addClientMessages(clientMessages);
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

}
