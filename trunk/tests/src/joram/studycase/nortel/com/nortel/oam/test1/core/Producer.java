package com.nortel.oam.test1.core;

import com.nortel.oam.test1.common.MessagesRate;
import com.nortel.oam.test1.common.Props;
import com.nortel.oam.test1.common.SpeedMeter;
import com.nortel.oam.test1.gui.DashBoardProducer;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class Producer {
  private TopicConnection mainTopicConnection;
  private TopicPublisher mainTopicPublisher;
  private TopicSession mainTopicSession;
  private boolean stop = false;

  public static void main(String[] args) {
    try {
      new Producer().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void start() throws Exception {
    System.out.println("Start Messages Producer...");
    createTopicPublisher();

    // Producer 1
    List msgList_1 = new ArrayList();
    MessagesRate msgRate_1 = new MessagesRate(20);
    DashBoardProducer dashBoard = new DashBoardProducer(msgRate_1);
    new SpeedMeter(msgList_1, dashBoard).start();
    Hashtable props_1 = new Hashtable();
    props_1.put("Prop1", "value1");
    props_1.put("Prop2", "value2");
    startMessagesProduction(props_1, msgList_1, msgRate_1);

    // Producer 2
    List msgList_2 = new ArrayList();
    MessagesRate msgRate_2 = new MessagesRate(20);
    Hashtable props_2 = new Hashtable();
    props_2.put("Prop1", "other value1");
    props_2.put("Prop2", "other value2");
    startMessagesProduction(props_2, msgList_2, msgRate_2);

    // Producer 3
    List msgList_3 = new ArrayList();
    MessagesRate msgRate_3 = new MessagesRate(20);
    Hashtable props_3 = new Hashtable();
    props_3.put("Prop3", "none");
    startMessagesProduction(props_3, msgList_3, msgRate_3);

    synchronized (msgRate_1) {
      msgRate_1.wait();
      stop = true;
      Thread.sleep(30);
    }
    closeConnections();
    System.out.println("Messages Producer is stopped. " + msgList_1.size() + " messages have been sent on producer 1.");
    System.exit(0);
  }

  private void createTopicPublisher() {
    try {
      Context jndiCtx = new InitialContext();
      TopicConnectionFactory topicConnectionFactory =
              (TopicConnectionFactory) jndiCtx.lookup(Props.mainTopicFactoryName);
      Topic mainTopic = (Topic) jndiCtx.lookup(Props.mainTopicName);
      mainTopicConnection = topicConnectionFactory.createTopicConnection();
      mainTopicSession = mainTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
      mainTopicPublisher = mainTopicSession.createPublisher(mainTopic);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("Failed to create publisher");
      System.exit(1);
    }
  }

  private void startMessagesProduction(final Hashtable properties, final List msgList, final MessagesRate msgRate) {
    new Thread("messagesProduction") {
      public void run() {
        try {
          long currentDelay = 1000;
          long msgCounter = 0;
          msgList.clear();
          TextMessage textMessage = mainTopicSession.createTextMessage();
          for (Iterator it = properties.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            textMessage.setStringProperty(key, (String) properties.get(key));
          }
          while (!stop) {
            String message_i = Props.messageBody + msgCounter;
            synchronized (msgList) {
              msgList.add(message_i);
            }
            textMessage.setText(message_i);
            textMessage.setLongProperty("number", msgCounter);
            mainTopicPublisher.publish(textMessage);
            msgCounter++;
            synchronized (msgRate) {
              if (msgRate.getMessagesRate() == 0) {
                return;
              }
              currentDelay = (long) (1000 / msgRate.getMessagesRate());
            }
            Thread.sleep(currentDelay);
          }
        } catch (Throwable e) {
          e.printStackTrace();
        }
      }
    }.start();
    System.out.println("Messages Producer is started.");
  }

  private void closeConnections() throws Exception {
    System.out.println("close connections");
    mainTopicConnection.close();
  }
}
