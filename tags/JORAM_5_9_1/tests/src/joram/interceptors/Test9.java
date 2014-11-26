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
import org.objectweb.joram.shared.security.SimpleIdentity;

import framework.TestCase;

/**
 * Testing:
 *
 * - create user with interceptor contains properties at creation
 *  send and receive message, and verify the right added properties (status = onCreate).
 * 
 * - remove interceptor
 * - add interceptor with properties on the user
 *  send and receive message, and verify the right added properties (status = onAdd).
 *  
 * - replace interceptor with properties
 *  send and receive message, and verify the right added properties (status = onReplace).
 *  
 * Do this test for the interceptor IN and OUT.
 *
 */
public class Test9 extends TestCase {

  public static void main(String[] args) {
    new Test9().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      
      ////////////////
      // Test interceptor with init properties on user creation
      Properties prop = new Properties();
      prop.setProperty(AdminCommandConstant.INTERCEPTORS_IN, "joram.interceptors.Exit5");
      prop.setProperty("testName", "test9");
      prop.setProperty("status", "onCreate");
      // Create user anonymous with interceptor Exit5
      User user = User.create("anonymous", "anonymous", 0, SimpleIdentity.class.getName(), prop);

      Queue queue = Queue.create(0);
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
      
      user.removeInterceptorsIN("joram.interceptors.Exit5");
      
      ////////////////
      // Test interceptor with init properties use the addInterceptor
      prop.setProperty("status", "onAdd");
      // add interceptor Exit5
      user.addInterceptorIN("joram.interceptors.Exit5", prop);
      
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
      user.replaceInterceptorIN("joram.interceptors.Exit5", "joram.interceptors.Exit5", prop);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      session.commit();
      // receive message
      message = consumer.receive();
      session1.commit();
      assertEquals("onReplace", message.getStringProperty("status"));

      connection.stop();
      
      //****************
      // Test with interceptor OUT
      ////////////////
      // Test interceptor with init properties on user creation
      prop.clear();
      prop.setProperty(AdminCommandConstant.INTERCEPTORS_OUT, "joram.interceptors.Exit5");
      prop.setProperty("testName", "test9");
      prop.setProperty("status", "onCreate");
      // Create user anonymous with interceptor Exit5
      user = User.create("anonymous1", "anonymous1", 0, SimpleIdentity.class.getName(), prop);

      connection = cf.createConnection("anonymous1", "anonymous1"); 
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = session.createProducer(queue);

      session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      consumer = session1.createConsumer(queue);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      message = consumer.receive();
      assertEquals("onCreate", message.getStringProperty("status"));

      user.removeInterceptorsOUT("joram.interceptors.Exit5");

      ////////////////
      // Test interceptor with init properties use the addInterceptor
      prop.setProperty("status", "onAdd");
      // add interceptor Exit5
      user.addInterceptorOUT("joram.interceptors.Exit5", prop);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      message = consumer.receive();
      assertEquals("onAdd", message.getStringProperty("status"));

      ////////////////
      // Test interceptor with init properties use the replaceInterceptor
      prop.setProperty("status", "onReplace");
      // add interceptor Exit5
      user.replaceInterceptorOUT("joram.interceptors.Exit5", "joram.interceptors.Exit5", prop);

      // send message
      producer.send(session.createTextMessage("test interceptors"));
      // receive message
      message = consumer.receive();
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

