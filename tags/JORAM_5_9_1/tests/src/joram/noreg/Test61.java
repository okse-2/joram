/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.TextMessage;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * The objective is to allow disabling the mechanism of pre-requesting 
 * of the messages in a listener mode (queueMessageReadMax = 0)
 * 
 * One producer send 4 messages on queue.
 * One consumer make time to process the message, and an other one is normal 
 * 
 * If queueMessageReadMax != 0 we receive on
 * consumer1 : msg0, msg2
 * consumer2 : msg1, msg3
 * 
 * If queueMessageReadMax == 0 we receive on
 * consumer1 : msg0 
 * consumer2 : msg1, msg2, msg3
 * 
 * @see JORAM-33
 */
public class Test61 extends TestCase {

  public static void main(String[] args) {
    new Test61().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

      AdminModule.connect(cf);

      // create queue
      Queue queue = Queue.create(0, "queue");
      queue.setFreeReading();
      queue.setFreeWriting();
      // create a user
      User.create("anonymous", "anonymous");

      //AdminModule.disconnect();
      System.out.println("admin config ok");

      Connection cnx = (Connection) cf.createConnection();
      cnx.start();
      
      Session sess1 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer1 = (MessageConsumer) sess1.createConsumer(queue);
      
      
      Session sess2 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer2 = (MessageConsumer) sess2.createConsumer(queue);
      
      Session sess = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = (MessageProducer) sess.createProducer(queue);

      for (int i = 0; i < 4; i++) {
        TextMessage msg = (TextMessage) sess.createTextMessage("Test number " + i);
        producer.send(msg);
      }

      MsgListener msgListener1 = new MsgListener("sleep: Listener Session1 on " + queue);
      MsgListener msgListener2 = new MsgListener("Listener Session2 on " + queue);
      consumer1.setMessageListener(msgListener1);
      consumer2.setMessageListener(msgListener2);

      for (int i = 0; i < 10; i++) {
        if (msgListener1.nbRec+msgListener2.nbRec == 4) {
          break;
        }
        Thread.sleep(1000);
      }

      assertEquals(2, msgListener1.nbRec);
      assertEquals(2, msgListener2.nbRec);
      
      // close session 1 and 2
      sess1.close();
      sess2.close();

      
      // Disable the mechanism of pre-requesting of the messages : set queueMessageReadMax = 0.
      System.out.println("\n\nTest: Disable the mechanism of pre-requesting of the messages.");
      Session sess3 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      System.out.println("sess3.setQueueMessageReadMax(0)");
      sess3.setQueueMessageReadMax(0);
      MessageConsumer consumer3 = (MessageConsumer) sess3.createConsumer(queue);
      
      Session sess4 = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer4 = (MessageConsumer) sess4.createConsumer(queue);
      
      for (int i = 0; i < 4; i++) {
        TextMessage msg = (TextMessage) sess.createTextMessage("Test number " + i);
        producer.send(msg);
      }

      MsgListener msgListener3 = new MsgListener("sleep: Listener Session3 on " + queue);
      MsgListener msgListener4 = new MsgListener("Listener Session4 on " + queue);
      consumer3.setMessageListener(msgListener3);
      consumer4.setMessageListener(msgListener4);

      for (int i = 0; i < 10; i++) {
        if (msgListener3.nbRec+msgListener4.nbRec == 4) {
          break;
        }
        Thread.sleep(1000);
      }

      assertEquals(1, msgListener3.nbRec);
      assertEquals(3, msgListener4.nbRec);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();     
    }
  }
}

class MsgListener  implements MessageListener {
  String ident = null;
  int nbRec = 0;

  public MsgListener() {}

  public MsgListener(String ident) {
    this.ident = ident;
  }

  public void onMessage(Message msg) {
    nbRec++;
    try {
      if (msg instanceof TextMessage) {
        if (ident == null) 
          System.out.println(((TextMessage) msg).getText());
        else
          System.out.println(ident + ": " + ((TextMessage) msg).getText());
      } else if (msg instanceof ObjectMessage) {
        if (ident == null) 
          System.out.println(((ObjectMessage) msg).getObject());
        else
          System.out.println(ident + ": " + ((ObjectMessage) msg).getObject());
      }
      if (ident.contains("sleep")) {
        Thread.sleep(1000);
      }
    } catch (Exception jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
