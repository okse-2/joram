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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.WakeUpNot;
import org.objectweb.joram.mom.util.DMQManager;
import org.objectweb.joram.shared.MessageErrorConstants;
import org.objectweb.joram.shared.excepts.MessageValueException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.joram.shared.messages.ConversionHelper;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link DistributionQueue} class implements the MOM distribution queue
 * behavior, delivering messages via the {@link DistributionModule}.
 */
public class DistributionQueue extends Queue {

  public static Logger logger = Debug.getLogger(DistributionQueue.class.getName());

  /** Default period used to clean queue and re-distribute failing messages. */
  public static final long DEFAULT_PERIOD = 1000;

  public static final String BATCH_DISTRIBUTION_OPTION = "distribution.batch";

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private transient DistributionModule distributionModule;
  
  /** The acquisition class name. */
  private String distributionClassName;

  /**
   * Tells if we try to distribute the each message each time (true) or if the
   * distribution is stopped on first error (false). Batch mode can (and will
   * probably) lose message ordering but will not stop deliverable messages in
   * the queue waiting for previous ones to be sent.
   */
  private boolean batchDistribution;

  private Properties properties;

  public DistributionQueue() {
    super();
  }

  /**
   * Configures a {@link DistributionQueue} instance.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void setProperties(Properties properties, boolean firstTime) throws Exception {
    super.setProperties(properties, firstTime);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DistributionQueue.<init> prop = " + properties);
    }

    this.properties = properties;

    batchDistribution = false;

    if (properties != null && properties.containsKey(BATCH_DISTRIBUTION_OPTION)) {
      try {
        batchDistribution = ConversionHelper.toBoolean(properties.get(BATCH_DISTRIBUTION_OPTION));
      } catch (MessageValueException exc) {
        logger.log(BasicLevel.ERROR, "DistributionModule: can't parse batch option.", exc);
      }
    }

    if (firstTime) {
      if (properties != null) {
        distributionClassName = properties.getProperty(DistributionModule.CLASS_NAME);
        properties.remove(DistributionModule.CLASS_NAME);
      }
      if (distributionClassName == null) {
        throw new RequestException("Distribution class name not found: " + DistributionModule.CLASS_NAME
            + " property must be set on queue creation.");
      }

      // Check the existence of the distribution class and the presence of a no-arg constructor.
      try {
        String className = distributionClassName;
        Class.forName(className).getConstructor();
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "DistributionQueue: error with distribution class.", exc);
        throw new RequestException(exc.getMessage());
      }
    } else {
      distributionModule.setProperties(properties);
    }
  }

  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (distributionModule == null) {
      distributionModule = new DistributionModule(distributionClassName, properties);
    }
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    if (distributionModule != null) {
      distributionModule.close();
    }
  }

  /**
   * @see DistributionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DistributionQueue.preProcess(" + from + ", " + cm + ')');
    }
    if (!batchDistribution && messages.size() > 0) {
      return cm;
    }

    List msgs = cm.getMessages();
    for (Iterator ite = msgs.iterator(); ite.hasNext();) {
      Message msg = (Message) ite.next();
      try {
        distributionModule.processMessage(msg);
        nbMsgsDeliverSinceCreation++;
        ite.remove();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.WARN)) {
          logger.log(BasicLevel.WARN, "DistributionModule: distribution error.", exc);
        }
        // if we don't do batch distribution, stop on first error
        if (!batchDistribution) {
          break;
        }
      }
    }

    if (msgs.size() > 0) {
      return cm;
    }
    return null;
  }

  public String toString() {
    return "DistributionQueue:" + getId().toString();
  }

  /**
   * wake up, and cleans the queue.
   */
  public void wakeUpNot(WakeUpNot not) {
    super.wakeUpNot(not);
    for (Iterator ite = messages.iterator(); ite.hasNext();) {
      org.objectweb.joram.mom.messages.Message msg = (org.objectweb.joram.mom.messages.Message) ite.next();
      try {
        distributionModule.processMessage(msg.getFullMessage());
        nbMsgsDeliverSinceCreation++;
        ite.remove();
        msg.delete();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Distribution redelivery number " + msg.getDeliveryCount()
              + " failed.", exc);
        }
        msg.incDeliveryCount();
        // If message considered as undeliverable, add it to the list of dead messages:
        if (isUndeliverable(msg)) {
          if (logger.isLoggable(BasicLevel.DEBUG)) {
            logger.log(BasicLevel.DEBUG, "Message can't be delivered, send to DMQ.");
          }
          ite.remove();
          msg.delete();
          DMQManager dmqManager = new DMQManager(dmqId, getId());
          nbMsgsSentToDMQSinceCreation++;
          dmqManager.addDeadMessage(msg.getFullMessage(), MessageErrorConstants.UNDELIVERABLE);
          dmqManager.sendToDMQ();
          continue;
        }
        if (!batchDistribution) {
          break;
        }
      }
    }
  }

  protected void processSetRight(AgentId user, int right) throws RequestException {
    if (right == READ) {
      throw new RequestException("A distribution queue can't be set readable.");
    }
    super.processSetRight(user, right);
  }
}
