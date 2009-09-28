package joram.carrieriq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * Default JMS MessageListener for the MessageBus.
 * 
 * Allows user to register listeners for particular object types
 * 
 * Will call the ObjectListener's setMessage() before onMessage() if it
 * implements the MessageAware interface
 * 
 * @see com.carrieriq.platform.messagebus.MessageAware
 */
public class ObjectMessageListenerImpl implements ObjectMessageListener {
  private ObjectListener objectListener = null;

  public void setObjectListener(ObjectListener objectListener) {
    this.objectListener = objectListener;
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
  }
}
