/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.dursub;


import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Check that two durable subscriptions do not
 * interfere when consuming their messages.
 */
public class Dursub_2 extends TestCase {

  public static void main(String[] args) {
    new Dursub_2().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost",2560 ,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Topic topic = 
        org.objectweb.joram.client.jms.Topic.create(0);
      topic.setFreeReading();
      topic.setFreeWriting();

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost",2560 );
      
      Connection connection1 = cf.createConnection(
        "anonymous", "anonymous");
      Connection connection2 = cf.createConnection(
        "anonymous", "anonymous");

      Session sendSession = connection1.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(topic);

      Session recSession1 = connection1.createSession(
        false,
        Session.CLIENT_ACKNOWLEDGE);
      Session recSession2 = connection2.createSession(
        false,
        Session.CLIENT_ACKNOWLEDGE);

      TopicSubscriber consumer1 = 
        recSession1.createDurableSubscriber(topic, "test_sub1");
      TopicSubscriber consumer2 = 
        recSession2.createDurableSubscriber(topic, "test_sub2");

      connection1.start();
      connection2.start();

      //System.out.println("Producer: send msg1");
      TextMessage msg1 = sendSession.createTextMessage("msg1");
      producer.send(msg1);

      //System.out.println("Consumer1: receive msg1");
      msg1 = (TextMessage)consumer1.receive();
      //System.out.println("Consumer1: msg = " + msg1.getText());
      assertEquals("msg1", msg1.getText());
      msg1.acknowledge();
      //System.out.println("Consumer2: receive msg1");
      msg1 = (TextMessage)consumer2.receive();
      //System.out.println("Consumer2: msg = " + msg1.getText());
      assertEquals("msg1", msg1.getText());
      msg1.acknowledge();

      //System.out.println("Close connection #2");
      connection2.close();

      //System.out.println("Producer: send msg2");
      TextMessage msg2 = sendSession.createTextMessage("msg2");
      producer.send(msg2);

      //System.out.println("Consumer1: receive msg2");
      msg2 = (TextMessage)consumer1.receive();
      //System.out.println("Consumer1: msg = " + msg2.getText());
      assertEquals("msg2", msg2.getText());
      msg2.acknowledge();

      Thread.sleep(2000);
      //System.out.println("restart connection #2");
      connection2 = cf.createConnection(
        "anonymous", "anonymous");
      recSession2 = connection2.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      consumer2 = 
        recSession2.createDurableSubscriber(topic, "test_sub2");
      connection2.start();
      
      //System.out.println("Consumer2: receive msg2");
      msg2 = (TextMessage)consumer2.receive();
      //System.out.println("Consumer2: msg = " + msg2.getText());
      assertEquals("msg2", msg2.getText());

      //System.out.println("Connection close");
      connection1.close();
      connection2.close();
      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
