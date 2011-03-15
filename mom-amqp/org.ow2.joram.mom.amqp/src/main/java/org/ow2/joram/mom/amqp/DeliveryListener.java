package org.ow2.joram.mom.amqp;

import org.ow2.joram.mom.amqp.structures.Deliver;

/**
 * Interface used for message deliveries.
 */
public interface DeliveryListener {

  public abstract void deliver(Deliver deliver, Queue queue);

}
