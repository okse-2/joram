/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):
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
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Tests modifying parameters monitored by the MonitoringTopic.
 */
public class TestMonitoringTopic2 extends TestCase implements MessageListener {

  private int nbReceived;
  private int nbMonitoringResults;

  public static void main(String[] args) {
    new TestMonitoringTopic2().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      admin();
      System.out.println("admin config ok");

      Context ictx = new InitialContext();
      Topic topic = (Topic) ictx.lookup("topic");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageConsumer consumer = sessionc.createConsumer(topic);
      MessageProducer producer = sessionp.createProducer(topic);

      // the consumer records on the topic
      consumer.setMessageListener(this);
      
      Thread.sleep(10000);
      
      Message msg = sessionp.createMessage();
      msg.setStringProperty("MBeanMonitoring:AgentServer:server=AgentServer#0,cons=Transaction",
          "LogMemorySize,   GarbageRatio");
      producer.send(msg);
      
      Thread.sleep(10000);

      assertTrue(nbReceived > 0);
      assertEquals(2, nbMonitoringResults);
      
      cnx.close();
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
      
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  /**
   * Admin : Create topic and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // connection 
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    
    Properties topicProps = new Properties();
    topicProps.put("MBeanMonitoring:AgentServer:agent=JoramAdminTopic,*", "AgentId");
    topicProps.put("period", "2000");
    
    // create a Topic   
    org.objectweb.joram.client.jms.Topic topic = org.objectweb.joram.client.jms.Topic.create(0,
        "MonitoringTopic", "org.objectweb.joram.mom.dest.MonitoringTopic", topicProps);

    // create a user
    User.create("anonymous", "anonymous");
    
    // set permissions
    topic.setFreeReading();
    topic.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    Context jndiCtx = new InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
  }

  public void onMessage(Message message) {
    nbReceived++;
    System.out.println(" ");
    System.out.println(" --> Message received :");
    nbMonitoringResults = 0;
    try {
      Enumeration enumNames = message.getPropertyNames();
      while (enumNames.hasMoreElements()) {
        nbMonitoringResults++;
        String name = (String) enumNames.nextElement();
        System.out.println(name + " : " + message.getObjectProperty(name));
      }
    } catch (JMSException exc) {
      addError(exc);
    }
  }
}
