/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.interceptors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Testing:
 *
 * - Add user interceptors IN for user1 and user2.
 * This interceptors add a property on message.
 * 
 * - Get user interceptors, verify it's ok.
 * 
 * send and receive message, and verify the right added properties.
 *
 * - Remove user interceptors.
 * 
 * send and receive message, and check property.
 */
public class Test4 extends TestCase {

  public static void main(String[] args) {
    new Test4().run();
  }

  public void run() {
    try {
      startAgentServer( (short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user1 = 
        org.objectweb.joram.client.jms.admin.User.create("user1", "user1", 0);
      
      org.objectweb.joram.client.jms.admin.User user2 = 
        org.objectweb.joram.client.jms.admin.User.create("user2", "user2", 0);

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
      
      // add interceptor Exit1 on user1
      user1.addInterceptorsIN("joram.interceptors.Exit1");
      
      // add interceptor Exit2 on user2
      user2.addInterceptorsIN("joram.interceptors.Exit2");

      // get user1 interceptor
      assertEquals("joram.interceptors.Exit1", user1.getInterceptorsIN());
      
      // get user2 interceptor
      assertEquals("joram.interceptors.Exit2", user2.getInterceptorsIN());
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      Connection connection1 = cf.createConnection("user1", "user1"); 
      connection1.start();
      
      Connection connection2 = cf.createConnection("user2", "user2"); 
      connection2.start();
      
      Session session1 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      MessageProducer producer1 = session1.createProducer(queue);
      MessageProducer producer2 = session2.createProducer(queue);
      
      Session session3 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session3.createConsumer(queue);

      // send message
      producer1.send(session1.createTextMessage("test interceptors"));
      producer2.send(session2.createTextMessage("test interceptors"));
      // receive message
      Message message = consumer.receive();
      assertEquals("Exit1", message.getStringProperty("interceptor.1"));
      message = consumer.receive();
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));     
      
      // remove interceptorsIN
      user1.removeInterceptorsIN("joram.interceptors.Exit1");
      
      // send message
      producer1.send(session1.createTextMessage("test interceptors 1"));
      producer2.send(session2.createTextMessage("test interceptors 2"));
      // receive message
      message = consumer.receive();
      assertEquals(null, message.getStringProperty("interceptor.1"));
      message = consumer.receive();
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));
      
      // remove interceptorsIN
      user2.removeInterceptorsIN("joram.interceptors.Exit2");
      
      connection1.stop();
      connection2.stop();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}

