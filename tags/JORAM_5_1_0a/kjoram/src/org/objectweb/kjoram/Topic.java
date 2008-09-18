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
public class Topic extends Destination {
  
  public Topic() {
    super(Destination.TOPIC);
  }
  
  /**
   * Creates a Queue object.
   */
  public Topic(String uid, String name) {
    super(uid, Destination.TOPIC, name);
  }
  
  public static Topic createTopic(int serverId,
      String name,
      String className,
      Properties prop) throws JoramException {
    CreateDestinationReply reply = doCreate(serverId, name, className, prop, Destination.TOPIC);
    Topic topic = new Topic(reply.getId(), reply.getName());
    return topic;
  }

  public static Topic createTopic(int serverId,
      String className,
      Properties prop) throws JoramException {
    return createTopic(serverId, null, className, prop);
  }

  public static Topic createTopic(int serverId, Properties prop) throws JoramException { 
    return createTopic(serverId, "org.objectweb.joram.mom.dest.Topic", prop);
  }

  public static Topic createTopic(int serverId, String name) throws JoramException {
    return createTopic(serverId, 
        name, 
        "org.objectweb.joram.mom.dest.Topic", 
        null);
  }

  public static Topic createTopic(String name) throws JoramException {
    return  createTopic(0, name);
  }

  public static Topic createTopic(int serverId) throws JoramException {
    return  createTopic(0, (String) null);
  }

  public static Topic createTopic() throws JoramException {
    return  createTopic(0);
  }

}
