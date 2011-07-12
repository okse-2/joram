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

import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link AcquisitionTopic} class implements the MOM acquisition topic
 * behavior, basically acquiring messages periodically or on client request,
 * using an {@link AcquisitionModule}.
 */
public class AcquisitionTopic extends Topic implements AcquisitionTopicMBean {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(AcquisitionTopic.class.getName());

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

  public AcquisitionTopic() {
    fixed = true;
  }

  /**
   * Configures an {@link AcquisitionTopic} instance.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void setProperties(Properties properties, boolean firstTime) throws Exception {
    super.setProperties(properties, firstTime);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionTopic.setProperties prop = " + properties);
    }
    this.properties = properties;

    // Acquisition class name can only be set the first time.
    if (firstTime) {
      if (properties != null) {
        acquisitionClassName = properties.getProperty(AcquisitionModule.CLASS_NAME);
        properties.remove(AcquisitionModule.CLASS_NAME);
      }
      if (acquisitionClassName == null) {
        throw new RequestException("Acquisition class name not found: " + AcquisitionModule.CLASS_NAME
            + " property must be set on topic creation.");
      }
      try {
        AcquisitionModule.checkAcquisitionClass(acquisitionClassName);
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "AcquisitionTopic: error with acquisition class.", exc);
        throw new RequestException(exc.getMessage());
      }
    }

    if (!firstTime) {
      acquisitionModule.setProperties(properties);
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (acquisitionModule == null) {
      acquisitionModule = new AcquisitionModule(this, acquisitionClassName, properties);
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
   * Incoming JMS messages are processed by the acquisition module 
   * and a null ClientMessages is always returned to the base
   * implementation.
   * 
   * @see AcquisitionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionTopic.preProcess(" + from + ", " + cm + ')');
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
      logger.log(BasicLevel.DEBUG, "AcquisitionTopic.startHandler(" + prop + ')');
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
      logger.log(BasicLevel.DEBUG, "AcquisitionTopic.stopHandler(" + prop + ')');
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
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Message already received, drop the message " + not);
      }
      return;
    }
    lastMessageId = not.getId();
    ClientMessages clientMessages = acquisitionModule.acquisitionNot(not, msgCount);
    if (clientMessages != null) {
      msgCount += clientMessages.getMessageCount();
      forwardMessages(clientMessages);
      processMessages(clientMessages);
      postProcess(clientMessages);
    }
  }

  public String toString() {
    return "AcquisitionTopic:" + getId().toString();
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
