/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import org.objectweb.joram.shared.client.ConsumerMessages;

/**
 *
 */
public class SingleSessionConsumer extends MessageConsumerListener {
  
  private Session sess;

  /**
   * 
   */
  SingleSessionConsumer(boolean queueMode,
                        boolean durable,
                        String selector,
                        String destName,
                        String targetName,
                        Session session,
                        MessageListener listener) {
    super(queueMode, durable, selector, destName, targetName,
          listener,
          session.getQueueMessageReadMax(),
          session.getTopicActivationThreshold(),
          session.getTopicPassivationThreshold(),
          session.getTopicAckBufferMax(),
          session.getRequestMultiplexer());
    sess = session;
  }
  
  public void pushMessages(ConsumerMessages cm) throws JMSException {
    sess.pushMessages(this, cm);
  }
  
  public void onMessage(Message msg, MessageListener listener, int ackMode) 
    throws JMSException {
    throw new Error("Invalid call");
  }

  @Override
  protected boolean checkSessionThread() {
    return sess.checkThread();
  }
}
