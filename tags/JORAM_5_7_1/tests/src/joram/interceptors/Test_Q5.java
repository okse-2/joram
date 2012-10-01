/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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

import framework.TestCase;

/**
 * Testing:
 *
 * - Add interceptors on Queue creation.
 * This interceptors add a property on message.
 * 
 * stop and restart AgentServer.
 * 
 * - Get interceptors, verify it's ok.
 * 
 * send and receive message, and verify the right added properties.
 *
 * - Remove interceptors.
 * 
 * send and receive message, and check property.
 */
public class Test_Q5 extends TestCase {

  public static void main(String[] args) {
    new Test_Q5().run();
  }

  public void run() {
    try {
      startAgentServer( (short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);

      Properties prop = new Properties();
      prop.put(AdminCommandConstant.INTERCEPTORS, "joram.interceptors.Exit1");
      
      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0, prop);
      queue.setFreeReading();
      queue.setFreeWriting();

      // STOP and RESTART
      stopAgentServer((short) 0);
      Thread.sleep(100);
      startAgentServer( (short)0);
      
      try {
      	AdminModule.disconnect();
      	AdminModule.connect("localhost", 2560, "root", "root", 60);
      } catch (Exception e) {
      	System.out.println("EXCEPTION : " + e);
      }
      
      // get interceptors
      assertEquals("joram.interceptors.Exit1", queue.getInterceptors());
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      Connection connection = cf.createConnection("anonymous", "anonymous"); 
      connection.start();
      
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(queue);
      
      Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session1.createConsumer(queue);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      Message message = consumer.receive();
      assertEquals("Exit1", message.getStringProperty("interceptor.1"));
      
      // remove interceptors
      queue.removeInterceptors("joram.interceptors.Exit1");
      
      // send message
      producer.send(session.createTextMessage("test interceptors 1"));
      // receive message
      message = consumer.receive();
      assertEquals(null, message.getStringProperty("interceptor.1"));
      
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

