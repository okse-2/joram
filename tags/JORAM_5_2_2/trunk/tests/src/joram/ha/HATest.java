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

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;
import org.objectweb.joram.client.jms.Queue;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

public class HATest extends TestCase {
  public static final int MESSAGE_NUMBER = 50;

  public static final int  pause = 100;

  public HATest() {
    super();
  }

  public void run() {
    Process p0 = null;
    Process p1 = null;
    Process p2 = null;

    try {
      System.out.println("Start the replica 0");
      p0 = startHAServer((short) 0, null, "0");

      Thread.sleep(2000);

      System.out.println("Start the replica 1");
      p1 = startHAServer((short) 0, null, "1");

      System.out.println("Start the replica 2");
      p2 = startHAServer((short) 0, null, "2");

      Thread.sleep(1000);

      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 500;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 30;

      AdminModule.connect(cf, "root", "root");
//      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      Queue queue = Queue.create(0, "queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      AdminModule.disconnect();

      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(queue);
      MessageConsumer consumer = session.createConsumer(queue);
      cnx.start();

      new Killer(p0, 0, pause * (MESSAGE_NUMBER / 2)).start();

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = session.createTextMessage();
        msg.setText("Test number1 " + i);
        producer.send(msg);

        Thread.sleep(pause);
      }

      Thread.sleep(1000);

      new Killer(p1, 1, pause * (MESSAGE_NUMBER / 2)).start();

      System.out.println("Restart the replica 0");
      p0 = startHAServer((short) 0, null, "0");

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = (TextMessage) consumer.receive();
        assertTrue(msg.getText().equals("Test number1 " +i));

        Thread.sleep(pause);
      }

      Thread.sleep(1000);

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = session.createTextMessage();
        msg.setText("Test number2 " + i);
        producer.send(msg);
      }

      new Killer(p2, 2, pause * (MESSAGE_NUMBER / 2)).start();

      System.out.println("Start the replica 1");
      p1 = startHAServer((short) 0, null, "1");

      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        TextMessage msg = (TextMessage) consumer.receive();
        assertTrue(msg.getText().equals("Test number2 " +i));

        Thread.sleep(pause);
      }
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
  }

  public static Process startHAServer(short sid,
                                      File dir,
                                      String rid) throws Exception {
    String[] jvmargs = new String[] {
      "-DnbClusterExpected=2",
      "-DTransaction=fr.dyade.aaa.util.NullTransaction"};

    String[] args = new String[] { rid };

    Process p =  getAdmin().execAgentServer(sid, dir,
                                            jvmargs,
                                            "fr.dyade.aaa.agent.AgentServer",
                                            args);
    getAdmin().closeServerStream(p);

    return p;
  }

  public static void main(String args[]) {
    new HATest().run();
  }

  public static class Killer extends Thread {
    private Process process;
    private int index;
    private long pause;

    Killer(Process process, int index, long pause) {
      this.process = process;
      this.index = index;
      this.pause = pause;
    }

    public void run() {
      try {
        Thread.sleep(pause);
      } catch (InterruptedException exc) {
      }
      System.out.println("Kill replica " + index);
      process.destroy();
    }
  }
}
