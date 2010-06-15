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
import fr.dyade.aaa.common.Debug;

/**
 * The {@link AcquisitionTopicImpl} class implements the MOM acquisition topic
 * behavior, basically acquiring messages periodically or on client request,
 * using an {@link AcquisitionModule}.
 */
public class AcquisitionTopicImpl extends TopicImpl implements AcquisitionTopicImplMBean {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(AcquisitionTopicImpl.class.getName());

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

  /**
   * Constructs an {@link AcquisitionTopicImpl} instance.
   * 
   * @param adminId
   *          Identifier of the administrator of the topic.
   * @param prop
   *          The initial set of properties.
   */
  public AcquisitionTopicImpl(AgentId adminId, Properties properties) throws RequestException {
    super(adminId, properties);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionTopicImpl.<init> prop = " + properties);
    }
    this.properties = properties;

    acquisitionClassName = properties.getProperty(AcquisitionModule.CLASS_NAME);
    properties.remove(AcquisitionModule.CLASS_NAME);
    try {
      AcquisitionModule.checkAcquisitionClass(acquisitionClassName);
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "AcquisitionTopicImpl: error with acquisition class.", exc);
      throw new RequestException(exc.getMessage());
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (acquisitionModule == null) {
      acquisitionModule = new AcquisitionModule(this, acquisitionClassName, properties, Message.TOPIC_TYPE);
    }
  }

  /**
   * @see AcquisitionModule#processMessages(ClientMessages)
   * @see DestinationImpl#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AcquisitionTopicImpl. preProcess(" + from + ", " + cm + ')');
    }
    properties = acquisitionModule.processMessages(cm);
    return null;
  }

  public void acquisitionNot(AcquisitionNot not) {
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
    }
  }

  public String toString() {
    return "AcquisitionTopicImpl:" + getId().toString();
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

  public void close() {
    if (acquisitionModule != null) {
      acquisitionModule.close();
    }
  }

  public long getAcquisitionPeriod() {
    return acquisitionModule.getPeriod();
  }

}
