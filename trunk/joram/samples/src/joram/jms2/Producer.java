package jms2;

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

public class Producer implements CompletionListener{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    ictx = new InitialContext();
    Destination dest = (Destination) ictx.lookup(args[0]);
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();
    
    JMSContext context = cf.createContext();
    JMSProducer producer = context.createProducer();
    producer.setAsync(new Producer());
    context.start();
    
    int i = 0;
    for (; i < 10; i++) {
      TextMessage msg = context.createTextMessage("Test number " + i);
      msg.setIntProperty("order", i);
      producer.send(dest, msg);
    }
    
    Thread.sleep(1000L);
    
    context.close();
    System.exit(0);
  }
  
  @Override
  public void onCompletion(Message message) {
    int n = -1;
    try {
      n = message.getIntProperty("order");
      System.out.println(n + " messages sent.");
    } catch (JMSException jE) {
      jE.printStackTrace();
    }
  }

  @Override
  public void onException(Message message, Exception exc) {
    int n = -1;
    try {
      n = message.getIntProperty("order");
      System.out.println(n + " messages failed.");
      exc.printStackTrace();
    } catch (JMSException jE) {
      jE.printStackTrace();
    }
  }

}
