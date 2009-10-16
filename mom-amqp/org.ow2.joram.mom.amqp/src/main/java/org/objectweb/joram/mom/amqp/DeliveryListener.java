package org.objectweb.joram.mom.amqp;

public interface DeliveryListener {

  public abstract void deliver(Deliver deliver, Queue queue);

}
