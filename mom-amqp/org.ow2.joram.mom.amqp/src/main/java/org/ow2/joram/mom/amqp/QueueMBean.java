package org.ow2.joram.mom.amqp;

import java.util.List;

/**
 * Monitoring interface for AMQP queues.
 */
public interface QueueMBean {

  public String getName();

  public int getConsumerCount();

  public boolean isAutodelete();

  public int getToDeliverMessageCount();

  public int getToAckMessageCount();

  public long getHandledMessageCount();

  public List<String> getBoundExchanges();

  public boolean isDurable();

  public boolean isExclusive();

}
