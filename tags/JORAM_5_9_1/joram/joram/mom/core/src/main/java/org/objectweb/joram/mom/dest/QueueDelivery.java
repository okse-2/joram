/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.dest;

import java.io.Serializable;

import org.objectweb.joram.mom.messages.Message;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.Encoder;

public class QueueDelivery implements Encodable, Serializable {
  
  private AgentId consumerId;
  
  private int contextId;
  
  private transient Message message;

  public QueueDelivery() {}
  
  public QueueDelivery(AgentId consumerId, int contextId, Message message) {
    super();
    this.consumerId = consumerId;
    this.contextId = contextId;
    this.message = message;
  }

  public AgentId getConsumerId() {
    return consumerId;
  }

  public int getContextId() {
    return contextId;
  }

  public Message getMessage() {
    return message;
  }

  public void setMessage(Message message) {
    this.message = message;
  }

  public int getEncodableClassId() {
    // Not Assigned
    return -1;
  }

  public int getEncodedSize() throws Exception {
    return consumerId.getEncodedSize() + 4;
  }

  public void encode(Encoder encoder) throws Exception {
    consumerId.encode(encoder);
    encoder.encode32(contextId);
  }

  public void decode(Decoder decoder) throws Exception {
    consumerId = new AgentId((short) 0, (short) 0, 0);
    consumerId.decode(decoder);
    contextId = decoder.decode32();
  }

}
