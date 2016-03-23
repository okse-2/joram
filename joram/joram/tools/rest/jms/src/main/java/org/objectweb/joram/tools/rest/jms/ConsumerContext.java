/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
package org.objectweb.joram.tools.rest.jms;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.Message;

public class ConsumerContext extends SessionContext {

  private JMSConsumer consumer;
  private ConcurrentHashMap<Long, Message> messages;

  public ConsumerContext(RestClientContext clientCtx) {
    super(clientCtx);
    messages = new ConcurrentHashMap<Long, Message>();
  }

  /**
   * @return the consumer
   */
  public JMSConsumer getConsumer() {
    return consumer;
  }

  /**
   * @param consumer
   *          the consumer to set
   */
  public void setConsumer(JMSConsumer consumer) {
    this.consumer = consumer;
  }

  public long getId(Message message) throws JMSException {
    if (message == null || !messages.containsValue(message))
      return -1;
    for (Entry<Long, Message> entry : messages.entrySet()) {
      if (message.getJMSMessageID().equals(entry.getValue().getJMSMessageID())) {
        return entry.getKey();
      }
    }
    return -1;
  }

  public void put(long id, Message msg) {
    if (msg == null)
      return;
    if (id > getLastId())
      setLastId(id);
    messages.put(id, msg);
  }

  public Message getMessage(long id) {
    return messages.get(id);
  }

  public Message removeMessage(long id) {
    return messages.remove(id);
  }

  public void clear() {
    messages.clear();
  }
}
