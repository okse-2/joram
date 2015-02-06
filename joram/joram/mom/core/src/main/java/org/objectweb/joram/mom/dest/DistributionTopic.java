/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 - 2015 ScalAgent Distributed Technologies
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
 * The {@link DistributionQueue} class implements the MOM distribution topic
 * behavior, delivering messages via the {@link DistributionModule}.
 */
public class DistributionTopic extends Topic {

  public static Logger logger = Debug.getLogger(DistributionTopic.class.getName());

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private transient DistributionModule distributionModule;
  
  /** The acquisition class name. */
  private String distributionClassName;

  private Properties properties;
  
  private transient DistributionDaemon distributionDaemon;  
  
  /**
   * Tells if daemon distribution is active.
   * On true, the batchDistribution is set.
   */
  private boolean isAsyncDistribution;

  /**
   * Configures a {@link DistributionTopic} instance.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void setProperties(Properties properties, boolean firstTime) throws Exception {
    super.setProperties(properties, firstTime);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "DistributionTopic.<init> prop = " + properties);
    }

    this.properties = properties;

    if (properties != null) {
    	isAsyncDistribution = isAsyncDistribution(properties);
    }
    
    if (firstTime) {
      if (properties != null) {
        distributionClassName = properties.getProperty(DistributionModule.CLASS_NAME);
        properties.remove(DistributionModule.CLASS_NAME);
      }
      if (distributionClassName == null) {
        throw new RequestException("Distribution class name not found: " + DistributionModule.CLASS_NAME
            + " property must be set on topic creation.");
      }

      // Check the existence of the distribution class and the presence of a no-arg constructor.
      try {
        Class.forName(distributionClassName).getConstructor();
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "DistributionTopic: error with distribution class.", exc);
        throw new RequestException(exc.getMessage());
      }
    } else {
      distributionModule.setProperties(properties, firstTime);
      
      if (distributionDaemon == null && isAsyncDistribution) {
    		// start distributionDaemon
    		distributionDaemon = new DistributionDaemon(distributionModule.getDistributionHandler(), getName(), this);
    		distributionDaemon.start();
    	} else if (distributionDaemon != null && !isAsyncDistribution) {
    		// stop distributionDaemon
    		distributionDaemon.close();
    		distributionDaemon = null;
    	}
    }
  }

  private boolean isAsyncDistribution(Properties properties) {
  	if (properties.containsKey(DistributionQueue.ASYNC_DISTRIBUTION_OPTION)) {
  		try {
  			return ConversionHelper.toBoolean(properties.get(DistributionQueue.ASYNC_DISTRIBUTION_OPTION));
  		} catch (MessageValueException exc) {
  			logger.log(BasicLevel.ERROR, "DistributionModule: can't parse DaemonDistribution option.", exc);
  		}	
  	}
  	return false;
  }
  
  public void initialize(boolean firstTime) {
    super.initialize(firstTime);
    if (distributionModule == null) {
      distributionModule = new DistributionModule(distributionClassName, properties, firstTime);
    }
    if (properties != null)
    	isAsyncDistribution = isAsyncDistribution(properties);
    if (distributionDaemon == null && isAsyncDistribution) {
    	distributionDaemon = new DistributionDaemon(distributionModule.getDistributionHandler(), getName(), this);
    	distributionDaemon.start();
    }
  }

  public void agentFinalize(boolean lastTime) {
    super.agentFinalize(lastTime);
    if (distributionModule != null) {
      distributionModule.close();
    }
    if (distributionDaemon != null) {
    	distributionDaemon.close();
    }
  }
  
  /**
   * @see DistributionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionTopic. preProcess(" + from + ", " + cm + ')');

    List msgs = cm.getMessages();
    // This method return null so the counter will not be updated in processMessage method
    nbMsgsReceiveSinceCreation = nbMsgsReceiveSinceCreation + msgs.size();

    DMQManager dmqManager = null;
    for (int i = 0; i < msgs.size(); i++) {
      Message msg = (Message) msgs.get(i);
      try {
        if (isAsyncDistribution) {
          if (distributionDaemon != null) {
            distributionDaemon.push(msg);
          } else {
            throw new Exception("distribution distributionDaemon is null");
          }
        } else {
          distributionModule.processMessage(msg);
          nbMsgsDeliverSinceCreation++;
        }
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "DistributionTopic: error during distribution.", exc);

        if (dmqManager == null)
          dmqManager = new DMQManager(cm.getDMQId(), getDMQAgentId(), getId());

        nbMsgsSentToDMQSinceCreation++;
        dmqManager.addDeadMessage(msg, MessageErrorConstants.UNDELIVERABLE);
      }
    }
    if (dmqManager != null) {
      dmqManager.sendToDMQ();
    }
    return null;
  }

  @Override
  protected void postProcess(ClientMessages msgs) {
    super.postProcess(msgs);
    if (distributionDaemon != null) {
    	if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "DistributionTopic postProcess(...)");
    	List ackList = distributionDaemon.getAckList();
    	nbMsgsDeliverSinceCreation += ackList.size();
    }
  }
  
  public String toString() {
    return "DistributionTopic:" + getId().toString();
  }

  public void wakeUpNot(WakeUpNot not) {
  	if (logger.isLoggable(BasicLevel.DEBUG) && !isAsyncDistribution) {
  		logger.log(BasicLevel.DEBUG, "DistributionTopic.wakeUpNot(" + not + ')');
  	}
  	super.wakeUpNot(not);

  	// delete the ackQueue
  	if (distributionDaemon != null) {
  		List ackList = distributionDaemon.getAckList();
      nbMsgsDeliverSinceCreation += ackList.size();
  	}

  	if (distributionDaemon != null) {
  		synchronized (distributionDaemon) {
  			// wakeup the daemon, because the distributionDaemon can wait 
    		// after a distribution exception
  			distributionDaemon.notify();
  		}
  	}
  }
  
}
