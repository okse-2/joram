/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;


/**
 * The <code>DeadMQueue</code> class allows administrators to manipulate
 * dead message queues.
 */
public class DeadMQueue extends Queue {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public static boolean isDeadMQueue(String type) {
    return Destination.isAssignableTo(type, DMQ_TYPE);
  }

  public static DeadMQueue createDeadMQueue(String agentId, String name) {
    DeadMQueue dest = new DeadMQueue();
    
    dest.agentId = agentId;
    dest.adminName = name;
    dest.type = DMQ_TYPE;

    return dest;
  }

  /**
   * Admin method creating and deploying a dead message queue on the
   * local server. 
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create() throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId());
  }

  /**
   * Admin method creating and deploying a dead message queue on a given
   * server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId) throws ConnectException, AdminException {
    return create(serverId, (String) null);
  }

  /**
   * Admin method creating and deploying a dead message queue on a given
   * server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId    The identifier of the server where deploying the queue.
   * @param name        The name of the created queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId, String name) throws ConnectException, AdminException {
    DeadMQueue dmq = new DeadMQueue();
    doCreate(serverId, name, DEAD_MQUEUE, null, dmq, DMQ_TYPE);
    return dmq;
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
