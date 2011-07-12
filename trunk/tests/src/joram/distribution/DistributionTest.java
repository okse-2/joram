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
package joram.distribution;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

public class DistributionTest extends TestCase {

  public static void main(String[] args) throws Exception {
    new DistributionTest().run();
  }

  public void run() {

    try {

      System.out.println("Administration...");

      startAgentServer((short) 0);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().connectingTimer = 60;
      AdminModule.connect(cf);

      Properties prop = new Properties();
      prop.put("distribution.className", DistributionHandlerTest.class.getName());
      prop.put("period", Long.toString(1000));
      Queue distributionQueue = Queue.create(0, null, Destination.DISTRIBUTION_QUEUE, prop);

      distributionQueue.setThreshold(10);

      prop = new Properties();
      prop.put("acquisition.className", AcquisitionHandlerTest.class.getName());
      prop.put("acquisition.period", Integer.toString(200));
      Queue acquisitionQueue = Queue.create(0, null, Destination.ACQUISITION_QUEUE, prop);

      Queue dmq = Queue.create();
      dmq.setFreeReading();
      dmq.setFreeWriting();
      distributionQueue.setDMQ(dmq);

      User.create("anonymous", "anonymous", 0);

      distributionQueue.setFreeWriting();
      try {
        distributionQueue.setFreeReading();
        assertTrue(false);
      } catch (Exception exc) {
        // OK
      }

      acquisitionQueue.setFreeReading();

      AdminModule.disconnect();
      System.out.println("Admin done.");
      
      // ------- Admin done

      Connection connection = cf.createConnection("anonymous", "anonymous");
      connection.start();

      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(distributionQueue);

      Session session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session1.createConsumer(acquisitionQueue);

      // send message
      Message msg = session.createTextMessage("test distribution");
      producer.send(msg);

      msg = consumer.receive(1000);
      assertNull(msg);

      // Redelivery attempt every second - should work the fifth time (see Handler)
      msg = consumer.receive(10000);
      assertNotNull(msg);
      assertEquals("test distribution - MODIFIED", ((TextMessage) msg).getText());

      AdminModule.connect(cf);
      // Change threshold, next message will go to DMQ
      distributionQueue.setThreshold(3);

      // send message
      msg = session.createTextMessage("test distribution 2");
      producer.send(msg);

      Thread.sleep(5000);
      assertEquals(1, dmq.getPendingMessages());

      AdminModule.disconnect();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
