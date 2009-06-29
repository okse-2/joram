/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.dmq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.DeadMQueue;

import framework.TestCase;

/**
 * Test : Set 2 specifics DMQs, one for queue and another for user. 
 *        Set threshold either for queue and user, verify that messages are
 *        well forwarded to the right DMQ.
 *    
 */
public class TestDmq1 extends TestCase {


  public static void main(String[] args) {
    new TestDmq1().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short)0);

      admin();
      System.out.println("admin config ok");

      Context  ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("queue");
      Topic topic = (Topic) ictx.lookup("topic");
      DeadMQueue dmq =(DeadMQueue) ictx.lookup("dmq");
      DeadMQueue dmq1 =(DeadMQueue) ictx.lookup("dmq1");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnxq = cf.createConnection();
      Connection cnxdq = cf.createConnection("dmq","dmq");
      Session sessionp = cnxq.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessioncdq = cnxdq.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessioncdq1 = cnxdq.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessioncq = cnxq.createSession(true, Session.AUTO_ACKNOWLEDGE);

      cnxq.start();
      cnxdq.start();

      // create a producer and a consumer
      MessageProducer producer = sessionp.createProducer(null);
      MessageConsumer consumerdq = sessioncdq.createConsumer(dmq);
      MessageConsumer consumerdq1 = sessioncdq1.createConsumer(dmq1);
      MessageConsumer consumerq = sessioncq.createConsumer(queue);
      MessageConsumer consumert = sessioncq.createConsumer(topic);

      // create a text message send to the queue by the pruducer 
      TextMessage msg = sessionp.createTextMessage();
      msg= sessionp.createTextMessage("message_q");
      producer.send(queue,msg);
      msg= sessionp.createTextMessage("message_t");
      producer.send(topic,msg);

      // the consumer receive the message from the dq
      TextMessage msg1=(TextMessage)consumerq.receive(); 
      sessioncq.rollback();

      msg1=(TextMessage) consumert.receive(); 
      sessioncq.rollback();

      msg1=(TextMessage) consumerq.receive(); 
      sessioncq.rollback();

      msg1=(TextMessage) consumert.receive(); 
      sessioncq.rollback();

      msg1=(TextMessage) consumerq.receive(500);
      assertEquals(null,msg1);

      msg1=(TextMessage) consumert.receive(500); 
      assertEquals(null,msg1);

      msg1 = (TextMessage) consumerdq.receive();
      assertEquals("message_q",msg1.getText());
      assertEquals(3, msg1.getIntProperty("JMSXDeliveryCount"));

      msg1 = (TextMessage) consumerdq1.receive();
      assertEquals("message_t",msg1.getText());
      assertEquals(3, msg1.getIntProperty("JMSXDeliveryCount"));

      cnxq.close();
      cnxdq.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short)0);
      endTest(); 
    }
  }

  /**
   * Admin : Create queue and a user anonymous
   *   use jndi
   */
  public void admin() throws Exception {
    // conexion 
    org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560, "root", "root", 60);
    // create a Queue   
    org.objectweb.joram.client.jms.Queue queue =
      (org.objectweb.joram.client.jms.Queue) org.objectweb.joram.client.jms.Queue.create("queue"); 
    org.objectweb.joram.client.jms.Topic topic =
      (org.objectweb.joram.client.jms.Topic) org.objectweb.joram.client.jms.Topic.create("topic"); 

    DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
    DeadMQueue dmq1 = (DeadMQueue) DeadMQueue.create(0);



    // create a user
    org.objectweb.joram.client.jms.admin.User user =
      org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
    org.objectweb.joram.client.jms.admin.User userdmq =
      org.objectweb.joram.client.jms.admin.User.create("dmq", "dmq");


    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();
    topic.setFreeReading();
    topic.setFreeWriting();
    queue.setDMQ(dmq);
    queue.setThreshold(2);
    user.setDMQ(dmq1);
    user.setThreshold(2);
    dmq.setReader(userdmq);
    dmq.setWriter(userdmq);
    dmq1.setReader(userdmq);
    dmq1.setWriter(userdmq);

    javax.jms.ConnectionFactory cf =
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("dmq", dmq);
    jndiCtx.bind("dmq1", dmq1);
    jndiCtx.close();

    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
  }
}

