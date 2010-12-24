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

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;

import framework.TestCase;

/**
 * Testing:
 *
 * - Add interceptors IN and OUT.
 * This interceptors add a property on message.
 * 
 * - Get interceptors, verify it's ok.
 * 
 * send and receive message, and verify the right added properties.
 *
 * - Remove interceptors.
 * 
 * send and receive message, and check property.
 */
public class Test1 extends TestCase {

  public static void main(String[] args) {
    new Test1().run();
  }

  public void run() {
    try {
      startAgentServer( (short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      Properties prop = new Properties();
      prop.put("interceptorsIN", "joram.interceptors.Exit1");
      prop.put("interceptorsOUT", "joram.interceptors.Exit2");
      // add interceptors
      AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_ADD_INTERCEPTORS, prop);

      // get interceptors
      AdminCommandReply reply = (AdminCommandReply) AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_GET_INTERCEPTORS, null);
      String in = (String) reply.getProp().get("interceptorsIN");
      String out = (String) reply.getProp().get("interceptorsOUT");
      assertEquals("joram.interceptors.Exit1", in);
      assertEquals("joram.interceptors.Exit2", out);
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      Connection connection = cf.createConnection("anonymous", "anonymous"); 
      connection.start();
      
      //System.out.println("Create a session");
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      
      //System.out.println("Create a producer");
      MessageProducer producer = session.createProducer(queue);
      
      Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      //System.out.println("Create a message listener");
      MessageConsumer consumer = session1.createConsumer(queue);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      Message message = consumer.receive();
      assertEquals("Exit1", message.getStringProperty("interceptor.1"));
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));     
      
      // remove interceptorsIN
      prop = new Properties();
      prop.put("interceptorsIN", "joram.interceptors.Exit1");
      AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_REMOVE_INTERCEPTORS, prop);
      
      // send message
      producer.send(session.createTextMessage("test interceptors 1"));
      // receive message
      message = consumer.receive();
      assertEquals(null, message.getStringProperty("interceptor.1"));
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));
      
      // remove interceptorsOUT
      prop.put("interceptorsOUT", "joram.interceptors.Exit2");
      AdminModule.processAdmin(user.getProxyId(), AdminCommandConstant.CMD_REMOVE_INTERCEPTORS, prop);
      
      //System.out.println("Concurrent close of the connection");
      connection.stop();
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

