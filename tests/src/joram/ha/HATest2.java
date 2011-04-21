/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
package joram.ha;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;

/**
 * Test HA servers with colocated client either with a queue and a topic. The test
 * starts 3 replicas then successively kills and restarts each  during sending and
 * receiving messages. The test verifies that none message is lost. 
 */
public class HATest2 extends HABaseTest {
  public static final int MESSAGE_NUMBER = 50;

  public static final int  pause = 100;
  
  public HATest2() {
    super();
  }

  public void run() {
    Process p0 = null;
    Process p1 = null;
    Process p2 = null;

    try {
      String type = System.getProperty("dest", "topic");
      
      // Starts the 3 replicas

      pw.println("Start the replica 0");
      p0 = CollocatedClient.startHACollocatedClient((short) 0, "0", type);

      Thread.sleep(2000);

      pw.println("Start the replica 1");
      p1 = CollocatedClient.startHACollocatedClient((short) 0, "1", type);

      Thread.sleep(2000);

      pw.println("Start the replica 2");
      p2 = CollocatedClient.startHACollocatedClient((short) 0, "2", type);

      Thread.sleep(1000);

      // Connects to active replica (0) and creates the needed administered objects.
      
      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 500;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 10;

      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);

      Destination dest = null;
      if (type.equals("queue")) {
        dest = Queue.create(0, "queue");
      } else {
        dest = Topic.create(0, "topic");
      }
      dest.setFreeReading();
      dest.setFreeWriting();

      Queue syncq = Queue.create(0, "syncq");
      syncq.setFreeReading();
      syncq.setFreeWriting();

      AdminModule.disconnect();

      Thread.sleep(1000L);

      pw.println("Start test: " + dest);
      
      // Creates a connection with the master replica (0) and initializes a session
      // a producer and a consumer.      
      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(dest);
      MessageConsumer consumer = session.createConsumer(syncq);
      cnx.start();
      
      // Waits for the CollocatedClient
      TextMessage ack = (TextMessage) consumer.receive(10000);
      if (ack == null) {
        assertTrue("No ack from client", false);
        return;
      }
      if (! ack.getText().equals("started")) {
        assertTrue("Bad ack from client: " + ack.getText(), false);
        return;
      }
      
      // Sends messages and schedule the dead of the master replica and colocated receiver (0)
      // during the sending (approximately at the middle). The 1st slave replica (1) becomes
      // master and the sending phase ends.
      // The messages are partly received by colocated receiver of each master replicas (0 then 1),
      // the test verify that all sent messages are received.
      new ProcessKiller((short) 0, p0, pause * (MESSAGE_NUMBER / 2)).start();
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        msg.setIntProperty("index", i);
        producer.send(msg);
        pw.println("Msg sent: " + msg.getText());
        Thread.sleep(pause);
      }

      // Restarts the killed replica.
      pw.println("Start the replica 0");
      p0 = CollocatedClient.startHACollocatedClient((short) 0, "0", type);

      Thread.sleep(2000);

      // Sends messages and schedule the dead of the master replica and colocated receiver (1)
      // during the sending (approximately at the middle). The 1st slave replica (2) becomes
      // master and the sending phase ends.
      // The messages are partly received by colocated receiver of each master replicas (1 then 2),
      // the test verify that all sent messages are received.
      new ProcessKiller((short) 1, p1, pause * (MESSAGE_NUMBER / 2)).start();
      for (int i = MESSAGE_NUMBER; i < MESSAGE_NUMBER * 2; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        msg.setIntProperty("index", i);
        pw.println("Msg sent: " + msg.getText());
        producer.send(msg);
        Thread.sleep(pause);
      }

      // Restarts the killed replica.
      pw.println("Start the replica 1");
      p1 = CollocatedClient.startHACollocatedClient((short) 0, "1", type);

      Thread.sleep(2000);

      // Sends messages and schedule the dead of the master replica and colocated receiver (2)
      // during the sending (approximately at the middle). The 1st slave replica (1) becomes
      // master and the sending phase ends.
      // The messages are partly received by colocated receiver of each master replicas (2 then 0),
      // the test verify that all sent messages are received.
      new ProcessKiller((short) 2, p2, pause * (MESSAGE_NUMBER / 2)).start();
      for (int i = MESSAGE_NUMBER * 2; i < MESSAGE_NUMBER * 3; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        msg.setIntProperty("index", i);
        producer.send(msg);
        pw.println("Msg sent: " + msg.getText());
        Thread.sleep(pause);
      }
      
      int i=0;
      for (; i < 3 * MESSAGE_NUMBER; ) {
        TextMessage msg = (TextMessage) consumer.receive(10000);
        if (msg == null) break;
        if (msg.getText().equals("started")) continue;
        
        int idx2 = msg.getIntProperty("index");
        assertTrue("Bad index", idx2 == i);
        i = idx2 +1;
      }
      assertTrue("Received " + i  + " messages", i == (3 * MESSAGE_NUMBER));

    } catch (Exception exc) {
      exc.printStackTrace(pw);
      error(exc);
    } finally {
      if (p0 != null) p0.destroy();
      if (p1 != null) p1.destroy();
      if (p2 != null) p2.destroy();
      endTest();
    }
  }

  public static void main(String args[]) {
    new HATest2().run();
  }

}
