/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
import javax.jms.TextMessage;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

public class HATest5 extends TestCase {
  public static int nbRound = 1000;
  public static int msgPerRound = 100;

  public static int  pause = 1000;

  public String name = "topic";

  public HATest5() {
    super();
  }

  public static volatile int round = 0;

  public void run() {
    Process p[] = new Process[3];

    try {
      System.out.println("Start the replica 0");
      p[0] = startHAServer((short) 0, null, "0");
      
      Thread.sleep(2000);

      System.out.println("Start the replica 1");
      p[1] = startHAServer((short) 0, null, "1");

      System.out.println("Start the replica 2");
      p[2] = startHAServer((short) 0, null, "2");

      Thread.sleep(1000);

      nbRound = Integer.getInteger("nbRound", nbRound).intValue();
      msgPerRound = Integer.getInteger("msgPerRound", msgPerRound).intValue();
      pause = Integer.getInteger("pause", pause).intValue();

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 1000;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 60;

      Topic topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();

      Queue queue = Queue.create(0, "queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      name = System.getProperty("name", name);
      Destination dest = null;
      if (name.equals("queue")) {
        dest = queue;
      } else {
        dest = topic;
      }

      AdminModule.disconnect();

      new Receiver(cf, dest).start();
      Thread.sleep(500);
      new Sender(cf, dest, pause).start();      

      int i = 0;
      while (round < nbRound) {
        Thread.sleep(15 * pause);

        if (round < nbRound) {
          System.out.println("Kill the replica " + i);
          p[i].destroy();

          Thread.sleep(10 * pause);
        }

        if (round < nbRound) {
          System.out.println("Start the replica " + i);
          p[i] = startHAServer((short) 0, null, "" + i);
        }

        i = ((i +1) %3);

        System.out.println(" --- round = " + round);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      if (p[0] != null) p[0].destroy();
      if (p[1] != null) p[1].destroy();
      if (p[2] != null) p[2].destroy();
      endTest();
    }
    System.out.println("end");
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
    new HATest5().run();
  }

  public static class Sender extends Thread {
    private Connection cnx;
    private Session session;
    private MessageProducer producer;
    private long pause;

    Sender(ConnectionFactory cf,
           Destination dest,
           int pause) throws JMSException {
      this.pause = pause;

      cnx = cf.createConnection("anonymous", "anonymous");
      session = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = session.createProducer(dest);
      cnx.start();
    }

    public void run() {
      try {
        long start, end;
        TextMessage msg = null;
        int idx = 0;
        for (int i=0; i<nbRound; i++) {
          start = System.currentTimeMillis();
          for (int j=0; j<msgPerRound; j++) {
            msg = session.createTextMessage("message #" + i + '.' + j);
            msg.setIntProperty("index", idx++);
            producer.send(msg);
          }
          end = System.currentTimeMillis();
          System.out.println("Sender - Round #" + i + " - " + (end -start));
          Thread.sleep(pause);

          if ((round +5) < i) {
            System.out.println("Sender - Pause");
            Thread.sleep(5 * pause);
          }
        }
      } catch (Exception exc) {
        exc.printStackTrace();
        error(exc);
      } finally {
        // Allow to end the test.
        round = nbRound;
      }
    }
  }

  public static class Receiver extends Thread {
    private Connection cnx;
    private Session session;
    private MessageConsumer consumer;

    Receiver(ConnectionFactory cf,
             Destination dest) throws JMSException {
      cnx = cf.createConnection("anonymous", "anonymous");
      session = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      session.setTopicActivationThreshold(50);
      session.setTopicPassivationThreshold(150);
      session.setTopicAckBufferMax(10);
      consumer = session.createConsumer(dest);
      cnx.start();
    }

    public void run() {
      try {
        long start, end;
        TextMessage msg = null;
        int idx = 0;
        for (int i=0; i<nbRound; i++) {
          start = System.currentTimeMillis();
          for (int j=0; j<msgPerRound; j++) {
            msg = (TextMessage) consumer.receive();

            int idx2 = msg.getIntProperty("index");
            if ((idx != -1) && (idx2 != idx +1)) {
              System.out.println("Message lost #" + (idx +1) + " - " + idx2);
            }
            idx = idx2;
          }
          end = System.currentTimeMillis();
          System.out.println("Receiver - Round #" + i + " - " + (end -start));
          round += 1;
        }
      } catch (Exception exc) {
        exc.printStackTrace();
        error(exc);
      } finally {
        // Allow to end the test.
        round = nbRound;
      }
    }
  }

}
