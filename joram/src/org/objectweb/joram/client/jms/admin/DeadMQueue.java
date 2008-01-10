/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s):
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.management.MXWrapper;


/**
 * The <code>DeadMQueue</code> class allows administrators to manipulate
 * dead message queues.
 */
public class DeadMQueue extends Queue {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final static String DMQ_TYPE = "queue.dmq";

  public static boolean isDeadMQueue(String type) {
    return Destination.isAssignableTo(type, DMQ_TYPE);
  }

  /**
   * Admin method creating and deploying a dead message queue on a given
   * server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId) throws ConnectException, AdminException {
    return create(serverId, (String) null);
  }

  public static Queue create(int serverId, String name) throws ConnectException, AdminException {
    DeadMQueue dmq = new DeadMQueue();
    doCreate(serverId,
             name,
             "org.objectweb.joram.mom.dest.DeadMQueue",
             null,
             dmq,
             DMQ_TYPE);
    
    StringBuffer buff = new StringBuffer();
    buff.append("type=").append(DMQ_TYPE);
    buff.append(",name=").append(name);
    try {
      MXWrapper.registerMBean(dmq, "joramClient", buff.toString());
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG, "registerMBean", e);
    }
    return dmq;
  }

  /**
   * Admin method creating and deploying a dead message queue on the
   * local server. 
   * <p>
   * The request fails if the destination deployment fails server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create() throws ConnectException, AdminException
  {
    return create(AdminModule.getLocalServerId());
  }

  // Used by jndi2 SoapObjectHelper
  public DeadMQueue() {}
  
  public DeadMQueue(String name) {
    super(name, DMQ_TYPE);
  }

  public String toString() {
    return "DeadMQueue:" + agentId;
  }
}
