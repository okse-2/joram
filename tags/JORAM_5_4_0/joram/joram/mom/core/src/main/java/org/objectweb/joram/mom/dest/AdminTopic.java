/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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

import org.objectweb.joram.mom.notifications.AdminReplyNot;
import org.objectweb.joram.mom.notifications.FwdAdminRequestNot;
import org.objectweb.joram.mom.notifications.GetProxyIdListNot;
import org.objectweb.joram.mom.notifications.GetProxyIdNot;
import org.objectweb.joram.mom.proxies.AdminNotification;
import org.objectweb.joram.shared.excepts.RequestException;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Notification;

/**
 * An <code>AdminTopic</code> agent is a MOM administration service, which
 * Behavior is provided by an <code>AdminTopicImpl</code> instance.
 *
 * @see AdminTopicImpl
 */
public class AdminTopic extends Topic {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs an <code>AdminTopic</code> agent. 
   */ 
  public AdminTopic() throws RequestException {
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
  public final static AgentId getDefault() {
    if (adminId == null)
      adminId = new AgentId(AgentServer.getServerId(),
                            AgentServer.getServerId(),
                            AgentId.JoramAdminStamp);
    return adminId;
  }
  
  /**
   * Returns true if the given AgentId is the unique identifier of an AdminTopic agent.
   * 
   * @param id  the AgentId to verify.
   * @return    true if the given AgentId is the unique identifier of an AdminTopic agent.
   */
  public final static boolean isAdminTopicId(AgentId id) {
    if (id == null) return false;
    return id.getStamp() == AgentId.JoramAdminStamp;
  }
  
  /**
   * Distributes the received notifications to the appropriate reactions.
   * @throws Exception 
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "--- " + this + ": got " + not + " from: " + from.toString());

    // state change, so save.
    setSave();

    if (not instanceof AdminNotification) {
      // This notification is used at boot by an administrator proxy to declare
      // its identification.
      ((AdminTopicImpl)destImpl).AdminNotification((AdminNotification) not);
    } else if (not instanceof FwdAdminRequestNot) {
      // This notification contains forwarded administration request from remote
      // AdminTopic. This code overloads the normal behavior in Destination.

      // AF (TODO): May be we should verify that from is an AdminTopic but in the
      // AgentServer sandbox only an AdminTopic can generate such a notification.
      ((AdminTopicImpl)destImpl).processAdminRequests(((FwdAdminRequestNot) not).getReplyTo(),
                                                      ((FwdAdminRequestNot) not).getRequestMsgId(),
                                                      ((FwdAdminRequestNot) not).getRequest(),
                                                      from);
    } else if (not instanceof AdminReplyNot) {
      // Now most of the replies are sent directly to the waiting destination. Only
      // the GetRightsReplyNot requires a processing, in future it will be removed.
      ((AdminTopicImpl)destImpl).AdminReply((AdminReplyNot) not);
    } else if (not instanceof GetProxyIdNot) {
      // This notification (SyncNotification) is used during connection to get the
      // AgentId of the user's proxy.
      ((AdminTopicImpl)destImpl).GetProxyIdNot((GetProxyIdNot)not);
    } else if (not instanceof GetProxyIdListNot) {
      // This notification is needed by the HA mode.
      // AF (TODO): remove it.
      ((AdminTopicImpl)destImpl).GetProxyIdListNot((GetProxyIdListNot)not);
    } else {
      super.react(from, not);
    }
  }
}