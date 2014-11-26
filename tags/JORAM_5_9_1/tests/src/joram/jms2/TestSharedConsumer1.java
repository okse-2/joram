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
 * - create 2 shared durable consumers on the topic
 * - create 1 consumer on the topic
 * - Set message listener for the 3 consumers
 * - send 2 messages on the topic
 * - verify that the shared consumers receive 1 message and the non-shared consumer receive 2 messages
 * 
 * Remove the one message listener on the shared consumer
 * - send 2 messages on the topic
 * - verify that the shared consumers receive 2 message and the non-shared consumer receive 2 messages
 * 
 */
public class TestSharedConsumer1 extends TestCase {
  public static void main(String[] args) {
    new TestSharedConsumer1().run();
  }

  final String subName = "sharedConsumerTest";
  
  public void run()  {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);
      
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");   
      User.create("anonymous", "anonymous", 0);
      Topic topic = Topic.create("topic");
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
      
      MsgListener ml1 = new MsgListener("1-Listener on topic");
      MsgListener ml2 = new MsgListener("2-Listener on topic");
      MsgListener ml3 = new MsgListener("3-Listener on topic");
      
      receiver1.setMessageListener(ml1);
      receiver2.setMessageListener(ml2);
      receiver3.setMessageListener(ml3);
      
      sendMsg(cnx, topic, 2);
      
      Thread.sleep(100);
      assertEquals("1-Listener : ", 1, ml1.getNbMsgReceived());
      assertEquals("2-Listener : ", 1, ml2.getNbMsgReceived());
      assertEquals("3-Listener : ", 2, ml3.getNbMsgReceived());
      ml1.reset();
      ml2.reset();
      ml3.reset();
      
      System.out.println("remove listener 1");
      receiver1.setMessageListener(null);
      sendMsg(cnx, topic, 2);
      Thread.sleep(100);
      assertEquals("1-Listener : ", 0, ml1.getNbMsgReceived());
      assertEquals("2-Listener : ", 2, ml2.getNbMsgReceived());
      assertEquals("3-Listener : ", 2, ml3.getNbMsgReceived());
      ml1.reset();
      ml2.reset();
      ml3.reset();
      
      System.out.println("remove listener 2");
      receiver2.setMessageListener(null);
      sendMsg(cnx, topic, 2);
      Thread.sleep(100);
      assertEquals("1-Listener : ", 0, ml1.getNbMsgReceived());
      assertEquals("2-Listener : ", 0, ml2.getNbMsgReceived());
      assertEquals("3-Listener : ", 2, ml3.getNbMsgReceived());
      ml1.reset();
      ml2.reset();
      ml3.reset();
      
      System.out.println("Close.");
      receiver1.close();
      receiver2.close();
      receiver3.close();
      System.out.println("unsubscribe");
      sess.unsubscribe("sharedConsumerTest");
      context.close();
      cnx.close();
      
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }
  
  public void sendMsg(Connection cnx, Topic topic, int nbMsg) throws JMSException {
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(topic);
    for (int i = 0; i < nbMsg; i++) {
      TextMessage msg = sess.createTextMessage("Test number " + i);
      producer.send(msg);
    }
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
