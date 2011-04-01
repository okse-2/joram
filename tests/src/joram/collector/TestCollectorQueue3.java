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
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.admin.AdminCommandConstant;

import framework.TestCase;

/**
 * Tests start and stop command.
 */
public class TestCollectorQueue3 extends TestCase implements MessageListener {

  private int nbReceived;

  public static void main(String[] args) {
    new TestCollectorQueue3().run();
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
      cnx.start();

      // create a producer and a consumer
      MessageConsumer consumer = sessionc.createConsumer(queue);

      // the consumer records on the queue
      consumer.setMessageListener(this);
      
      AdminModule.connect("localhost", 2560, "root", "root", 60);
      AdminModule.processAdmin(queue.getName(), AdminCommandConstant.CMD_STOP_HANDLER, null);
      
      AdminModule.processAdmin(queue.getName(), AdminCommandConstant.CMD_START_HANDLER, null);
      AdminModule.disconnect();
      
      Thread.sleep(100);

      assertEquals(3, nbReceived);
      
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
    
    Properties properties = new Properties();
    properties.setProperty("expiration", "0");
    properties.setProperty("persistent", "true");
    properties.setProperty("acquisition.className", EmptyAcquisiton.class.getName());
        
    // create a Queue   
    Queue queue = Queue.create(0, "CollectorQueue", Destination.ACQUISITION_QUEUE, properties);

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

    AdminModule.disconnect();
  }

  public void onMessage(Message message) {
    nbReceived++;
    try {
      System.out.println("\n --> Message received :" + message + ", status = " + message.getStringProperty("collector.status"));
      if (nbReceived == 1 || nbReceived == 3)
        assertTrue("start".equals(message.getStringProperty("collector.status")));
      else if (nbReceived == 2)
        assertTrue("stop".equals(message.getStringProperty("collector.status")));
    } catch (JMSException exc) {
      addError(exc);
    }
  }
}
