package org.objectweb.joram.client.jms;

import java.io.Serializable;

import javax.jms.TransactionInProgressRuntimeException;

public class XAJMSContext implements javax.jms.XAJMSContext {
  /** Embedded JMSContext object associated with this XAJMSContext. */
  private JMSContext context = null;
  /** The XA resource representing the session to the transaction manager. */
  private XAResource xaResource;

  /**
   * Creates a new Context using a newly created JMS connection.
   * 
   * @param connection  the created JMS connection.
   */
  public XAJMSContext(XAConnection cnx) {
    context = new JMSContext(cnx, Session.SESSION_TRANSACTED);
    xaResource = new XAResource(cnx.getXAResourceMngr(), context.getSession());
  }

  public javax.jms.JMSContext createContext(int sessionMode) {
    return context.createContext(sessionMode);
  }

  public javax.jms.JMSProducer createProducer() {
    return context.createProducer();
  }

  public String getClientID() {
    return context.getClientID();
  }

  public void setClientID(String clientID) {
    context.setClientID(clientID);
  }

  public javax.jms.ConnectionMetaData getMetaData() {
    return context.getMetaData();
  }

  public javax.jms.ExceptionListener getExceptionListener() {
    return context.getExceptionListener();
  }

  public void setExceptionListener(javax.jms.ExceptionListener listener) {
    context.setExceptionListener(listener);
  }

  public void start() {
    context.start();
  }

  public void stop() {
    context.stop();
  }

  public void setAutoStart(boolean autoStart) {
    context.setAutoStart(autoStart);
  }

  public boolean getAutoStart() {
    return context.getAutoStart();
  }

  public void close() {
    context.close();
  }

  public javax.jms.BytesMessage createBytesMessage() {
    return context.createBytesMessage();
  }

  public javax.jms.MapMessage createMapMessage() {
    return context.createMapMessage();
  }

  public javax.jms.Message createMessage() {
    return context.createMessage();
  }

  public javax.jms.ObjectMessage createObjectMessage() {
    return context.createObjectMessage();
  }

  public javax.jms.ObjectMessage createObjectMessage(Serializable object) {
    return context.createObjectMessage();
  }

  public javax.jms.StreamMessage createStreamMessage() {
    return context.createStreamMessage();
  }

  public javax.jms.TextMessage createTextMessage() {
    return context.createTextMessage();
  }

  public javax.jms.TextMessage createTextMessage(String text) {
    return context.createTextMessage();
  }

  public int getSessionMode() {
    return context.getSessionMode();
  }

  public void recover() {
    context.recover();
  }

  public javax.jms.JMSConsumer createConsumer(javax.jms.Destination destination) {
    return context.createConsumer(destination);
  }

  public javax.jms.JMSConsumer createConsumer(javax.jms.Destination destination,
                                              String selector) {
    return context.createConsumer(destination, selector);
  }

  public javax.jms.JMSConsumer createConsumer(javax.jms.Destination destination,
                                              String selector,
                                              boolean noLocal) {
    return context.createConsumer(destination, selector, noLocal);
  }

  public javax.jms.Queue createQueue(String name) {
    return context.createQueue(name);
  }

  public javax.jms.Topic createTopic(String name) {
    return context.createTopic(name);
  }

  public javax.jms.JMSConsumer createDurableConsumer(javax.jms.Topic topic, String name) {
    return context.createDurableConsumer(topic, name);
  }

  public javax.jms.JMSConsumer createDurableConsumer(javax.jms.Topic topic,
                                                     String name,
                                                     String selector,
                                                     boolean noLocal) {
    return context.createConsumer(topic, selector, noLocal);
  }

  public javax.jms.JMSConsumer createSharedDurableConsumer(javax.jms.Topic topic,
                                                           String name) {
    return context.createSharedDurableConsumer(topic, name);
  }

  public javax.jms.JMSConsumer createSharedDurableConsumer(javax.jms.Topic topic,
                                                           String name,
                                                           String selector) {
    return context.createSharedDurableConsumer(topic, name, selector);
  }

  public javax.jms.JMSConsumer createSharedConsumer(javax.jms.Topic topic,
                                                    String name) {
    return context.createSharedConsumer(topic, name);
  }

  public javax.jms.JMSConsumer createSharedConsumer(javax.jms.Topic topic,
                                                    String name,
                                                    String selector) {
    return context.createSharedConsumer(topic, name, selector);
  }

  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue) {
    return context.createBrowser(queue);
  }

  public javax.jms.QueueBrowser createBrowser(javax.jms.Queue queue, String selector) {
    return context.createBrowser(queue, selector);
  }

  public javax.jms.TemporaryQueue createTemporaryQueue() {
    return context.createTemporaryQueue();
  }

  public javax.jms.TemporaryTopic createTemporaryTopic() {
    return context.createTemporaryTopic();
  }

  public void unsubscribe(String name) {
    context.unsubscribe(name);
  }

  public void acknowledge() {
    context.acknowledge();
  }

  public javax.jms.JMSContext getContext() {
    return context;
  }

  public XAResource getXAResource() {
    return xaResource;
  }

  public boolean getTransacted() {
    return true;
  }

  public void commit() {
    throw new TransactionInProgressRuntimeException("Unable to commit a XAJMSContext");
  }

  public void rollback() {
    throw new TransactionInProgressRuntimeException("Unable to rollback a XAJMSContext");
  }

}
