/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2012 ScalAgent Distributed Technologies
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
import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.mom.notifications.PingNot;
import org.objectweb.joram.mom.notifications.PongNot;
import org.objectweb.joram.shared.excepts.AccessException;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.ExpiredNot;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.UnknownAgent;
import fr.dyade.aaa.common.Debug;

/**
 * The {@link AliasQueue} class forwards messages to a destination in an other
 * Joram server using the destination ID.
 */
public class AliasQueue extends Queue {

  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(AliasQueue.class.getName());

  public static final String REMOTE_AGENT_OPTION = "remoteAgentID";
  
  /** The queue to send the notification to */
  private AgentId remoteDestinationID = null;

  /** The maximum time the notification can wait in the network before expiration. */
  private long expiration = 1000;

  public AliasQueue() {
    super();
  }

  /**
   * Configures an {@link AliasQueue} instance.
   * 
   * @param properties
   *          The initial set of properties.
   */
  public void setProperties(Properties properties, boolean firstTime) throws Exception {
    super.setProperties(properties, firstTime);

    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AliasQueue.<init> prop = " + properties);
    }

    remoteDestinationID = null;

    if (properties != null && properties.containsKey(REMOTE_AGENT_OPTION)) {
      try {
        remoteDestinationID = AgentId.fromString(properties.getProperty(REMOTE_AGENT_OPTION));
      } catch (IllegalArgumentException exc) {
        logger.log(BasicLevel.ERROR, "AliasQueue: can't parse '" + REMOTE_AGENT_OPTION + " option.", exc);
      }
    }

    if (remoteDestinationID == null) {
      throw new Exception("Remote agent identifier is null or invalid." + " The property '"
          + REMOTE_AGENT_OPTION + "' of the Alias queue has not been set properly.");
    }
  }

  public ClientMessages preProcess(AgentId from, ClientMessages cm) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "AliasQueue.preProcess(" + from + ", " + cm + ')');
    }
    if (messages.size() > 0) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "Messages are already waiting, enqueue the new ones");
      }
      return cm;
    }

    ClientMessages forward = new ClientMessages(-1, -1, cm.getMessages());
    forward.setExpiration(System.currentTimeMillis() + expiration);
    forward.setDeadNotificationAgentId(getId());
    forward.setAsyncSend(true);

    /* sending the notification */
    Channel.sendTo(remoteDestinationID, forward);
    nbMsgsDeliverSinceCreation += forward.getMessageCount();
    return null;
  }

  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AliasQueue.react(" + from + ',' + not + ')');

    if (not instanceof PongNot) {
      if (logger.isLoggable(BasicLevel.DEBUG)) {
        logger.log(BasicLevel.DEBUG, "PongNot received, unqueue the waiting messages.");
      }
      ClientMessages cm = new ClientMessages();
      cm.setExpiration(System.currentTimeMillis() + expiration);
      cm.setDeadNotificationAgentId(getId());

      for (Iterator ite = messages.iterator(); ite.hasNext();) {
        org.objectweb.joram.mom.messages.Message msg = (org.objectweb.joram.mom.messages.Message) ite.next();
        cm.addMessage(msg.getFullMessage());
        ite.remove();
        msg.delete();
      }

      /* sending the notification */
      Channel.sendTo(remoteDestinationID, cm);
      nbMsgsDeliverSinceCreation += cm.getMessageCount();
    } else {
      super.react(from, not);
    }
  }

  protected void handleExpiredNot(AgentId from, ExpiredNot not) {
    if (logger.isLoggable(BasicLevel.DEBUG)) {
      logger.log(BasicLevel.DEBUG, "ExpiredNot received, messages will be queued.");
    }
    Notification expiredNot = not.getExpiredNot();
    if (expiredNot instanceof ClientMessages) {
      nbMsgsDeliverSinceCreation -= ((ClientMessages) expiredNot).getMessageCount();
      // If this is the first expired notification, send a PingNot
      if (messages.size() == 0) {
        if (logger.isLoggable(BasicLevel.DEBUG)) {
          logger.log(BasicLevel.DEBUG, "Send a Ping to know when the foreign destination will be reachable.");
        }
        Channel.sendTo(remoteDestinationID, new PingNot());
      }
      try {
        addClientMessages(((ClientMessages) expiredNot), false);
      } catch (AccessException e) {/* never happens*/}
    } else {
      super.handleExpiredNot(from, not);
    }
  }

  protected void doUnknownAgent(UnknownAgent uA) {
    if (uA.not instanceof ClientMessages) {
      logger.log(BasicLevel.ERROR, "Unknown agent: " + remoteDestinationID + ". '" + REMOTE_AGENT_OPTION
          + "' property refers to an unknown agent.");

      nbMsgsDeliverSinceCreation -= ((ClientMessages) uA.not).getMessageCount();
      try {
        addClientMessages(((ClientMessages) uA.not), false);
      } catch (AccessException e) {/* never happens */}
    } else {
      super.doUnknownAgent(uA);
    }
  }

  public String toString() {
    return "AliasQueue:" + getId().toString();
  }

  protected void processSetRight(AgentId user, int right) throws RequestException {
    if (right == READ) {
      throw new RequestException("An alias queue can't be set readable.");
    }
    super.processSetRight(user, right);
  }
  
  public int getEncodableClassId() {
    // Not defined: still not encodable
    return -1;
  }
  
}
