/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package org.objectweb.joram.mom.notifications;

import org.objectweb.joram.mom.util.JoramHelper;

import fr.dyade.aaa.agent.CallbackNotification;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;
import fr.dyade.aaa.common.encoding.Encoder;


/**
 * A <code>TopicForwardNot</code> is a notification sent by a topic to 
 * another topic part of the same cluster, or to its hierarchical father,
 * and holding a forwarded <code>ClientMessages</code> notification.
 */
public class TopicForwardNot extends CallbackNotification {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  /**
   * <code>true</code> if the notification is destinated to a hierarchical
   * father.
   */
  public boolean fromCluster;

  /** The forwarded messages. */
  public ClientMessages messages;
  
  public TopicForwardNot() {}

  /**
   * Constructs a <code>TopicForwardNot</code> instance.
   * 
   * @param messages Notification to forward.
   * @param fromCluster <code>true</code> if the notification is coming
   *          from a cluster friend.
   */
  public TopicForwardNot(ClientMessages messages, boolean fromCluster) {
    this.messages = messages;
    this.fromCluster = fromCluster;
  }
  
  public void setPersistent(boolean persistent) {
    this.persistent = persistent;
  }
  
  @Override
  public int getEncodableClassId() {
    return JoramHelper.TOPIC_FWD_NOT_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    int res = super.getEncodedSize() ;
    res += BOOLEAN_ENCODED_SIZE + INT_ENCODED_SIZE;
    res += messages.getEncodedSize();
    return res;
  }
  
  public void encode(Encoder encoder) throws Exception {
    super.encode(encoder);
    encoder.encodeBoolean(fromCluster);
    
    // Polymorphism may be used
    encoder.encode32(messages.getEncodableClassId());
    messages.encode(encoder);
  }

  public void decode(Decoder decoder) throws Exception {
    super.decode(decoder);
    fromCluster = decoder.decodeBoolean();
    
    int factoryId = decoder.decode32();
    if (factoryId == JoramHelper.CLIENT_MESSAGES_CLASS_ID) {
      messages = new ClientMessages();
    } else {
      // Polymorphism
      // TODO: a cache could be used
      EncodableFactory factory = EncodableFactoryRepository.getFactory(factoryId);
      messages = (ClientMessages) factory.createEncodable();
    }
    messages.decode(decoder);
  }
  
  public StringBuffer toString(StringBuffer output) {
    output.append('(');
    super.toString(output);
    output.append(",fromCluster=").append(fromCluster);
    output.append(",messages=").append(messages);
    output.append(')');

    return output;
  }
  
  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new TopicForwardNot();
    }

  }
  
}
