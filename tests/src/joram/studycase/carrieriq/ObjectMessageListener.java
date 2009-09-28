package joram.carrieriq;

import javax.jms.Message;
import javax.jms.MessageListener;

public interface ObjectMessageListener extends MessageListener {
  public void setObjectListener(ObjectListener objectListener);
  public void onMessage(Message m);
}
