package org.ow2.joram.mom.amqp;

import java.util.Set;

/**
 * Monitoring interface for AMQP exchanges.
 */
public interface IExchangeMBean {

  public String getName();

  public Set<String> getBoundQueues();

  public boolean isDurable();

  public String getType();

  public long getHandledMessageCount();

  public long getPublishedMessageCount();

}
