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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;


/**
 * Tests durable subscription.
 * 
 * Create a durable sub 'test_sub' to the topic 'topic'.
 * Sends a message msg1 to 'topic'.
 * Receive msg1 from 'test_sub'.
 * Sends a message msg2 to 'topic'.
 * Stops the client.
 * 
 * Start another client (DurTest2_2).
 * Receive msg2 from 'test_sub'.
 * 
 * @author feliot
 *
 *
 */
public class DurTest2 extends TestCase {

  public static void main(String[] args) {
    new DurTest2().run();
  }

  public void run() {
    try {
      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Topic topic = 
        org.objectweb.joram.client.jms.Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      
      Connection connection = cf.createConnection(
        "anonymous", "anonymous");

      Session sendSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(topic);

      Session recSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);

      TopicSubscriber consumer = recSession.createDurableSubscriber(topic, "test_sub");
      
      connection.start();
      
      recSession.close();

      recSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);

      consumer = recSession.createDurableSubscriber(topic, "test_sub");

      //System.out.println("Producer: send msg1");
      TextMessage msg1 = sendSession.createTextMessage("msg1");
      producer.send(msg1);

      //System.out.println("Consumer: receive msg1");
      msg1 = (TextMessage)consumer.receive();
      //System.out.println("Consumer: msg = " + msg1);

      //System.out.println("Producer: send msg2");
      TextMessage msg2 = sendSession.createTextMessage("msg2");
      producer.send(msg2);

      Thread.sleep(2000);

      // Simulates a client failure
      System.out.println("Exit");
      System.exit(0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    }
  }
}
