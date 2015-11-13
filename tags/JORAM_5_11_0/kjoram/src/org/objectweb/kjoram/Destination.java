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
 * A Destination object encapsulates a Joram's specific address.
 */
public abstract class Destination {
  private String uid;
  private byte type;
  private String name;

  public final static byte NOTYPE = 0;
  public final static byte QUEUE = 1;
  public final static byte TOPIC = 2;

  public Destination(byte type) {
    this.type = type;
  }
  
  public Destination(String uid, byte type, String name) {
    this.uid = uid;
    this.type = type;
    this.name = name;
  }

  /**
   * Returns the unique internal name of the destination.
   */
  public String getUID() {
    return uid;
  }

  /**
   * Returns the administrative name of the destination.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns true if the destination is a queue.
   */
  public boolean isQueue() {
    return (type == QUEUE);
  }

  /**
   * Returns true if the destination is a topic.
   */
  public boolean isTopic() {
    return (type == TOPIC);
  }

  /**
   * Non API method, should be hidden.
   */
  public byte getType() {
    return type;
  }

  /**
   * Non API method, should be hidden.
   */
  public static String typeToString(byte type) throws JoramException {
    if (type == QUEUE)
      return "queue";
    else if (type == TOPIC)
      return "topic";
    else if (type == NOTYPE)
      return null;
    throw new JoramException();
  }

  /**
   * Non API method, should be hidden.
   */
  public static byte stringToType(String type) throws JoramException {
    if ("queue".equals(type))
      return QUEUE;
    else if ("topic".equals(type))
      return TOPIC;
    else if (type == null)
      return NOTYPE;
    throw new JoramException();
  }

  /**
   * Non API method, should be hidden.
   */
  protected static Destination newInstance(String uid,
                                           byte type,
                                           String name) throws JoramException {
    if (type == QUEUE) {
      return new Queue(uid, name);
    } else if (type == TOPIC) {
      return new Topic(uid, name);
    }
    throw new JoramException();
  }
  
  public static CreateDestinationReply doCreate(
      int serverId, 
      String name,
      String className, 
      Properties prop, 
      byte type) throws JoramException {
    CreateDestinationRequest cdr =
      new CreateDestinationRequest(serverId,
          name,
          className,
          prop,
          Destination.typeToString(type));
    AdminReply reply = AdminModule.doRequest(cdr);
    return (CreateDestinationReply) reply;
  }
  
  public String toString() {
    String t = null;
    try {
      t = typeToString(type);
    } catch (JoramException e) {}
    return t + ": " + name + '(' + uid + ')';
  }
}
