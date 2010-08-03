/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared;

/**
 * Defines constants needed to distinguish Queue and Topic. 
 */
public final class DestinationConstants {
  /** the destination is a Topic */
  public final static byte TOPIC_TYPE = 0x01;
  /** the destination is a Queue */
  public final static byte QUEUE_TYPE = 0x02;
  /** the destination is temporary */
  public final static byte TEMPORARY = 0x10;

  /** the destination is a Queue or a Topic */
  private final static byte DESTINATION_TYPE = TOPIC_TYPE |QUEUE_TYPE;
  
  public final static boolean compatible(byte type1, byte type2) {
    return (type1 & DESTINATION_TYPE) == (type2 & DESTINATION_TYPE);
  }
  
  public final static boolean isQueue(byte type) {
    return ((type & QUEUE_TYPE) != 0);
  }
  
  public final static boolean isTopic(byte type) {
    return ((type & TOPIC_TYPE) != 0);
  }
  
  public final static boolean isTemporary(byte type) {
    return ((type & TEMPORARY) != 0);
  }
  
  public final static byte getQueueType() {
    return QUEUE_TYPE;
  }
  
  public final static byte getTopicType() {
    return TOPIC_TYPE;
  }

  public final static byte getTemporaryQueueType() {
    return (QUEUE_TYPE | TEMPORARY);
  }

  public final static byte getTemporaryTopicType() {
    return (TOPIC_TYPE | TEMPORARY);
  }

  /**
   * Check the specified destination identifier.
   * 
   * @exception Exception if an invalid destination identifier is specified.
   */
  public static final void checkId(String id) throws Exception {
    if (id == null)
      throw new Exception("Undefined (null) destination identifier.");
    
    if (id.matches("#\\d+\\.\\d+\\.\\d+")) return;
    
    throw new Exception("Bad destination identifier:" + id);
  }

  public static final String getNullId(int serverId) {
    StringBuilder strbuf = new StringBuilder(10);
    strbuf.append("#").append(serverId).append('.').append(serverId).append(".0");
    return strbuf.toString();
  }

  public static final String getAdminTopicId(int serverId) {
    StringBuilder strbuf = new StringBuilder(10);
    strbuf.append("#").append(serverId).append('.').append(serverId).append('.').append(".10");
    return strbuf.toString();
  }
}
