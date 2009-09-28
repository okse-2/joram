package joram.carrieriq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class ConverterMessageListenerImpl implements ObjectMessageListener {
  private ObjectListener objectListener = null;

  public void setObjectListener(ObjectListener objectListener) {
    this.objectListener = objectListener;
  }

  Producer producer = null;

  public void setMessageProducer(Producer producer) {
    this.producer = producer;
  }

  public void onMessage(Message m) {
    System.out.println("Received message: " + m);

    if (!(m instanceof ObjectMessage)) {
      // We won't ever be able to process it so log it and discard it
      System.out.println("Don't know how to handle message of type "
                         + m.getClass().getName() + ". Discarding");
      return;
    }

    Object payload;
    try {
      payload = ((ObjectMessage) m).getObject();
    } catch (JMSException e) {
      System.out.println("Error obtaining payload of message");
      return;
    }

    try {
      objectListener.onObject(payload);
    } catch (ForceRollbackRuntimeException e) {
      System.out.println("Rollback requested: " + e);
      System.out.println(e);
      throw e;
    } catch (Throwable e) {
      // Be sure to log any errors that get by. Also don't pass exceptions
      // out of this method because the container won't know what to do
      // with them.
      System.out.println("Swallowing throwable of type " + e);
      System.out.println(e);
    }

    try {
      producer.send(payload);
    } catch (MessageConversionException e) {
      System.out.println("Error converting message " + e);
      System.out.println(e);
    } catch (JMSException e) {
      // Be sure to log any errors that get by. Also don't pass exceptions
      // out of this method because the container won't know what to do
      // with them.
      System.out.println("Error forwarding message " + e);
      System.out.println(e);
    }
  }
}
