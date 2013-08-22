/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.monitoring;

import java.util.Enumeration;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.MonitoringQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Tests modifying parameters monitored by the MonitoringQueue.
 */
public class TestMonitoringQueue2 extends TestCase implements MessageListener {

  private int nbReceived;

  public static void main(String[] args) {
    new TestMonitoringQueue2().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);

      admin();

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("MonitoringQueue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // create a producer and a consumer
      MessageConsumer consumer = sessionc.createConsumer(queue);
      MessageProducer producer = sessionp.createProducer(queue);

      // the consumer records on the queue
      consumer.setMessageListener(this);
      
      cnx.start();

      Thread.sleep(3000);
      
      assertTrue(nbReceived == 0);
      
      // Launch an acquisition
      Message m = sessionp.createMessage();
      m.setStringProperty("AgentServer:server=AgentServer#0,cons=Transaction", "LogMemorySize,GarbageRatio");
      producer.send(m);
      
      Thread.sleep(3000);

      assertTrue(nbReceived == 1);
      
      // Change mode
      Properties prop = new Properties();
      prop.setProperty("acquisition.period", "2000");
      prop.setProperty("AgentServer:server=AgentServer#0,cons=Transaction", "LogFileSize,NbLoadedObjects");
      queue.setProperties(prop);
      
      Thread.sleep(10000);

      assertTrue(nbReceived > 3);
      
      AdminModule.disconnect();
      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }

  /**
   * Admin : Create queue and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // connection 
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    
    Queue queue = MonitoringQueue.create(0, "MonitoringQueue");

    // create a user
    User.create("anonymous", "anonymous");
    
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    Context jndiCtx = new InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("MonitoringQueue", queue);
    jndiCtx.close();
  }

  public void onMessage(Message message) {
    nbReceived++;
//    System.out.println("\n --> Message received :" + message);
    int nbMonitoringResults = 0;
    try {
      Enumeration enumNames = message.getPropertyNames();
      while (enumNames.hasMoreElements()) {
        String name = (String) enumNames.nextElement();
        if ((name != null) && (name.startsWith("JMS"))) continue;
        nbMonitoringResults++;
//        System.out.println(name + " : " + message.getObjectProperty(name));
      }
      assertEquals(2, nbMonitoringResults);
    } catch (JMSException exc) {
      addError(exc);
    }
    assertEquals(2, nbMonitoringResults);
  }
}
