package com.nortel.oam.test2.core;

import com.nortel.oam.test2.common.CloseLock;
import com.nortel.oam.test2.common.Data;
import com.nortel.oam.test2.common.ProgressSetter;
import com.nortel.oam.test2.common.Props;
import com.nortel.oam.test2.gui.Gauge;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.Iterator;

public class Producer {
  private TopicConnection mainTopicConnection;
  private TopicPublisher mainTopicPublisher;
  private TopicSession mainTopicSession;
  private QueueConnection requestQueueConnection;

  private boolean stop = false;
  private Trigger[] triggers;
  private CloseLock closeLock = new CloseLock();
  private static final String PAYLOAD = "abcdefghijklmnopqrstuvwxyz_1234567890 : " +
                                        "abcdefghijklmnopqrstuvwxyz_1234567890 : " +
                                        "abcdefghijklmnopqrstuvwxyz_1234567890 : "; // 120 chars

  public Producer() {
    triggers = new Trigger[4];
    for (int i = 0; i < triggers.length; i++) {
      triggers[i] = new Trigger();
    }
  }

  public static void main(String[] args) {
    try {
      new Producer().start();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  private void start() throws Exception {
    System.out.println("Start Messages Producer...");

    createTopicPublisher();
    createQueueListener();

    // Producer 1
    Hashtable props_1 = new Hashtable();
    props_1.put("Prop1", "value1");
    props_1.put("Prop2", "value2");
    startMessagesProduction(props_1, "'Producer 1' (right one)", new ProgressToGauge(), triggers[0]);

    // Producer 2
    Hashtable props_2 = new Hashtable();
    props_2.put("Prop1", "other value1");
    props_2.put("Prop2", "other value2");
    startMessagesProduction(props_2, "'Producer 2'", null, triggers[1]);

    // Producer 3
    Hashtable props_3 = new Hashtable();
    props_3.put("Prop3", "none");
    startMessagesProduction(props_3, "'Producer 3'", null, triggers[2]);

//    // Producer 4
//    Hashtable props_4 = new Hashtable();
//    props_4.put("Prop4", "boson");
//    startMessagesProduction(props_4, "'Producer 4'", null, triggers[3]);

    synchronized (closeLock) {
      System.out.println("Close window to exit...");
      if (!closeLock.closed) {
        closeLock.wait();
      }
    }
    stop = true;
    notifyAllTriggers();
    Thread.sleep(30);

    closeConnections();
    System.out.println("Messages Producer is stopped.");
    System.exit(0);
  }

  private void notifyAllTriggers() {
    for (int i = 0; i < triggers.length; i++) {
      Trigger trigger = triggers[i];
      synchronized (trigger) {
        trigger.burned = false;
        trigger.notifyAll();
      }
    }
  }

  private void createTopicPublisher() throws Exception {
    Context jndiCtx = new InitialContext();
    TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) jndiCtx.lookup(Props.topicFactoryName);
    Topic mainTopic = (Topic) jndiCtx.lookup(Props.mainTopicName);
    mainTopicConnection = topicConnectionFactory.createTopicConnection();
    mainTopicSession = mainTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    mainTopicPublisher = mainTopicSession.createPublisher(mainTopic);
    mainTopicConnection.setExceptionListener(new ExceptionListener() {
      public void onException(JMSException e) {
        e.printStackTrace();
      }
    });
  }

  private void createQueueListener() throws Exception {
    Context jndiCtx = new InitialContext();
    QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) jndiCtx.lookup(Props.queueFactoryName);
    Queue requestQueue = (Queue) jndiCtx.lookup(Props.requestQueueName);
    requestQueueConnection = queueConnectionFactory.createQueueConnection();
    QueueSession requestQueueSession = requestQueueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    QueueReceiver requestQueueReceiver = requestQueueSession.createReceiver(requestQueue);
    requestQueueReceiver.setMessageListener(new MessageListener() {
      public void onMessage(Message message) {
        try {
          System.out.println("Received new cycle request...");
          notifyAllTriggers();
          System.out.println("request dispatched.");
        } catch (Throwable th) {
          th.printStackTrace();
        }
      }
    });
    requestQueueConnection.start();
  }

  private void startMessagesProduction(final Hashtable properties, final String id,
                                       final ProgressSetter progressSetter, final Producer.Trigger trigger) {
    new Thread("messagesProduction-" + id) {
      public void run() {
        try {
          while (!stop) {
            synchronized (trigger) {
              if (trigger.burned) {
                trigger.wait();
              }
              trigger.burned = true;
              if (stop) {
                System.out.println("Stop messages producer " + id);
                return;
              }
            }
            System.out.println("Start cycle " + id);
            for (int i = 0; i < Props.msgs_count_per_cycle; i++) {
              ObjectMessage objectMessage = mainTopicSession.createObjectMessage(new Data(i, PAYLOAD));
              for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                objectMessage.setStringProperty(key, (String) properties.get(key));
              }
              mainTopicPublisher.publish(objectMessage);
              if (progressSetter != null) {
                progressSetter.setProgress(i);
              }
              Thread.sleep(Props.PRODUCER_DELAY);
            }
            System.out.println("End cycle " + id);
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }.start();
    System.out.println("Messages Producer " + id + " is started.");
  }

  private void closeConnections() throws Exception {
    System.out.println("close connections");
    mainTopicConnection.close();
    requestQueueConnection.close();
  }

  class ProgressToGauge implements ProgressSetter {
    private int progress;
    private Gauge gauge;

    public ProgressToGauge() {
      gauge = new Gauge("Producer Gauge", closeLock);
      Thread thread = new Thread("Supervisor") {
        public void run() {
          try {
            while (!stop) {
              gauge.setProgress(progress);
              Thread.sleep(500);
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      };
      thread.setDaemon(true);
      thread.start();
    }

    public void setProgress(int progress) {
      this.progress = progress;
    }
  }

  class Trigger {
    boolean burned = true;
  }
}
