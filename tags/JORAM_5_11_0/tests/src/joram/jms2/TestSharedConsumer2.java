/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package joram.jms2;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Create a Topic, JMSContext, Connection and User.
 * 
 * On main thread:
 * - create 2 shared durable consumers ("sharedConsumerTest") on the topic
 * - create 2 non-shared consumer on the topic
 * - Set message listener for the 4 consumers
 * On Thread 1:
 * - create 1 shared durable consumer ("sharedConsumerTest") on the topic
 * 
 * - send 3 messages on the topic
 * - verify that the shared consumers (main and Thread) receive 1 message and the non-shared consumer receive 3 messages
 * 
 * - send 30 messages on the topic
 * - verify that the sum of received messages by the shared consumer is 30.
 * - verify that the non-shared consumer received 30 messages each one.
 */
public class TestSharedConsumer2 extends TestCase {
  public static void main(String[] args) {
    new TestSharedConsumer2().run();
  }
  
  final String subName = "sharedConsumerTest";

  public void run()  {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);
      
      final ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");   
      User.create("anonymous", "anonymous", 0);
      final Topic topic = Topic.create("topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      AdminModule.disconnect();
      
      Connection cnx = cf.createConnection();
      cnx.start();
      
      // create shared consumer
      JMSContext context = cf.createContext();
      JMSConsumer receiver1 = context.createSharedDurableConsumer(topic, subName);
      JMSConsumer receiver2 = context.createSharedDurableConsumer(topic, subName);
      
      // create non-shared consumer
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer receiver3 = sess.createConsumer(topic);
      Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer receiver4 = sess1.createConsumer(topic);
      
      MsgListener ml1 = new MsgListener("Main-1-Listener on topic");
      MsgListener ml2 = new MsgListener("Main-2-Listener on topic");
      MsgListener ml3 = new MsgListener("Main-3-Listener on topic");
      MsgListener ml4 = new MsgListener("Main-4-Listener on topic");
      
      final MsgListener ml = new MsgListener("Thread-1-Listener on topic");
      Thread t = new Thread() {
        public synchronized void run() {
          try {
              consumer(cf, topic, ml);
          } catch (Exception e) {
            e.printStackTrace();
          }
          try {
            wait(50000);
          } catch (InterruptedException e) { }
        }
      };
      t.start();
      
      receiver1.setMessageListener(ml1);
      receiver2.setMessageListener(ml2);
      receiver3.setMessageListener(ml3);
      receiver4.setMessageListener(ml4);
      
      // send 3 messages
      System.out.println("== send 3");
      sendMsg(cnx, topic, 3);
      Thread.sleep(100);
      assertEquals("Main-1-Listener : ", 1, ml1.getNbMsgReceived());
      assertEquals("Main-2-Listener : ", 1, ml2.getNbMsgReceived());
      assertEquals("Main-3-Listener : ", 3, ml3.getNbMsgReceived());
      assertEquals("Main-4-Listener : ", 3, ml4.getNbMsgReceived());
      assertEquals("Thread-1-Listener : ", 1, ml.getNbMsgReceived());
      ml1.reset();
      ml2.reset();
      ml3.reset();
      ml4.reset();
      ml.reset();
      
      // send 30 message
      int nbMsgTosend = 30;
      System.out.println("\n== send " + nbMsgTosend);
      sendMsg(cnx, topic, nbMsgTosend);
      Thread.sleep(500);
      assertEquals("Main-3-Listener : ", nbMsgTosend, ml3.getNbMsgReceived());
      assertEquals("Main-4-Listener : ", nbMsgTosend, ml4.getNbMsgReceived());
      assertEquals("Shared consumers : ", nbMsgTosend, ml.getNbMsgReceived() + ml1.getNbMsgReceived() + ml2.getNbMsgReceived());
      ml1.reset();
      ml2.reset();
      ml3.reset();
      ml4.reset();
      ml.reset();
      
      
      System.out.println("Close.");
      receiver1.close();
      receiver2.close();
      receiver3.close();
      receiver4.close();
      System.out.println("unsubscribe");
      sess.unsubscribe("sharedConsumerTest");
      context.close();
      cnx.close();
      
      synchronized (t) {
        t.notify();
      }
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }
  
  public void sendMsg(Connection cnx, Topic topic, int nbMsg) throws JMSException {
    System.out.println("send " + nbMsg + " messages on topic");
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(topic);
    for (int i = 0; i < nbMsg; i++) {
      TextMessage msg = sess.createTextMessage("Test number " + i);
      producer.send(msg);
    }
  }
  
  public void consumer(ConnectionFactory cf, Topic topic, MsgListener ml) {
    // create shared consumer
    JMSContext context = cf.createContext();
    JMSConsumer receiver = context.createSharedDurableConsumer(topic, subName);
    receiver.setMessageListener(ml);
  }
  
  class MsgListener implements MessageListener {
    private String ident = null;
    private int nbMsgReceived = 0;

    public MsgListener(String ident) {
      this.ident = ident;
    }

    public void onMessage(Message msg) {
      try {
        if (msg instanceof TextMessage) {
          System.out.println(ident + ": " + ((TextMessage) msg).getText());
          nbMsgReceived++;
        }        
      } catch (JMSException e) {
        System.err.println("Exception in listener: " + e);
        error(e);
      }
    }
    
    public int getNbMsgReceived() {
      return nbMsgReceived;
    }
    
    public void reset() {
      nbMsgReceived = 0;
    }
  }
}
