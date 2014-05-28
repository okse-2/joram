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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.admin.AdminCommandReply;

import com.scalagent.joram.mom.dest.collector.URLAcquisition;

import framework.TestCase;

/**
 * Tests changing acquisition mode with setProperties method.
 */
public class TestCollectorQueue2 extends TestCase implements MessageListener {

  private int nbReceived;
  String url1 = "http://www.gnu.org/licenses/lgpl-3.0-standalone.html";
  String url2 = "http://www.gnu.org/licenses/lgpl.txt";

  public static void main(String[] args) {
    new TestCollectorQueue2().run();
  }
  
  public void run() {
    try {
      startAgentServer((short) 0);

      admin();

      Context ictx = new InitialContext();
      Queue queue = (Queue) ictx.lookup("CollectorQueue");
      ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
      ictx.close();

      Connection cnx = cf.createConnection();
      Session sessionc = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Session sessionp = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      // create a consumer
      MessageConsumer consumer = sessionc.createConsumer(queue);
      MessageProducer producer = sessionp.createProducer(queue);

      // the consumer records on the queue
      consumer.setMessageListener(this);
      
      cnx.start();

      Thread.sleep(10000);
      
      assertTrue(nbReceived == 0);

      Message m = sessionp.createMessage();
      m.setStringProperty("collector.url", url1);
      producer.send(m);
      
      Properties prop = new Properties();
      prop.setProperty("expiration", "0");
      prop.setProperty("persistent", "true");
      prop.setProperty("acquisition.period", "5000");
      prop.setProperty("collector.url", url2);
      prop.setProperty("collector.type", "" + org.objectweb.joram.shared.messages.Message.BYTES);
      AdminCommandReply reply = (AdminCommandReply) queue.setProperties(prop);
      // System.out.println("reply = " + reply);
      
      Thread.sleep(12000);

      assertTrue(nbReceived >= 3);
      
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
    
    // create a queue
    Properties props = new Properties();
    props.put("acquisition.className", URLAcquisition.class.getName());
    Queue queue = Queue.create(0, "CollectorQueue", Destination.ACQUISITION_QUEUE, props);

    // create a user
    User.create("anonymous", "anonymous");
    
    // set permissions
    queue.setFreeReading();
    queue.setFreeWriting();

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    Context jndiCtx = new InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("CollectorQueue", queue);
    jndiCtx.close();
  }

  public void onMessage(Message message) {
    nbReceived++;
    try {
//      System.out.println("\n --> Message received :" + message + ", url = " + message.getStringProperty("collector.url"));
      if (nbReceived == 1) {
        assertTrue(url1.equals(message.getStringProperty("collector.url")));
      } else {
        assertTrue(url2.equals(message.getStringProperty("collector.url")));
      }
    } catch (JMSException exc) {
      addError(exc);
    }
  }
}
