/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.messages;

import org.objectweb.joram.mom.util.JoramHelper;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.Encoder;

public class MessageTxId implements Encodable {
  
  private AgentId ownerId;
  
  private long order;
  
  public MessageTxId() {}

  public MessageTxId(AgentId ownerId, long order) {
    super();
    this.ownerId = ownerId;
    this.order = order;
  }

  public AgentId getOwnerId() {
    return ownerId;
  }

  public long getOrder() {
    return order;
  }

  public int getClassId() {
    return JoramHelper.MESSAGETXID_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    return ownerId.getEncodedSize() + 8;
  }

  public void encode(Encoder encoder) throws Exception {
    ownerId.encode(encoder);
    encoder.encodeUnsignedLong(order);
  }

  public void decode(Decoder decoder) throws Exception {
    ownerId = new AgentId((short) 0, (short) 0, 0);
    ownerId.decode(decoder);
    order = decoder.decodeUnsignedLong();
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(18);
    buf.append('M').append(ownerId.toString()).append('_');
    buf.append(order);
    return buf.toString();
  }
  
  public static MessageTxId fromString(String s) throws Exception {
    int sepIndex = s.indexOf('_');
    if (sepIndex < 0) throw new Exception("Malformed MessageTxId: " + s);
    long order = Long.parseLong(s.substring(sepIndex + 1));
    AgentId ownerId = AgentId.fromString(s.substring(1, sepIndex));
    return new MessageTxId(ownerId, order);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (order ^ (order >>> 32));
    result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MessageTxId other = (MessageTxId) obj;
    if (order != other.order)
      return false;
    if (ownerId == null) {
      if (other.ownerId != null)
        return false;
    } else if (!ownerId.equals(other.ownerId))
      return false;
    return true;
  }
  
  public static class MessageTxIdFactory implements EncodableFactory {

    public Encodable createEncodable() {
      return new MessageTxId();
    }
    
  }

}
