/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.admin.AdminCommandConstant;

import framework.TestCase;

/**
 * Testing:
 *
 * - create queue with interceptor contains properties at creation
 *  send and receive message, and verify the right added properties (status = onCreate).
 * 
 * - remove interceptor
 * - add interceptor with properties on the queue
 *  send and receive message, and verify the right added properties (status = onAdd).
 *  
 * - replace interceptor with properties
 *  send and receive message, and verify the right added properties (status = onReplace).
 *
 */
public class Test_Q7 extends TestCase {

  public static void main(String[] args) {
    new Test_Q7().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      
      // Create user anonymous 
      User user = User.create("anonymous", "anonymous");
      
      Properties prop = new Properties();
      prop.setProperty(AdminCommandConstant.INTERCEPTORS, "joram.interceptors.Exit5");
      prop.setProperty("testName", "test9");
      prop.setProperty("status", "onCreate");

      // Create queue with interceptor Exit5
      Queue queue = Queue.create(0, prop);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      Connection connection = cf.createConnection("anonymous", "anonymous"); 
      connection.start();
      
      Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(queue);
      
      Session session1 = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session1.createConsumer(queue);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      session.commit();
      // receive message
      Message message = consumer.receive();
      session1.commit();
      assertEquals("onCreate", message.getStringProperty("status"));
      
      queue.removeInterceptors("joram.interceptors.Exit5");
      
      ////////////////
      // Test interceptor with init properties use the addInterceptor
      prop.setProperty("status", "onAdd");
      // add interceptor Exit5
      queue.addInterceptor("joram.interceptors.Exit5", prop);
      
      // send message
      producer.send(session.createTextMessage("test interceptors"));
      session.commit();
      // receive message
      message = consumer.receive();
      session1.commit();
      assertEquals("onAdd", message.getStringProperty("status"));

      ////////////////
      // Test interceptor with init properties use the replaceInterceptor
      prop.setProperty("status", "onReplace");
      // add interceptor Exit5
      queue.replaceInterceptor("joram.interceptors.Exit5", "joram.interceptors.Exit5", prop);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      session.commit();
      // receive message
      message = consumer.receive();
      session1.commit();
      assertEquals("onReplace", message.getStringProperty("status"));

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

