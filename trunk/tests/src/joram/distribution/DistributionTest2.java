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
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

public class DistributionTest2 extends TestCase {

  // /!\ Must be odd to work properly.
  static final int MSG_COUNT = 9;

  public static void main(String[] args) throws Exception {
    new DistributionTest2().run();
  }

  public void run() {

    try {

      System.out.println("Administration...");

      startAgentServer((short) 0);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().connectingTimer = 60;
      AdminModule.connect(cf);

      Properties prop = new Properties();
      prop.put("distribution.className", DistributionHandlerTest2.class.getName());
      prop.put("period", Long.toString(1000));
      Queue distributionQueue = Queue.create(0, null, Destination.DISTRIBUTION_QUEUE, prop);

      prop = new Properties();
      prop.put("acquisition.className", AcquisitionHandlerTest2.class.getName());
      prop.put("acquisition.period", Integer.toString(200));
      Queue acquisitionQueue = Queue.create(0, null, Destination.ACQUISITION_QUEUE, prop);

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

      MessageListener listener = new MessageListener1();
      consumer.setMessageListener(listener);

      // send message
      for (int i = 0; i < MSG_COUNT; i++) {
        Message msg = session.createTextMessage(Integer.toString(i));
        producer.send(msg);
      }

      Thread.sleep(15000);

      assertEquals(MSG_COUNT, ((MessageListener1) listener).count);

      session1.close();

      // *********** Test batch mode
      AdminModule.connect(cf);
      prop = new Properties();
      prop.put("distribution.batch", "true");
      prop.put("period", Long.toString(1000));
      distributionQueue.setProperties(prop);
      AdminModule.disconnect();

      session1 = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      consumer = session1.createConsumer(acquisitionQueue);

      listener = new MessageListener2();
      consumer.setMessageListener(listener);

      // send message
      for (int i = 0; i < MSG_COUNT; i++) {
        Message msg = session.createTextMessage(Integer.toString(i));
        producer.send(msg);
      }

      Thread.sleep(5000);

      assertEquals(MSG_COUNT, ((MessageListener2) listener).count);

      connection.close();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }

  class MessageListener1 implements MessageListener {

    int lastId = -1;
    long lastTime = -1;
    int count = 0;

    public void onMessage(Message msg) {
      try {
        // Check ordering
        int newId = Integer.parseInt(((TextMessage) msg).getText());
        System.out.println("MessageListener: " + newId);
        long time = System.currentTimeMillis();
        assertEquals(lastId + 1, newId);

        if (newId % 2 == 0) {
          // This one is even (so quick, see Handler)
          if (lastTime != -1) {
            assertTrue(time - lastTime < 300);
          }
        } else {
          // This one is odd (so lenghty, see Handler)
          if (lastTime != -1) {
            assertTrue(time - lastTime >= 2000);
          }
        }

        lastId = newId;
        lastTime = time;
        count++;
      } catch (Exception exc) {
        error(exc);
      }
    }
  }

  class MessageListener2 implements MessageListener {

    int lastId = -2;
    long lastTime = -1;
    int count = 0;

    public void onMessage(Message msg) {
      try {
        // Ordering is lost
        int newId = Integer.parseInt(((TextMessage) msg).getText());
        System.out.println("MessageListener2: " + newId);
        long time = System.currentTimeMillis();

        // All even messages first
        if (newId % 2 == 0) {
          assertEquals(lastId + 2, newId);
        } else {
          // last even message
          if (lastId != MSG_COUNT - 1) {
            assertEquals(lastId + 2, newId);
          }
        }

        // the messages make 2 groups: even messages then odd
        if (lastTime != -1 && lastId != MSG_COUNT - 1) {
          assertTrue(" result: " + (time - lastTime), time - lastTime < 300);
        }

        lastId = newId;
        lastTime = time;
        count++;
      } catch (Exception exc) {
        error(exc);
      }
    }
  }
}
