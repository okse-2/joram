/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2008 - 2011 ScalAgent Distributed Technologies
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
import org.objectweb.joram.shared.admin.AdminCommandReply;

import com.scalagent.joram.mom.dest.collector.URLAcquisition;

import framework.TestCase;

/**
 * Tests modifying parameters monitored by the CollectorTopic.
 */
public class TestCollectorTopic2 extends TestCase implements MessageListener {

  private int nbReceived;
  String url = null;

  public static void main(String[] args) {
    new TestCollectorTopic2().run();
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

      // create a consumer
      MessageConsumer consumer = sessionc.createConsumer(topic);

      // the consumer records on the topic
      consumer.setMessageListener(this);
      
      cnx.start();

      Thread.sleep(10000);
      
      assertTrue(nbReceived == 0);

      url = "http://www.gnu.org/licenses/lgpl.txt";
      Properties prop = new Properties();
      prop.setProperty("expiration", "0");
      prop.setProperty("persistent", "true");
      prop.setProperty("acquisition.period", "5000");
      prop.setProperty("collector.url", url);
      prop.setProperty("collector.type", "" + org.objectweb.joram.shared.messages.Message.BYTES);
      AdminCommandReply reply = (AdminCommandReply) topic.setProperties(prop);
      // System.out.println("reply = " + reply);
      
      Thread.sleep(12000);

      assertTrue(nbReceived >= 2);
      
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
   * Admin : Create topic and a user anonymous use jndi
   */
  public void admin() throws Exception {
    // connection 
    AdminModule.connect("localhost", 2560, "root", "root", 60);
    
    // create a Topic
    Properties props = new Properties();
    props.put("acquisition.className", URLAcquisition.class.getName());
    Topic topic = Topic.create(0, "CollectorTopic", Destination.ACQUISITION_TOPIC, props);

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
  }

  public void onMessage(Message message) {
    nbReceived++;
    try {
//      System.out.println("\n --> Message received :" + message + ", url = " + message.getStringProperty("collector.url"));
      assertTrue(url.equals(message.getStringProperty("collector.url")));
    } catch (JMSException exc) {
      addError(exc);
    }
  }
}
