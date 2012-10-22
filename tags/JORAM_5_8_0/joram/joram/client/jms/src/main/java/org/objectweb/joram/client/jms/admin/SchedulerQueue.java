/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;

import org.objectweb.joram.client.jms.Queue;

/**
 * The <code>SchedulerQueue</code> class allows administrators to create scheduled queues.
 * <p>
 * A scheduled queue is a standard JMS queue extended with a timer behavior. When a scheduler
 * queue receives a message with a property called 'scheduleDate' (typed as a long) then the
 * message is not available for delivery before the date specified by the property.
 */
public class SchedulerQueue {
  /**
   * Administration method creating and deploying a scheduled queue on the local server.
   * <p>
   * The request fails if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   * 
   * @param name  The name of the created queue.
   * @return the created destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see #create(int, String)
   */
  public static Queue create(String name) throws ConnectException, AdminException {
    return create(AdminModule.getLocalServerId(), name);
  }

  /**
   * Administration method creating and deploying a scheduled queue on a given server.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the queue.
   * @param name      The name of the created queue.
   * @return the created  destination.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public static Queue create(int serverId,
                             String name) throws ConnectException, AdminException {
    Queue queue = Queue.create(serverId, name, Queue.SCHEDULER_QUEUE, null);
    return queue;
  }
}
