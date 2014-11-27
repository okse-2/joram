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
import org.objectweb.joram.shared.security.SimpleIdentity;

import framework.TestCase;

/**
 * Testing:
 *
 * - Add user interceptors IN and OUT at creation.
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
public class Test5 extends TestCase {

  public static void main(String[] args) {
    new Test5().run();
  }

  public void run() {
    try {
      startAgentServer( (short)0);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      Properties prop = new Properties();
      prop.put(AdminCommandConstant.INTERCEPTORS_IN, "joram.interceptors.Exit1");
      prop.put(AdminCommandConstant.INTERCEPTORS_OUT, "joram.interceptors.Exit2");
      
      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0, SimpleIdentity.class.getName(), prop);

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();
      
      // get interceptors
      assertEquals("joram.interceptors.Exit1", user.getInterceptorsIN());
      assertEquals("joram.interceptors.Exit2", user.getInterceptorsOUT());
      
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
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));     
      
      // remove interceptorsIN
      user.removeInterceptorsIN("joram.interceptors.Exit1");
      
      // send message
      producer.send(session.createTextMessage("test interceptors 1"));
      // receive message
      message = consumer.receive();
      assertEquals(null, message.getStringProperty("interceptor.1"));
      assertEquals("Exit2", message.getStringProperty("interceptor.2"));
      
      // remove interceptorsOUT
      user.removeInterceptorsOUT("joram.interceptors.Exit2");
      
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

