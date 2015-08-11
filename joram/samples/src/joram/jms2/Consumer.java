package jms2;

import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.jms.JMSContext;
import javax.jms.CompletionListener;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class Consumer implements MessageListener {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    ictx = new InitialContext();
    Destination dest = (Destination) ictx.lookup(args[0]);
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();
    
    JMSContext context = cf.createContext();
    JMSConsumer consumer = context.createConsumer(dest);
    consumer.setMessageListener(new Consumer());
    context.start();
    
    System.in.read();
    context.close();
    System.out.println("Consumer closed.");

    System.exit(0);
  }
  
  @Override
  public void onMessage(Message msg) {
    try {
      Destination destination = msg.getJMSDestination();
      Destination replyTo = msg.getJMSReplyTo();
      
      int n = msg.getIntProperty("order");
      
      System.out.println("Receives message #" + n + " from=" + destination);

      if (msg instanceof TextMessage) {
        System.out.println(" -- " + ((TextMessage) msg).getText());
      }
    } catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }

}
