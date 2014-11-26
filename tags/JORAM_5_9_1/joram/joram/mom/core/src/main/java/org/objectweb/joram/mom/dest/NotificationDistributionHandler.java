/*
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
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.common.Debug;

/**
 * A distribution handler that distributed message to a given destination using
 * an A3 acquisition notification.
 * 
 * @author willy malvault (willy.malvault@scalagent.fr)
 * @see AcquisitionNot
 */
public class NotificationDistributionHandler implements DistributionHandler {
  public static Logger logger = Debug.getLogger(NotificationDistributionHandler.class.getName());

  /** the queue to send the notification to */
  private AgentId remoteDestinationID = null;

  /** @see DistributionHandler#init(Properties) */
  public void init(Properties properties, boolean firstTime) {
    remoteDestinationID = AgentId.fromString(properties.getProperty("remoteAgentID"));

    if (logger.isLoggable(BasicLevel.INFO))
      logger.log(BasicLevel.INFO, "--- " + this + " notification distribution handler bind to destination ("
          + remoteDestinationID + ")");
  }

  /** @see DistributionHandler#distribute(Message) */
  public void distribute(Message message) throws Exception {
    /* building a ClientMessages object for the notification */
    ClientMessages cm = new ClientMessages();
    cm.addMessage(message);

    /* building a notification that inherits messages properties (id and
     * persistent) */
    AcquisitionNot an = new AcquisitionNot(cm, message.persistent, message.id);

    if (remoteDestinationID == null)
      throw new Exception("Remote \"alias destination\" A3 agent identifier is null. Did you set the property \"remoteAgentID\" of the distribution destination?");

    /* sending the notification */
    Channel.sendTo(remoteDestinationID, an);
  }

  /** @see AcquisitionHandler#close() */
  public void close() {
    // Nothing to do
  }
}
