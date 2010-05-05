/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2003 - 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

import org.objectweb.joram.mom.dest.AdminTopicImpl.AdminRequestNot;
import org.objectweb.joram.mom.notifications.AdminReply;
import org.objectweb.joram.mom.notifications.GetProxyIdListNot;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.notifications.RegisterDestNot;
import org.objectweb.joram.mom.notifications.RegisterTmpDestNot;
import org.objectweb.joram.mom.notifications.RegisteredDestNot;
import org.objectweb.joram.mom.proxies.AdminNotification;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Notification;

/**
 * An <code>AdminTopic</code> agent is a MOM administration service, which
 * behaviour is provided by an <code>AdminTopicImpl</code> instance.
 *
 * @see AdminTopicImpl
 */
public class AdminTopic extends Topic {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>AdminTopic</code> agent. 
   */ 
  public AdminTopic() {
    super("JoramAdminTopic", true, AgentId.JoramAdminStamp);
    init(null, null);
  }

  /**
   * Creates the <tt>TopicImpl</tt>.
   *
   * @param adminId  Identifier of the topic administrator.
   * @param prop     The initial set of properties.
   */
  public DestinationImpl createsImpl(AgentId adminId, Properties prop) {
    return new AdminTopicImpl(getId());
  }

  /**
   * Gets the identifier of the default administration topic on a given server.
   */
  public static AgentId getDefault(short serverId) {
    return new AgentId(serverId, serverId, AgentId.JoramAdminStamp);
  }

  static AgentId adminId = null;

  /**
   * Gets the identifier of the default administration topic on the
   * current server.
   */
  public static AgentId getDefault() {
    if (adminId == null)
      adminId = new AgentId(AgentServer.getServerId(),
                            AgentServer.getServerId(),
                            AgentId.JoramAdminStamp);
    return adminId;
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this
                                   + ": got " + not
                                   + " from: " + from.toString());

    // state change, so save.
    setSave();

    if (not instanceof AdminNotification)
      ((AdminTopicImpl)destImpl).AdminNotification(from, (AdminNotification) not);
    else if (not instanceof AdminRequestNot)
      ((AdminTopicImpl)destImpl).AdminRequestNot(from, (AdminRequestNot) not);
    else if (not instanceof org.objectweb.joram.mom.notifications.AdminReply)
      ((AdminTopicImpl)destImpl).AdminReply(from, (AdminReply) not);
    else if (not instanceof GetProxyIdNot)
      ((AdminTopicImpl)destImpl).GetProxyIdNot((GetProxyIdNot)not);
    else if (not instanceof GetProxyIdListNot)
      ((AdminTopicImpl)destImpl).GetProxyIdListNot((GetProxyIdListNot)not);
    else if (not instanceof RegisterTmpDestNot)
      ((AdminTopicImpl)destImpl).RegisterTmpDestNot((RegisterTmpDestNot)not);
    else if (not instanceof RegisterDestNot)
      ((AdminTopicImpl)destImpl).RegisterDestNot((RegisterDestNot)not);
    else if (not instanceof RegisteredDestNot)
      ((AdminTopicImpl)destImpl).RegisteredDestNot(from, (RegisteredDestNot)not);
    else
      super.react(from, not);
  }
}
