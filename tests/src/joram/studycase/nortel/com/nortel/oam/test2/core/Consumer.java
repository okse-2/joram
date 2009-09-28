package com.nortel.oam.test2.core;

import com.nortel.oam.test2.common.CloseLock;
import com.nortel.oam.test2.common.Props;
import com.nortel.oam.test2.gui.Gauge;

import javax.jms.*;
import javax.naming.InitialContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Consumer {
  private TopicConnection mainTopicConnection;
  private QueueConnection requestQueueConnection;
  private QueueSession requestQueueSession;
  private QueueSender requestQueueSender;

  private List dispatchedMessages = new ArrayList();
  private boolean stop = false;
  private static final String SELECTOR = "Prop1 = 'value1' AND Prop2 = 'value2'";
  private CloseLock closeLock = new CloseLock();

  public static void main(String[] args) {
    try {
      new Consumer().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void start() throws Exception {
    createAndRegisterTopicListener(SELECTOR);
    createRequestPublisher();
    createSupervisor();

    synchronized (closeLock) {
      System.out.println("Close window to exit...");
      if (!closeLock.closed) {
        closeLock.wait();
      }
    }
    stop = true;
    Thread.sleep(150);

    closeTopicConnection();
    System.out.println("Messages Consumer is stopped.");
    System.exit(0);
  }

  private void createSupervisor() {
    final Gauge gauge = new Gauge("Consumer Gauge", closeLock);
    new Thread("Supervisor") {
      public void run() {
        try {
          System.out.println("Supervisor started");
          TextMessage textMessage = requestQueueSession.createTextMessage("Request");
          requestQueueSender.send(textMessage);

          while (!stop) {
            gauge.setProgress(dispatchedMessages.size());
            //System.out.println("Size is: " + dispatchedMessages.size());
            if (dispatchedMessages.size() == Props.msgs_count_per_cycle) {
              System.out.println(Props.msgs_count_per_cycle + " messages received: OK");
              dispatchedMessages.clear();

              createAndRegisterTopicListener(SELECTOR); // re-create connection deliberatly
              textMessage = requestQueueSession.createTextMessage("Request");
              requestQueueSender.send(textMessage);
            }
            Thread.sleep(500);
          }
        } catch (Throwable th) {
          th.printStackTrace();
          System.exit(1);
        }
      }
    }.start();
  }

  public void createAndRegisterTopicListener(String selector) throws Exception {
    if (mainTopicConnection != null) {
      mainTopicConnection.setExceptionListener(null);
      mainTopicConnection.close();
    }

    InitialContext jndiCtxt = new InitialContext();
    TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiCtxt.lookup(Props.topicFactoryName);
    Topic topic = (Topic) jndiCtxt.lookup(Props.mainTopicName);

    mainTopicConnection = topicConnectionFactory.createTopicConnection();
    TopicSession session = mainTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    TopicSubscriber topicSubscriber = session.createSubscriber(topic, selector, false);
    MainTopicListener mainTopicListener = new MainTopicListener();
    topicSubscriber.setMessageListener(mainTopicListener);
    mainTopicConnection.setExceptionListener(new ExceptionListener() {
      public void onException(JMSException e) {
        e.printStackTrace();
      }
    });
    mainTopicConnection.start();
    jndiCtxt.close();
  }

  private void createRequestPublisher() throws Exception {
    InitialContext jndiCtxt = new InitialContext();
    QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiCtxt.lookup(Props.queueFactoryName);
    Queue queue = (Queue) jndiCtxt.lookup(Props.requestQueueName);

    requestQueueConnection = queueConnectionFactory.createQueueConnection();
    requestQueueSession = requestQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    requestQueueSender = requestQueueSession.createSender(queue);
  }

  public void closeTopicConnection() throws Exception {
    System.out.println("close connections");
    mainTopicConnection.close();
    requestQueueConnection.close();
  }

  class MainTopicListener implements MessageListener {
    public void onMessage(Message message) {
      try {
        if (message instanceof ObjectMessage) {
          Thread.sleep(Props.CONSUMER_DELAY);
          ObjectMessage objMessage = (ObjectMessage) message;
          processMessage(objMessage.getObject());
        } else {
          System.err.println("Wrong message type!");
        }
      } catch (Throwable th) {
        System.err.println("MainTopicListener get an exception:");
        th.printStackTrace();
      }
    }

  }

  private void processMessage(Serializable object) {
    synchronized (dispatchedMessages) {
      dispatchedMessages.add(object);
    }
  }
}
