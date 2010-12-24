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

import java.util.Properties;

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
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;

import framework.TestCase;

/**
 * Testing:
 *
 * - Add interceptors OUT.
 * The interceptors Exit1 and Exit2 add a property on message.
 * The interceptor Exit3 set msg to null.
 * 
 * - Get interceptors, verify it's ok.
 * 
 * send and receive message, verify no message receive.
 * read and check the message on the DMQ (with the user1)
 * - Remove interceptor Exit3.
 * 
 * send and receive message, and check the added properties.
 * - Remove interceptors.
 * 
 */
public class Test3 extends TestCase {

  public static void main(String[] args) {
    new Test3().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);
      User user1 = User.create("anonymous1", "anonymous1", 0);

      Queue queue = Queue.create("queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      Queue dmq = Queue.create("dmq");
      dmq.setFreeReading();
      dmq.setFreeWriting();
      user.setDMQId(dmq.getName());
      
      Properties prop = new Properties();
      prop.put("interceptorsOUT", "joram.interceptors.Exit1,joram.interceptors.Exit2,joram.interceptors.Exit3");
      AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_ADD_INTERCEPTORS, prop);

      AdminCommandReply reply = (AdminCommandReply) AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_GET_INTERCEPTORS, null);
      String in = (String) reply.getProp().get("interceptorsOUT");

      assertEquals("joram.interceptors.Exit1,joram.interceptors.Exit2,joram.interceptors.Exit3", in);
      
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      Connection connection = cf.createConnection("anonymous", "anonymous"); 
      connection.start();
      
      //System.out.println("Create a session");
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      //System.out.println("Create a producer");
      MessageProducer producer = session.createProducer(queue);
      
      Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      //System.out.println("Create a message listener");
      MessageConsumer consumer = session1.createConsumer(queue);

      // create DMQ connection, session and Consumer
      Connection cnx_dmq = cf.createConnection("anonymous1", "anonymous1"); 
      cnx_dmq.start();
      Session session_dmq = cnx_dmq.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_dmq = session_dmq.createConsumer(dmq);
      
      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      Message message = consumer.receive(100);
      assertEquals(null, message);
      
      // read the message on the DMQ
      TextMessage msg = (TextMessage) cons_dmq.receive();
      if (msg == null)
      	fail("no message on user DMQ.");
      else
      	assertEquals("test interceptors", msg.getText());
      
      // remove interceptorsOUT
      prop = new Properties();
      prop.put("interceptorsOUT", "joram.interceptors.Exit3");
      AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_REMOVE_INTERCEPTORS, prop);
      
      // send message
      producer.send(session.createTextMessage("test interceptors 1"));
      // receive message
      message = consumer.receive(100);
      if (message == null)
      	fail("message is null (interceptors Exit3 not removed)");
      assertEquals("Exit1", message.getStringProperty("interceptor.1"));
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));
      assertEquals("test interceptors 1", ((TextMessage) message).getText());
      
      //System.out.println("Concurrent close of the connection");
      connection.stop();
      cnx_dmq.stop();
      //System.out.println("Connection closed");
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}

