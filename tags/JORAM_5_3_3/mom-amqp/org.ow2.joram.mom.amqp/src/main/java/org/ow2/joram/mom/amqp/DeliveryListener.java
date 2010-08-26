package org.ow2.joram.mom.amqp;

public interface DeliveryListener {

  public abstract void deliver(Deliver deliver, Queue queue);

}
