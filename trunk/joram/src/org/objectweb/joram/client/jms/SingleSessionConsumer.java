/*
 * Created on 24 mai 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.MessageListener;

import org.objectweb.joram.client.jms.connection.RequestMultiplexer;
import org.objectweb.joram.shared.client.ConsumerMessages;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * @author feliot
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SingleSessionConsumer extends MessageConsumerListener {
  
  private Session sess;

  /**
   * 
   */
  SingleSessionConsumer(
      boolean queueMode,
      boolean durable,
      String selector,
      String targetName,
      Session session,
      MessageListener listener,
      int queueMessageReadMax, 
      int topicActivationThreshold, 
      int topicPassivationThreshold, 
      int topicAckBufferMax, 
      RequestMultiplexer reqMultiplexer) {
    super(queueMode, durable, selector, targetName,
        listener, queueMessageReadMax,
        topicActivationThreshold, 
        topicPassivationThreshold, topicAckBufferMax,
        reqMultiplexer);
    sess = session;
  }
  
  
  public void pushMessages(ConsumerMessages cm) throws JMSException {
    sess.pushMessages(this, cm);
  }
  
  public void onMessage(Message msg, MessageListener listener, int ackMode) 
    throws JMSException {
    throw new Error("Invalid call");
  }
}
