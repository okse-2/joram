package org.objectweb.joram.mom.amqp.proxy.request;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.SyncNotification;

public class BasicAckNot extends SyncNotification {
  
  private int channelId;
  private long deliveryTag;
  private boolean multiple;

  public BasicAckNot(int channelNumber, long deliveryTag, boolean multiple) {
    this.channelId = channelNumber;
    this.deliveryTag = deliveryTag;
    this.multiple = multiple;
  }

  public int getChannelId() {
    return channelId;
  }

  public long getDeliveryTag() {
    return deliveryTag;
  }

  public boolean isMultiple() {
    return multiple;
  }
  
  public void basicAck(AgentId proxyId) throws Exception {
    invoke(proxyId);
  }

  public void Return() {
    Return(null);
  }
}
