package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.MessageListener;

public class JMSConsumer implements javax.jms.JMSConsumer {
  private MessageConsumer consumer;

  /**
   * API method
   */
  public JMSConsumer(MessageConsumer consumer) {
    this.consumer = consumer;
  }

  /**
   * API method
   */
  public void close() {
    try {
      consumer.close();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to close JMSConsumer", e.getMessage(), e);
    }
  }

  /**
   * API method
   */
  public MessageListener getMessageListener() throws JMSRuntimeException {
    try {
      return consumer.getMessageListener();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to get message listener", e.getMessage(), e);
    }
  }

  /**
   * API method
   */
  public String getMessageSelector() {
    try {
      return consumer.getMessageSelector();
    } catch (JMSException e) {
      throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
    }
  }

  /**
   * API method
   */
  public Message receive() {
    Message message = null;
    try {
      message = consumer.receive();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to receive message", e.getMessage(), e);
    }
    return message;
  }

  /**
   * API method
   */
  public Message receive(long timeout) {
    Message message = null;
    try {
      message = consumer.receive(timeout);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to receive message", e.getMessage(), e);
    }
    return message;
  }

  /**
   * API method
   */
  public <T> T receiveBody(Class<T> c) {
    try {
      Message message = receive();
      if (message != null) {
        T body = message.getBody(c);
        if (body == null) throw new MessageFormatRuntimeException("Message has no body.");
        return body;
      }
      return null;
    } catch (MessageFormatException e) {
      throw new MessageFormatRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new MessageFormatRuntimeException("Unable to get message body", e.getMessage(), e);
    }
  }

  /**
   * API method
   */
  public <T> T receiveBody(Class<T> c, long timeout) {
    try {
      Message message = receive(timeout);
      if (message != null) {
        T body = message.getBody(c);
        if (body == null) throw new MessageFormatRuntimeException("Message has no body.");
        return body;
      }
      return null;
    } catch (MessageFormatException e) {
      throw new MessageFormatRuntimeException(e.getMessage());
    } catch (JMSException e) {
      throw new MessageFormatRuntimeException("Unable to get message body", e.getMessage(), e);
    }
  }

  /**
   * API method
   */
  public <T> T receiveBodyNoWait(Class<T> c) {
    Message m = receiveNoWait();
    if (m != null)
      try {
        return m.getBody(c);
      } catch (JMSException e) {
        throw new MessageFormatRuntimeException("Unable to get message body", e.getMessage(), e);
      }
    return null;
  }

  /**
   * API method
   */
  public Message receiveNoWait() {
    Message message = null;
    try {
      message = consumer.receiveNoWait();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to receive message", e.getMessage(), e);
    }
    return message;
  }

  /**
   * API method
   */
  public void setMessageListener(MessageListener listener) throws JMSRuntimeException {
    try {
      consumer.setMessageListener(listener);
    } catch (JMSException e) {
      throw new JMSRuntimeException("Unable to set message listener", e.getMessage(), e);
    }
  }
}
