package com.nortel.oam.test1.core;

import com.nortel.oam.test1.common.MessagesRateSetter;
import com.nortel.oam.test1.common.Props;
import com.nortel.oam.test1.common.SpeedMeter;
import com.nortel.oam.test1.gui.DashBoardConsumer;

import javax.jms.*;
import javax.naming.InitialContext;
import java.util.ArrayList;
import java.util.List;

public class Consumer {
  private TopicConnection mainTopicConnection;

  private List dispatchedMessages;

  public Consumer() {
    dispatchedMessages = new ArrayList();
  }

  public static void main(String[] args) {
    try {
      new Consumer().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void start() throws Exception {
    createAndRegisterTopicListener();
    MessagesRateSetter dashBoardConsumer = new DashBoardConsumer();
    new SpeedMeter(dispatchedMessages, dashBoardConsumer).start();

    synchronized(dashBoardConsumer) {
      dashBoardConsumer.wait();
    }
    closeTopicConnection();
    System.out.println("Messages Consumer is stopped. " + dispatchedMessages.size() + " messages have been processed");
    System.exit(0);
  }

  public void createAndRegisterTopicListener() throws Exception {
    InitialContext jndiContext = new InitialContext();
    TopicConnectionFactory topicConnectionFactory =
            (TopicConnectionFactory) jndiContext.lookup(Props.mainTopicFactoryName);
    mainTopicConnection = topicConnectionFactory.createTopicConnection();

    TopicSession session = mainTopicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

    Topic topic = (Topic) jndiContext.lookup(Props.mainTopicName);
    TopicSubscriber topicSubscriber = session.createSubscriber(topic,
                                                               "Prop1 = 'value1' AND Prop2 = 'value2'", false);

    MainTopicListener mainTopicListener = new MainTopicListener();
    topicSubscriber.setMessageListener(mainTopicListener);
    mainTopicConnection.start();
    jndiContext.close();
  }

  public void closeTopicConnection() throws Exception {
    System.out.println("close topics connection ");
    mainTopicConnection.close();
  }

  class MainTopicListener implements MessageListener {
    public void onMessage(Message message) {
      try {
        if (message instanceof TextMessage) {
          //Thread.sleep(100); // consumer processing deliberatly slightly slow
          TextMessage txtMessage = (TextMessage) message;
          processMessage(txtMessage.getText());
        }
      } catch (Throwable th) {
        System.err.println("MainTopicListener get an exception:");
        th.printStackTrace();
      }
    }

  }

  private void processMessage(String text) {
    synchronized (dispatchedMessages) {
      dispatchedMessages.add(text);
    }
  }
}
