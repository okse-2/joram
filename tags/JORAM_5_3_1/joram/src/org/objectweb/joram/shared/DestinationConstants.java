/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
public class DestinationConstants {
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
}
