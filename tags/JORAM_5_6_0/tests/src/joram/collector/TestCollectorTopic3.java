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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.collector;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.admin.AdminCommandConstant;

import framework.TestCase;

/**
 * Tests start and stop command.
 */
public class TestCollectorTopic3 extends TestCase implements MessageListener {

  private int nbReceived;

  public static void main(String[] args) {
    new TestCollectorTopic3().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0);

      admin();

      Context ictx = new InitialContext();
      Topic topic = (Topic) ictx.lookup("CollectorTopic");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      cnx.start();

      // create a producer and a consumer
      MessageConsumer consumer = sessionc.createConsumer(topic);

      // the consumer records on the topic
      consumer.setMessageListener(this);
      
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      AdminModule.processAdmin(topic.getName(), AdminCommandConstant.CMD_STOP_HANDLER, null);
      
      AdminModule.processAdmin(topic.getName(), AdminCommandConstant.CMD_START_HANDLER, null);
      AdminModule.disconnect();
      
      Thread.sleep(100);

      assertEquals(2, nbReceived);
      
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
   * Admin : Create topic and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // connection 
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    
    Properties properties = new Properties();
    properties.setProperty("expiration", "0");
    properties.setProperty("persistent", "true");
    properties.setProperty("acquisition.className", EmptyAcquisiton.class.getName());
        
    // create a Topic   
    Topic topic = Topic.create(0, "CollectorTopic", Destination.ACQUISITION_TOPIC, properties);

    // create a user
    User.create("anonymous", "anonymous");
    
    // set permissions
    topic.setFreeReading();
    topic.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    Context jndiCtx = new InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("CollectorTopic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
  }

  public void onMessage(Message message) {
    nbReceived++;
    try {
      System.out.println("\n --> Message received :" + message + ", status = " + message.getStringProperty("collector.status"));
      if (nbReceived == 2)
        assertTrue("start".equals(message.getStringProperty("collector.status")));
      else if (nbReceived == 1)
        assertTrue("stop".equals(message.getStringProperty("collector.status")));
    } catch (JMSException exc) {
      addError(exc);
    }
  }
}
