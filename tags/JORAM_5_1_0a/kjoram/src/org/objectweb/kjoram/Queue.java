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

  public static Queue createQueue(int serverId,
      String name,
      String className,
      Properties prop) throws JoramException {
    CreateDestinationReply reply = doCreate(serverId, name, className, prop, Destination.QUEUE);
    Queue queue = new Queue(reply.getId(), reply.getName());
    return queue;
  }

  public static Queue createQueue(int serverId,
      String className,
      Properties prop) throws JoramException {
    return createQueue(serverId, null, className, prop);
  }

  public static Queue createQueue(int serverId, Properties prop) throws JoramException { 
    return createQueue(serverId, "org.objectweb.joram.mom.dest.Queue", prop);
  }

  public static Queue createQueue(int serverId, String name) throws JoramException {
    return createQueue(serverId, 
        name, 
        "org.objectweb.joram.mom.dest.Queue", 
        null);
  }

  public static Queue createQueue(String name) throws JoramException {
    return  createQueue(0, name);
  }

  public static Queue createQueue(int serverId) throws JoramException {
    return  createQueue(0, (String) null);
  }

  public static Queue createQueue() throws JoramException {
    return  createQueue(0);
  }

}
