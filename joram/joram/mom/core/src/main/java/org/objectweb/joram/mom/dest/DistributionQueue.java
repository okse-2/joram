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
import fr.dyade.aaa.agent.AgentServer;
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
  
  public static final String ASYNC_DISTRIBUTION_OPTION = "distribution.async";

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private transient DistributionModule distributionModule;
  
  private transient DistributionDaemon distributionDaemon;
  
  /** The acquisition class name. */
  private String distributionClassName;

  /**
   * Tells if we try to distribute the each message each time (true) or if the
   * distribution is stopped on first error (false). Batch mode can (and will
   * probably) lose message ordering but will not stop deliverable messages in
   * the queue waiting for previous ones to be sent.
   */
  private boolean batchDistribution;
  
  /**
   * Tells if daemon distribution is active.
   * On true, the batchDistribution is set.
   */
  private boolean isAsyncDistribution;

  private Properties properties;

  public DistributionQueue() {
    super();
    fixed = true;
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
      logger.log(BasicLevel.DEBUG, "DistributionQueue.setProperties prop = " + properties);
    }

    this.properties = properties;

    batchDistribution = false;
    isAsyncDistribution = false;

    if (properties != null) {
    	if (properties.containsKey(BATCH_DISTRIBUTION_OPTION)) {
    		try {
    			batchDistribution = ConversionHelper.toBoolean(properties.get(BATCH_DISTRIBUTION_OPTION));
    		} catch (MessageValueException exc) {
    			logger.log(BasicLevel.ERROR, "DistributionModule: can't parse batch option.", exc);
    		}
    	}
    	isAsyncDistribution = isAsyncDistribution(properties);
    	if (isAsyncDistribution)
    		batchDistribution = true;
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
        Class.forName(distributionClassName).getConstructor();
      } catch (Exception exc) {
        logger.log(BasicLevel.ERROR, "DistributionQueue: error with distribution class.", exc);
        throw new RequestException(exc.getMessage());
      }
    } else {
      distributionModule.setProperties(properties,firstTime);
      
    	if (distributionDaemon == null && isAsyncDistribution) {
    		// start distributionDaemon
    		distributionDaemon = new DistributionDaemon(distributionModule.getDistributionHandler(), getAgentId(), getName(), this);
    		distributionDaemon.start();
    	} else if (distributionDaemon != null && !isAsyncDistribution) {
    		// stop distributionDaemon
    		distributionDaemon.close();
    		distributionDaemon = null;
    		if (properties.containsKey(BATCH_DISTRIBUTION_OPTION)) {
      		try {
      			batchDistribution = ConversionHelper.toBoolean(properties.get(BATCH_DISTRIBUTION_OPTION));
      		} catch (MessageValueException exc) {
      			logger.log(BasicLevel.ERROR, "DistributionQueue: can't parse batch option.", exc);
      		}
      	}
    	}

      //TODO: if isAsyncDistribution change, call wakeup.
    	
    }
  }

  private boolean isAsyncDistribution(Properties properties) {
  	if (properties.containsKey(ASYNC_DISTRIBUTION_OPTION)) {
  		try {
  			return ConversionHelper.toBoolean(properties.get(ASYNC_DISTRIBUTION_OPTION));
  		} catch (MessageValueException exc) {
  			logger.log(BasicLevel.ERROR, "DistributionQueue: can't parse DaemonDistribution option.", exc);
  		}	
  	}
  	return false;
  }
  
  public void initialize(boolean firstTime) throws Exception {
    super.initialize(firstTime);

    try {
      String list = (String) AgentServer.getTransaction().load(getAgentId() + "AL");
      
      if (logmsg.isLoggable(BasicLevel.DEBUG))
        logmsg.log(BasicLevel.DEBUG, getName() + ": removes " + list);
      
      if ((list != null) && (list.length() > 0)) {
        String[] ids = list.split(",");
        for (int i=0; i<ids.length; i++) {
          removeAndDeleteMessage(ids[i]);
        }
      }
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "DistributionQueue: error during initialisation.", exc);
    }

    if (distributionModule == null) {
      distributionModule = new DistributionModule(distributionClassName, properties, firstTime);
    }
    if (properties != null)
    	isAsyncDistribution = isAsyncDistribution(properties);
    if (distributionDaemon == null && isAsyncDistribution) {
    	distributionDaemon = new DistributionDaemon(distributionModule.getDistributionHandler(), getAgentId(), getName(), this);
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

  // Bug fix (JORAM-232): Adapts the counters algorithmic to fit with the behavior of the
  // Distribution Queue.
  
  protected long nbMsgsReceiveSinceCreation = 0;
  
  public final long getNbMsgsReceiveSinceCreation() {
    return nbMsgsReceiveSinceCreation;
  }

  public final long getNbMsgsDeliverSinceCreation() {
    return nbMsgsReceiveSinceCreation - nbMsgsSentToDMQSinceCreation -  getPendingMessageCount();
  }

  /**
   * @see DistributionModule#processMessages(ClientMessages)
   * @see Destination#preProcess(AgentId, ClientMessages)
   */
  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionQueue.preProcess(" + from + ", " + cm + ')');

    // This method modifies the counters (Bug fix JORAM-232).
    setSave();

    if (!batchDistribution && messages.size() > 0) {
      // we already have an Exception because messages.size>0
      // so return immediately the new client messages
      return cm;
    }

    if (! isAsyncDistribution) {
      List msgs = cm.getMessages();
      for (Iterator ite = msgs.iterator(); ite.hasNext();) {
        // Bug fix JORAM-232: now counts received messages.
        nbMsgsReceiveSinceCreation++;
        
        Message msg = (Message) ite.next();
        try {
          distributionModule.processMessage(msg);
          nbMsgsDeliverSinceCreation++;
          ite.remove();
        } catch (Exception exc) {
          if (logger.isLoggable(BasicLevel.WARN))
            logger.log(BasicLevel.WARN, "DistributionQueue.preProcess: distribution error.", exc);

          // if we don't do batch distribution, stop on first error
          if (!batchDistribution)
            break;
        }
      }

      if (msgs.size() > 0) {
        return cm;
      }
      return null;
    } else {
      return cm;
    }

  }

  private void removeAndDeleteMessages() {
    // This method modifies the counters (Bug fix JORAM-232).
    setSave();
    
    if (logmsg.isLoggable(BasicLevel.DEBUG))
      logmsg.log(BasicLevel.DEBUG, getName() + ": removeAndDeleteMessages..");
        
    String id = distributionDaemon.getNextAck();
    while (id != null) {
      removeAndDeleteMessage(id);
      id = distributionDaemon.getNextAck();
    }
  }

  private void removeAndDeleteMessage(String id) {
//  if (logger.isLoggable(BasicLevel.DEBUG))
//  logger.log(BasicLevel.DEBUG, "DistributionQueue.removeAndDeleteMessages() - Acked: " + id);

    int i = 0;
    org.objectweb.joram.mom.messages.Message message = null;
    while (i < messages.size()) {
      message = (org.objectweb.joram.mom.messages.Message) messages.get(i);

//    if (logger.isLoggable(BasicLevel.DEBUG))
//      logger.log(BasicLevel.DEBUG, "DistributionQueue.removeAndDeleteMessages() - handles: " + message.getId());

      if (id.equals(message.getId())) {
        messages.remove(i);
        message.delete();

        // Now this counter is no longer used (JORAM-232).
        nbMsgsDeliverSinceCreation++;

        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "DistributionQueue.removeAndDeleteMessages() - removes " + id);

        if (logmsg.isLoggable(BasicLevel.INFO))
          logmsg.log(BasicLevel.INFO, getName() + ": removes message " + id);

        break;
      }
      // Bug fix (JORAM-199): avoid infinite loop!!
      i++;
    }
  }

  @Override
  protected void postProcess(ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "DistributionQueue.postProcess(...)");
    super.postProcess(cm);
    
    // Bug fix (JORAM-198): Avoid parallelism access during message serialization (save method).
    // The handling of messages is no longer in pre-process.
    if (isAsyncDistribution) {
      List msgs = cm.getMessages();

      for (Iterator ite = msgs.iterator(); ite.hasNext();) {
        // Bug fix JORAM-232: now counts received messages.
        nbMsgsReceiveSinceCreation++;

        Message msg = (Message) ite.next();

        // a processMessage exception is normal with async mode.
        if (distributionDaemon != null) {
          // use msg.id ?
          distributionDaemon.push(msg);
        } else {
          if (logger.isLoggable(BasicLevel.WARN)) {
            logger.log(BasicLevel.WARN,
                       "DistributionQueue.postProcess: distribution distributionDaemon = null in async mode.");
          }
        }
      }
    }
    
    if (distributionDaemon != null) {
      // Cleans message list using ackList from daemon.
      removeAndDeleteMessages();
    }
  }
  
  public String toString() {
    return "DistributionQueue:" + getId().toString();
  }

  /**
   * wake up, and cleans the queue.
   */
  public void wakeUpNot(WakeUpNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG) && !isAsyncDistribution) {
      logger.log(BasicLevel.DEBUG, "DistributionQueue.wakeUpNot(" + not + ')');
    }
    // Cleans outdated waiting messages
    super.wakeUpNot(not);

    // This method modifies the counters (Bug fix JORAM-232).
    setSave();

    if (distributionDaemon != null) {
      // Cleans message list using ackList from daemon.
      removeAndDeleteMessages();
    }

    for (Iterator ite = messages.iterator(); ite.hasNext();) {
      org.objectweb.joram.mom.messages.Message msg = (org.objectweb.joram.mom.messages.Message) ite.next();
      try {
        distributionModule.processMessage(msg.getFullMessage());
        nbMsgsDeliverSinceCreation++;
        ite.remove();
        msg.delete();
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.DEBUG) && !isAsyncDistribution) {
          logger.log(BasicLevel.DEBUG,
                     "DistributionQueue.wakeUpNot redelivery number " + msg.getDeliveryCount() + " failed.", exc);
        } else if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG,
                     "DistributionQueue.wakeUpNot redelivery " + msg.getId() + " number " + msg.getDeliveryCount());
        }

        // increase the delivery count
        if (distributionDaemon == null)
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

        if (distributionDaemon != null) {
          synchronized (distributionDaemon) {
            // Wakeup the daemon, because the distributionDaemon can wait 
            // after a distribution exception
            distributionDaemon.notify();
          }
        }

        if (logger.isLoggable(BasicLevel.DEBUG) && distributionDaemon != null) {
          logger.log(BasicLevel.DEBUG, "DistributionQueue.wakeUpNot distributionDaemon = " + distributionDaemon + 
                     ", distributionDaemon.isEmpty() = " + distributionDaemon.isEmpty());
        }

        if (distributionDaemon != null) {
          if (!distributionDaemon.isEmpty()) {
            // needless to push an other message 
            // because the distributionDaemon can't distribute message now.
            break;
          } else {
            if (logger.isLoggable(BasicLevel.DEBUG))
              logger.log(BasicLevel.DEBUG, "DistributionQueue.wakeUpNot " + msg.getId());

            // Bug fix (JORAM-200): Avoid to duplicate a message already known by the daemon (either in
            // the distributeQueue or the ackedQueue).
            if (! distributionDaemon.isHandling(msg.getId()))
              distributionDaemon.push(msg.getFullMessage());
          }
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
  
  public int getEncodableClassId() {
    // Not defined: still not encodable
    return -1;
  }
  
}
