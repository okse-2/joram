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

import org.objectweb.joram.client.jms.Queue;

import java.net.ConnectException;


/**
 * The <code>DeadMQueue</code> class allows administrators to manipulate
 * dead message queues.
 */
public class DeadMQueue extends org.objectweb.joram.client.jms.Queue
{
  /**
   * Constructs a <code>DeadMQueue</code> instance.
   *
   * @param agentId  Identifier of the dead message queue agent.
   */
  public DeadMQueue(String agentId)
  {
    super(agentId);
  }

  /**
   * Constructs a <code>DeadMQueue</code> instance.
   *
   * @param agentId  Identifier of the dead message queue agent.
   * @param name     Name set by administrator.
   */
  public DeadMQueue(String agentId, String name)
  {
    super(agentId, name);
  }

  public String toString()
  {
    return "DeadMQueue:" + agentId;
  }


  /**
   * Admin method creating and deploying a dead message queue on a given
   * server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId)
                throws ConnectException, AdminException
  {
    String queueId =  doCreate(serverId,
                              null,
                              "org.objectweb.joram.mom.dest.DeadMQueue",
                              null);
    return new DeadMQueue(queueId);
  }

  /**
   * Admin method creating and deploying a dead message queue on the
   * local server. 
   * <p>
   * The request fails if the destination deployement fails server side.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create() throws ConnectException, AdminException
  {
    return create(AdminModule.getLocalServer());
  }
}
