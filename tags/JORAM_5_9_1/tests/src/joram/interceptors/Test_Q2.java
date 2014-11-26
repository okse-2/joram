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
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Testing:
 *
 * - Add queue interceptors.
 * The interceptors Exit1 and Exit2 add a property on message.
 * The interceptor Exit3 set msg to null.
 * 
 * - Get queue interceptors, verify it's ok.
 * 
 * send and receive message, verify no message receive.
 * read and check the message on the DMQ
 * - Remove interceptor Exit3.
 * 
 * send and receive message, and check the added properties.
 * - Remove interceptors.
 * 
 */
public class Test_Q2 extends TestCase {

  public static void main(String[] args) {
    new Test_Q2().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      Queue queue = Queue.create("queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      Queue dmq = Queue.create("dmq");
      dmq.setFreeReading();
      dmq.setFreeWriting();
      queue.setDMQId(dmq.getName());
      
      // add interceptors
      queue.addInterceptors("joram.interceptors.Exit1,joram.interceptors.Exit2,joram.interceptors.Exit3");
      
      // get interceptors
      assertEquals("joram.interceptors.Exit1,joram.interceptors.Exit2,joram.interceptors.Exit3", queue.getInterceptors());
      
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      Connection connection = cf.createConnection("anonymous", "anonymous"); 
      connection.start();
      
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(queue);
      
      Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session1.createConsumer(queue);

      // create DMQ session
      Session session_dmq = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_dmq = session_dmq.createConsumer(dmq);
      
      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      Message message = consumer.receive(100);
      assertEquals(null, message);
      
      // read the message on the DMQ
      TextMessage msg = (TextMessage) cons_dmq.receive();
      if (msg == null)
      	fail("no message on DMQ.");
      else
      	assertEquals("test interceptors", msg.getText());

      // remove interceptors
      queue.removeInterceptors("joram.interceptors.Exit3");
      
      // send message
      producer.send(session.createTextMessage("test interceptors 1"));
      // receive message
      message = consumer.receive(100);
      if (message == null)
      	fail("message is null (interceptors Exit3 not removed)");
      assertEquals("Exit1", message.getStringProperty("interceptor.1"));
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));
      
      connection.stop();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}

