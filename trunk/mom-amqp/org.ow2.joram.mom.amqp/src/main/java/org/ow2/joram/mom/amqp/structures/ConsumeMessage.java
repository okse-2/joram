/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp.structures;

import java.io.Serializable;

/**
 * A {@link ConsumeMessage} is sent to the proxy on <code>Basic.Consume</code>
 * method, in order to request a message on the specified queue. This message
 * will be requeued as long as there are more messages on the queue.
 */
public class ConsumeMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  public String queueName;

  public String consumerTag;

  public int channelNumber;

  public boolean noAck;

  public short consumerServerId;

  public ConsumeMessage(String queueName, String consumerTag, int channelNumber, boolean noAck) {
    super();
    this.queueName = queueName;
    this.consumerTag = consumerTag;
    this.channelNumber = channelNumber;
    this.noAck = noAck;
  }

  public ConsumeMessage(String queueName, String consumerTag, int channelNumber, boolean noAck,
      short consumerServerId) {
    super();
    this.queueName = queueName;
    this.consumerTag = consumerTag;
    this.channelNumber = channelNumber;
    this.noAck = noAck;
    this.consumerServerId = consumerServerId;
  }


}
