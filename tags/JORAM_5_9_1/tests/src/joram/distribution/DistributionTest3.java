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

/**
 * Test distribution async mode (ON and OFF) on a distribution Queue.
 */
public class DistributionTest3 extends TestCase {

  // /!\ Must be odd to work properly.
  static final int MSG_COUNT = 30;

  public static void main(String[] args) throws Exception {
    new DistributionTest3().run();
  }

  public void run() {

    try {
      System.out.println("Administration...");

      startAgentServer((short) 0);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().connectingTimer = 60;
      AdminModule.connect(cf);

      Properties prop = new Properties();
      prop.put("distribution.className", DistributionHandlerTest3.class.getName());
      prop.put("distribution.async", "true");
      prop.put("period", Long.toString(2000));
      Queue distributionQueue = Queue.create(0, null, Destination.DISTRIBUTION_QUEUE, prop);
      System.out.println("distributionQueue = " + distributionQueue);

      prop = new Properties();
      prop.put("acquisition.className", AcquisitionHandlerTest3.class.getName());
      prop.put("acquisition.period", Integer.toString(200));
      Queue acquisitionQueue = Queue.create(0, null, Destination.ACQUISITION_QUEUE, prop);

      User.create("anonymous", "anonymous", 0);

      distributionQueue.setFreeWriting();
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

      System.out.println("Async ON...");
      // send message
      for (int i = 0; i < MSG_COUNT; i++) {
        Message msg = session.createTextMessage(Integer.toString(i));
        producer.send(msg);
      }

      Thread.sleep(30000);

      assertEquals(MSG_COUNT, ((MessageListener1) listener).count);

      System.out.println("Async OFF...");
      // set async OFF
      AdminModule.connect(cf);
      prop = new Properties();
      prop.put("distribution.async", "false");
      prop.put("period", Long.toString(1000));
      distributionQueue.setProperties(prop);
      AdminModule.disconnect();
      Thread.sleep(1000);
      
      for (int i = MSG_COUNT; i < 2*MSG_COUNT; i++) {
        Message msg = session.createTextMessage(Integer.toString(i));
        producer.send(msg);
      }
      
      Thread.sleep(5000);
      assertEquals(2*MSG_COUNT, ((MessageListener1) listener).count);
      
      System.out.println("Async ON...");
      // set async ON
      AdminModule.connect(cf);
      prop = new Properties();
      prop.put("distribution.async", "true");
      prop.put("period", Long.toString(1000));
      distributionQueue.setProperties(prop);
      AdminModule.disconnect();
      Thread.sleep(1000);
      
      for (int i = 2*MSG_COUNT; i < 3*MSG_COUNT; i++) {
        Message msg = session.createTextMessage(Integer.toString(i));
        producer.send(msg);
      }
      
      Thread.sleep(5000);
      assertEquals(3*MSG_COUNT, ((MessageListener1) listener).count);
      
      System.out.println("close...");
      session.close();
      session1.close();
      System.out.println("close cnx");
      connection.close();
      System.out.println("cnx closed");
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
        
        lastId = newId;
        lastTime = time;
        count++;
      } catch (Exception exc) {
        error(exc);
      }
    }
  }
}
