package com.nortel.oam.test3.core;

import com.nortel.oam.test3.common.CloseLock;
import com.nortel.oam.test3.common.Closer;
import com.nortel.oam.test3.common.Props;
import com.nortel.oam.test3.gui.Gauge;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Consumer {
  private int dispatchedMsgsCount = 0;
  private boolean stop = false;
  private static final String SELECTOR = "Prop1 = 'value1' AND Prop2 = 'value2'";
  private CloseLock closeLock = new CloseLock();
  private int cycle_count = 1;

  public static void main(String[] args) {
    try {
      new Consumer().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void start() throws Exception {
    createSupervisor();

    synchronized (closeLock) {
      System.out.println("Close window to exit...");
      if (!closeLock.closed) {
        closeLock.wait();
      }
    }
    stop = true;
    Thread.sleep(150);

    System.out.println("Messages Consumer is stopped.");
    System.exit(0);
  }

  private void createSupervisor() {
    final int msgs_count_per_cycle = 3 * Props.msgs_count_per_cycle;
    final Gauge gauge = new Gauge("Consumer Gauge", closeLock, msgs_count_per_cycle);
    new Thread("Supervisor") {
      public void run() {
        List resourcesToClose = Collections.EMPTY_LIST;
        try {
          System.out.println("Supervisor started");
          resourcesToClose = initAndStart();   // start connections and send request

          while (!stop) {
            gauge.setProgress(getDispatchedMsgsCount());

            if (getDispatchedMsgsCount() == msgs_count_per_cycle) {
              System.out.println("\t" + msgs_count_per_cycle + " messages received: OK");
              initDispatchedMsgsCount();

              for (Iterator it = resourcesToClose.iterator(); it.hasNext();) {
                Closer closer = (Closer) it.next();
                closer.close();
                it.remove();
              }
              resourcesToClose = initAndStart();
            }
            Thread.sleep(500);
          }
        } catch (Throwable th) {
          th.printStackTrace();
          System.exit(1);
        } finally {
          for (Iterator it = resourcesToClose.iterator(); it.hasNext();) {
            Closer closer = (Closer) it.next();
            closer.close();
            it.remove();
          }
        }
      }
    }.start();
  }

  private List initAndStart() throws NamingException, JMSException {
    System.out.println("Start cycle #" + cycle_count++);
    List resourcesToClose = new ArrayList();

    InitialContext jndiCtxt = new InitialContext();
    TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiCtxt.lookup(Props.topicFactoryName);
    Topic topic = (Topic) jndiCtxt.lookup(Props.mainTopicName);

    final TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
    topicConnection.setExceptionListener(new ExceptionListener() {
      public void onException(JMSException e) {
        System.err.println("Exception on Topic Connection");
        e.printStackTrace();
      }
    });
    final TopicSession topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic, SELECTOR, false);
    topicSubscriber.setMessageListener(new MainTopicListener());
    topicConnection.start();
    resourcesToClose.add(new Closer() {
      public void close() {
        try {
          System.out.println("\t" + "Close Topic resources (main)");
          topicConnection.setExceptionListener(null);
          topicSession.setMessageListener(null);
          topicSession.close();
          topicConnection.close();
        } catch (JMSException e) {
          e.printStackTrace();
        }
      }
    });

    QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiCtxt.lookup(Props.queueFactoryName);
    Queue queue = (Queue) jndiCtxt.lookup(Props.requestQueueName);

    final QueueConnection queueConnection = queueConnectionFactory.createQueueConnection();
    queueConnection.setExceptionListener(new ExceptionListener() {
      public void onException(JMSException e) {
        System.err.println("Exception on Queue Connection");
        e.printStackTrace();
      }
    });
    final QueueSession requestQueueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    QueueSender requestQueueSender = requestQueueSession.createSender(queue);
    queueConnection.start();

    resourcesToClose.add(new Closer() {
      public void close() {
        try {
          System.out.println("\t" + "Close Queue resources (Resync Req & Tmp)");
          queueConnection.setExceptionListener(null);
          requestQueueSession.setMessageListener(null);
          requestQueueSession.close();
          queueConnection.close();
        } catch (JMSException e) {
          e.printStackTrace(System.out);
        }
      }
    });

    TextMessage textMessage = requestQueueSession.createTextMessage("Request");
    requestQueueSender.send(textMessage);

    jndiCtxt.close();
    return resourcesToClose;
  }

  class MainTopicListener implements MessageListener {
    public void onMessage(Message message) {
      try {
        if (message instanceof ObjectMessage) {
          Thread.sleep(Props.CONSUMER_DELAY);
          ObjectMessage objMessage = (ObjectMessage) message;
          processMessage(objMessage.getObject());
        } else {
          System.err.println("MainTopic: Wrong message type!");
        }
      } catch (Throwable th) {
        System.err.println("MainTopicListener get an exception:");
        th.printStackTrace();
      }
    }
  }

  private synchronized void processMessage(Serializable object) {
    dispatchedMsgsCount++;
  }

  private synchronized int getDispatchedMsgsCount() {
    return dispatchedMsgsCount;
  }

  private synchronized void initDispatchedMsgsCount() {
    dispatchedMsgsCount = 0;
  }
}
