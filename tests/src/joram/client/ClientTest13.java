/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;


import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Reproduce bug in ClientSubscription.
 */
public class ClientTest13 extends TestCase {

  public static void main(String[] args) {
    new ClientTest13().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost", 2560,
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
          "localhost", 2560);
      
      Connection connection = cf.createConnection(
        "anonymous", "anonymous");

      Session sendSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(topic);

      Session recSession = connection.createSession(
        false,
        Session.CLIENT_ACKNOWLEDGE);

      MessageConsumer consumer1 = recSession.createConsumer(topic);
      MessageConsumer consumer2 = recSession.createConsumer(topic);

      connection.start();

      System.out.println("Producer: send msg1");
      TextMessage msg1 = sendSession.createTextMessage("msg1");
      producer.send(msg1, DeliveryMode.NON_PERSISTENT, 0, 1000);

      System.out.println("Consumer1: receive msg1");
      msg1 = (TextMessage)consumer1.receive();
      assertEquals("msg1",msg1.getText());
      Thread.sleep(2000);
      
      // because of ttl msg1 is deleted on server. 

      System.out.println("Producer: send msg2");
      TextMessage msg2 = sendSession.createTextMessage("msg2");
      producer.send(msg2);

      // This second receive should delete the
      // message msg1 as it is not valid any more
      // and return msg2.
      System.out.println("Consumer2: receive msg2");
      msg2 = (TextMessage)consumer2.receive();
      assertEquals("msg2",msg2.getText());
      
      // Bug: null pointer in ClientSubscription
      // denies msg1.
      System.out.println("Session: recover");
      recSession.recover();
      // msg1 and msg2 not are ack. but msg1 was delete on server.Only msg2 is recovered

      msg2 = (TextMessage)consumer2.receive();
      assertEquals("msg2",msg2.getText());
      
      msg2 = (TextMessage)consumer2.receive(500L);
      assertEquals(null,msg2);
      Thread.sleep(2000);

      System.out.println("Connection close");
      connection.close();
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
