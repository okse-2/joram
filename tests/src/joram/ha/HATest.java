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

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;

import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * Test HA servers with external client either with a queue and a topic. The test
 * starts 3 replicas then successively kills and restarts each  during sending and
 * receiving messages. The test verifies that none message is lost. 
 */
public class HATest extends HABaseTest {
  public static final int MESSAGE_NUMBER = 50;

  public static final int  pause = 100;

  public HATest() {
    super();
  }

  Process p0 = null;
  Process p1 = null;
  Process p2 = null;

  ConnectionFactory cf = null;
  
  public void run() {
    try {
      // Starts the 3 replicas
      
      pw.println("Start the replica 0");
      p0 = startHAServer((short) 0, null, "0");

      Thread.sleep(2000);

      pw.println("Start the replica 1");
      p1 = startHAServer((short) 0, null, "1");

      Thread.sleep(2000);

      pw.println("Start the replica 2");
      p2 = startHAServer((short) 0, null, "2");

      Thread.sleep(1000);

      // Connects to active replica (0) and creates the needed administered objects.
      cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 500;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 10;

      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous", 0);
      
      Queue queue = Queue.create(0, "queue");
      queue.setFreeReading();
      queue.setFreeWriting();
      
      Topic topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();

      runTest(queue);   
      runTest(topic);
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

  private void runTest(Destination dest) throws Exception {
    pw.println("Start test: " + dest);
    
    // Creates a connection with the master replica (0) and initializes a session
    // a producer and a consumer.
    Connection cnx = cf.createConnection("anonymous", "anonymous");
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = session.createProducer(dest);
    MessageConsumer consumer = session.createConsumer(dest);
    cnx.start();

    // Sends messages and schedule the dead of the master replica (0) during the
    // sending (approximately at the middle). The 1st slave replica (1) becomes master
    // and the sending phase ends.
    new Killer(p0, 0, pause * (MESSAGE_NUMBER / 2)).start();
    for (int i = 0; i < MESSAGE_NUMBER; i++) {
      TextMessage msg = session.createTextMessage();
      msg.setText("Test number1 " + i);
      producer.send(msg);
      pw.println("Msg sent: " + msg.getText());

      Thread.sleep(pause);
    }

    Thread.sleep(1000);

    // Restarts the killed replica.
    pw.println("Restart the replica 0");
    p0 = startHAServer((short) 0, null, "0");

    // Gets the sent messages and schedule the dead of the master replica (1) during the
    // receiving (approximately at the middle). The 1st slave replica (2) becomes master
    // and the receiving phase ends.
    new Killer(p1, 1, pause * (MESSAGE_NUMBER / 2)).start();
    for (int i = 0; i < MESSAGE_NUMBER; i++) {
      TextMessage msg = (TextMessage) consumer.receive();
      assertTrue(msg.getText().equals("Test number1 " +i));
      pw.println("Msg received: " + msg.getText());

      Thread.sleep(pause);
    }

    Thread.sleep(1000);

    // Sends messages using the master replica (2).
    for (int i = 0; i < MESSAGE_NUMBER; i++) {
      TextMessage msg = session.createTextMessage();
      msg.setText("Test number2 " + i);
      producer.send(msg);
    }
    pw.println(MESSAGE_NUMBER + " messages sent.");

    // Restarts the killed replica.
    pw.println("Start the replica 1");
    p1 = startHAServer((short) 0, null, "1");

    // Gets the sent messages and schedule the dead of the master replica (2) during the
    // receiving (approximately at the middle). The 1st slave replica (0) becomes master
    // and the receiving phase ends.
    new Killer(p2, 2, pause * (MESSAGE_NUMBER / 2)).start();
    for (int i = 0; i < MESSAGE_NUMBER; i++) {
      TextMessage msg = (TextMessage) consumer.receive();
      assertTrue(msg.getText().equals("Test number2 " +i));
      pw.println("Msg received: " + msg.getText());

      Thread.sleep(pause);
    }
    
    // Restarts the killed replica.
    pw.println("Start the replica 2");
    p1 = startHAServer((short) 0, null, "2");

    cnx.close();
  }

  public static void main(String args[]) {
    new HATest().run();
  }
}
