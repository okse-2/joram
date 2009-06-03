/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2008 ScalAgent Distributed Technologies
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

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;
import org.objectweb.joram.client.jms.Topic;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

public class HATest2 extends TestCase {
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
      System.out.println("Start the replica 0");
      p0 = startHACollocatedClient((short) 0, null, "0");
      
      Thread.sleep(2000);

      System.out.println("Start the replica 1");
      p1 = startHACollocatedClient((short) 0, null, "1");

      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 500;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 30;

      Topic topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();

      AdminModule.disconnect();

      Connection cnx = cf.createConnection("root", "root");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(topic);
      cnx.start();

      new HATest.Killer(p0, 0, pause * (MESSAGE_NUMBER / 2)).start();
      Thread.sleep(2000);
      
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        producer.send(msg);
        System.out.println("Msg sent: " + msg.getText());
        Thread.sleep(pause);
      }

      System.out.println("Start the replica 2");
      p2 = startHACollocatedClient((short) 0, null, "2");

      Thread.sleep(2000);

      new HATest.Killer(p1, 1, pause * (MESSAGE_NUMBER / 2)).start();

      for (int i = MESSAGE_NUMBER; i < MESSAGE_NUMBER * 2; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        System.out.println("Msg sent: " + msg.getText());
        producer.send(msg);
        Thread.sleep(pause);
      }

      System.out.println("Start the replica 0");
      p0 = startHACollocatedClient((short) 0, null, "0");

      Thread.sleep(2000);

      new HATest.Killer(p2, 2, pause * (MESSAGE_NUMBER / 2)).start();

      for (int i = MESSAGE_NUMBER * 2; i < MESSAGE_NUMBER * 3; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        producer.send(msg);
        System.out.println("Msg sent: " + msg.getText());
        Thread.sleep(pause);
      }

      // Wait to enable the consumer to receive the messages.
      Thread.sleep(5000);

    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      if (p0 != null)
        p0.destroy();
      if (p1 != null)
        p1.destroy();
      if (p2 != null)
        p2.destroy();
      endTest();
    }
    System.out.println("end");
  }

  public static Process startHACollocatedClient(short sid,
                                                File dir,
                                                String rid) throws Exception {
    String[] jvmargs = new String[] {
      "-DnbClusterExpected=1",
      "-DTransaction=fr.dyade.aaa.util.NullTransaction",
      "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.."};

    String[] args = new String[] { rid };

    Process p =  getAdmin().execAgentServer(sid, dir,
                                            jvmargs,
                                            "joram.ha.CollocatedClient",
                                            args);
    getAdmin().closeServerStream(p);

    return p;
  }

  public static void main(String args[]) {
    new HATest2().run();
  }

}
