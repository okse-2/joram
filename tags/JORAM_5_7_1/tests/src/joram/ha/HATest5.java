/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2009 ScalAgent Distributed Technologies
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
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;

public class HATest5 extends HABaseTest {
  public static int nbRound = 100;
  public static int msgPerRound = 50;

  public static int  pause = 1000;

  public String type = "topic";

  public HATest5() {
    super();
  }

  public static volatile int round = 0;

  public void run() {

    try {
      // Starts the 3 replicas
      
      pw.println("Start the replica 0");
      startHAServer((short) 0, (short) 0);
      
      Thread.sleep(2000);

      pw.println("Start the replica 1");
      startHAServer((short) 0, (short) 1);
      
      Thread.sleep(2000);

      pw.println("Start the replica 2");
      startHAServer((short) 0, (short) 2);

      Thread.sleep(1000);

      nbRound = Integer.getInteger("nbRound", nbRound).intValue();
      msgPerRound = Integer.getInteger("msgPerRound", msgPerRound).intValue();
      pause = Integer.getInteger("pause", pause).intValue();

      // Connects to active replica (0) and creates the needed administered objects.
      
      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 500;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 10;

      AdminModule.connect(cf, "root", "root");

      User.create("anonymous", "anonymous", 0);

      type = System.getProperty("name", type);
      Destination dest = null;
      if (type.equals("queue")) {
        dest = Queue.create(0, "queue");
      } else {
        dest = Topic.create(0, "topic");
      }
      dest.setFreeReading();
      dest.setFreeWriting();

      AdminModule.disconnect();

      new Receiver(cf, dest).start();
      Thread.sleep(500);
      new Sender(cf, dest, pause).start();      

      int i = 0;
      while (round < nbRound) {
        Thread.sleep(15 * pause);

        if (round < nbRound) {
          pw.println("Kill the replica " + i);
          killAgentServer((short) i);

          Thread.sleep(10 * pause);
        }

        if (round < nbRound) {
          pw.println("Start the replica " + i);
          startHAServer((short) 0, (short) i);
        }

        i = ((i +1) %3);

        pw.println(" --- round = " + round);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      killAgentServer((short) 0);
      killAgentServer((short) 1);
      killAgentServer((short) 2);
      endTest();
    }
    System.out.println("end");
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
          pw.println("Sender - Round #" + i + " - " + (end -start));
          Thread.sleep(pause);

          if ((round +5) < i) {
            pw.println("Sender - Pause");
            Thread.sleep(5 * pause);
          }
        }
      } catch (Exception exc) {
        exc.printStackTrace(pw);
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
              pw.println("Message lost #" + (idx +1) + " - " + idx2);
            }
            idx = idx2;
          }
          end = System.currentTimeMillis();
          pw.println("Receiver - Round #" + i + " - " + (end -start));
          round += 1;
        }
      } catch (Exception exc) {
        exc.printStackTrace(pw);
        error(exc);
      } finally {
        // Allow to end the test.
        round = nbRound;
      }
    }
  }

}
