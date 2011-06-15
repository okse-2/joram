/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class used to hold all the contextual data relative to one channel.
 */
public class ChannelContext {

  static class Delivery {

    long queueMsgId;

    long deliveryTag;

    QueueShell queue;

    boolean waitingCommit;

    public Delivery(long deliverytag, long queueMsgId, QueueShell queue) {
      this.deliveryTag = deliverytag;
      this.queueMsgId = queueMsgId;
      this.queue = queue;
      waitingCommit = false;
    }

    public String toString() {
      return "Delivery [queueMsgId=" + queueMsgId + ", deliveryTag=" + deliveryTag + ", queue=" + queue
          + ", waitingCommit=" + waitingCommit + "]";
    }
  }

  /**
   * The name of the last queue created on this channel, used when queue name is
   * not specified on BasicGet or QueueBind.
   */
  String lastQueueCreated;

  /**
   * The configured maximum prefetched message count. 0 means no limit.
   */
  int prefetchCount = 0;

  /**
   * Keeps the deliveries waiting for an acknowledgment.
   */
  List<Delivery> deliveriesToAck = new LinkedList<Delivery>();

  /**
   * Maps a consumer tag to a queue. A Least Recently Used map is necessary to
   * get new deliveries on a round-robin basis when an acknowledgment is
   * received.
   */
  Map<String, QueueShell> consumerQueues = new LinkedHashMap(16, 0.75f, true);

  /**
   * Tells if the channel is in transacted mode.
   */
  boolean transacted = false;

  /**
   * The publish requests waiting the Tx.Commit command.
   */
  List<PublishRequest> pubToCommit = new ArrayList<PublishRequest>();

  /**
   * Id used to generate a channel-unique deliveryTag;
   */
  private long deliveryTagCounter;
  
  /**
   * Id used to generate a channel-unique consumerTag.
   */
  long consumerTagCounter;

  public long nextDeliveryTag() {
    deliveryTagCounter++;
    return deliveryTagCounter;
  }

}
