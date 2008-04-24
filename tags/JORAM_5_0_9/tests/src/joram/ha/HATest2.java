/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.ha;

import java.io.File;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.ha.tcp.TopicHATcpConnectionFactory;

public class HATest2 extends TestCase {

  public static final int MESSAGE_NUMBER = 20;

  public HATest2() {
    super();
  }

  public void run() {
    Process p0 = null;
    Process p1 = null;
    Process p2 = null;
    try {
      File r0 = new File("r0");
      r0.mkdir();

      File r1 = new File("r1");
      r1.mkdir();

      File r2 = new File("r2");
      r2.mkdir();

      System.out.println("Start the replica 0");
      p0 = startAgentServer((short) 0, r0, new String[] {
          "-DnbClusterExpected=1",
          "-DTransaction=fr.dyade.aaa.util.NullTransaction",
          "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.." },
          new String[] { "0" });
      
      try {
        Thread.sleep(4000);
      } catch (InterruptedException exc) {
      }

      System.out.println("Start the replica 1");
      p1 = startAgentServer((short) 0, r1, new String[] {
          "-DnbClusterExpected=1",
          "-DTransaction=fr.dyade.aaa.util.NullTransaction",
          "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.." },
          new String[] { "1" });

     Thread.sleep(4000);

      TopicConnectionFactory cf = TopicHATcpConnectionFactory
          .create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().cnxPendingTimer = 500;
      ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().connectingTimer = 30;
      AdminModule.connect(cf, "root", "root");

      Topic topic = org.objectweb.joram.client.jms.Topic.create(0, "topic");
      ((org.objectweb.joram.client.jms.Topic) topic).setFreeReading();
      ((org.objectweb.joram.client.jms.Topic) topic).setFreeWriting();

      org.objectweb.joram.client.jms.admin.User user = org.objectweb.joram.client.jms.admin.User
          .create("anonymous", "anonymous", 0);

      AdminModule.disconnect();

      Connection cnx = cf.createConnection("root", "root");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(topic);
      cnx.start();

      new HATest.Killer(p0, 0, 500 * (MESSAGE_NUMBER / 2)).start();

      Thread.sleep(8000);
      
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        producer.send(msg);
         System.out.println("Msg sent: " + msg.getText());
        Thread.sleep(500);
      }

      System.out.println("Start the replica 2");
      p2 = startAgentServer((short) 0, r2, new String[] {
          "-DnbClusterExpected=1",
          "-DTransaction=fr.dyade.aaa.util.NullTransaction",
          "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.." },
          new String[] { "2" });

      Thread.sleep(8000);

      new HATest.Killer(p1, 1, 500 * (MESSAGE_NUMBER / 2)).start();

      for (int i = MESSAGE_NUMBER; i < MESSAGE_NUMBER * 2; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
         System.out.println("Msg sent: " + msg.getText());
        producer.send(msg);
        Thread.sleep(500);
      }

      System.out.println("Start the replica 0");
      p0 = startAgentServer((short) 0, r0, new String[] {
          "-DnbClusterExpected=1",
          "-DTransaction=fr.dyade.aaa.util.NullTransaction",
          "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=.." },
          new String[] { "0" });

      Thread.sleep(8000);

      new HATest.Killer(p2, 2, 500 * (MESSAGE_NUMBER / 2)).start();

      for (int i = MESSAGE_NUMBER * 2; i < MESSAGE_NUMBER * 3; i++) {
        TextMessage msg = session.createTextMessage("message #" + i);
        producer.send(msg);
         System.out.println("Msg sent: " + msg.getText());
        Thread.sleep(500);
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

  public static Process startAgentServer(short sid, File dir, String[] jvmarg,
      String[] servarg) throws Exception {
    return HATest.startAgentServer(sid, dir, jvmarg, servarg,
        "joram.ha.CollocatedClient");
  }

  public static void main(String args[]) {
    new HATest2().run();
  }

}
