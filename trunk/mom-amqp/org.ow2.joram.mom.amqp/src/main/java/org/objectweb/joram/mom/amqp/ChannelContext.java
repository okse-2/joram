package org.objectweb.joram.mom.amqp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChannelContext {

  public static class Delivery {

    long queueMsgId;

    long deliveryTag;

    QueueShell queue;

    public Delivery(long deliverytag, long queueMsgId, QueueShell queue) {
      this.deliveryTag = deliverytag;
      this.queueMsgId = queueMsgId;
      this.queue = queue;
    }
  }

  /**
   * The name of the last queue created on this channel, used when queue name is
   * not specified on BasicGet or QueueBind.
   */
  String lastQueueCreated;

  /**
   * Maps a delivery tag to a delivery. Keeps the deliveries waiting for an
   * acknowledgment.
   */
  List<Delivery> deliveriesToAck = new LinkedList<Delivery>();

  /**
   * Maps a consumer tag to a queue.
   */
  Map<String, QueueShell> consumerQueues = new HashMap<String, QueueShell>();
  
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
