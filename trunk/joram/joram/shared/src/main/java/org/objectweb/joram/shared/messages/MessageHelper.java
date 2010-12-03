/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.messages;

import org.objectweb.joram.shared.DestinationConstants;

public class MessageHelper {

  // Suppresses default constructor, ensuring non-instantiability.
  private MessageHelper() {
  }

  /**
   * Creates a well formed {@link Message}.
   * 
   * @param msgId ID of the new message
   * @param correlationId correlation ID of the new message
   * @param destId ID of the destination creating the message
   * @param destType type of the destination creating the message:
   *          {@link DestinationConstants#QUEUE_TYPE} or
   *          {@link DestinationConstants#TOPIC_TYPE}
   * @return the new message.
   */
  public static Message createMessage(String msgId, String correlationId, String destId, byte destType) {
    Message message = new Message();
    message.id = msgId;
    message.correlationId = correlationId;
    message.timestamp = System.currentTimeMillis();
    message.setDestination(destId, destType);
    return message;
  }

}
