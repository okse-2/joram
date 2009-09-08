/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

/**
 * A Queue object encapsulates information to handle a Joram's queue.
 */
public class Queue extends Destination {
  /**
   * Creates a Queue object.
   */
  public Queue(String uid, String name) {
    super(uid, Destination.QUEUE, name);
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   * @param className  The MOM's queue class name.
   * @param prop       The queue properties.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue(
      int serverId,
      String name,
      String className,
      Properties prop) throws JoramException {
    CreateDestinationReply reply = doCreate(serverId, name, className, prop, Destination.QUEUE);
    Queue queue = new Queue(reply.getId(), reply.getName());
    return queue;
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param className  The MOM's queue class name.
   * @param prop       The queue properties.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue(
      int serverId,
      String className,
      Properties prop) throws JoramException {
    return createQueue(serverId, null, className, prop);
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param prop       The queue properties.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue(int serverId, Properties prop) throws JoramException { 
    return createQueue(serverId, "org.objectweb.joram.mom.dest.Queue", prop);
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   * @param name       The name of the queue.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue(int serverId, String name) throws JoramException {
    return createQueue(serverId, 
        name, 
        "org.objectweb.joram.mom.dest.Queue", 
        null);
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param name       The name of the queue.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue(String name) throws JoramException {
    return  createQueue(0, name);
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @param serverId   The identifier of the server where deploying the queue.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue(int serverId) throws JoramException {
    return  createQueue(0, (String) null);
  }

  /**
   *  Admin method creating and deploying (or retrieving) a queue on a
   * given server. First a destination with the specified name is searched
   * on the given server, if it does not exist it is created. In any case,
   * its provider-specific address is returned.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   *
   * @exception JoramException  If the admin connection is closed or broken.
   *                            If the request fails.
   */
  public static Queue createQueue() throws JoramException {
    return  createQueue(0);
  }

}
