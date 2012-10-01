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

public class HATest4 extends HABaseTest {
  public static int nbRound = 100;
  public static int msgPerRound = 50;

  public static int  pause = 1000;

  public String type = "topic";

  public HATest4() {
    super();
  }

  public static volatile boolean sending = true;

  public void run() {
    Process p[] = new Process[3];

    try {
      type = System.getProperty("dest", type);
      
      // Starts the 3 replicas
      
      pw.println("Start the replica 0");
      p[0] = CollocatedClient.startHACollocatedClient((short) 0, "0", type);
      
      Thread.sleep(2000);

      pw.println("Start the replica 1");
      p[1] = CollocatedClient.startHACollocatedClient((short) 0, "1", type);
      
      Thread.sleep(2000);

      pw.println("Start the replica 2");
      p[2] = CollocatedClient.startHACollocatedClient((short) 0, "2", type);

      Thread.sleep(2000);

      nbRound = Integer.getInteger("nbRound", nbRound).intValue();
      msgPerRound = Integer.getInteger("msgPerRound", msgPerRound).intValue();
      pause = Integer.getInteger("pause", pause).intValue();

      // Connects to active replica (0) and creates the needed administered objects.

      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 1000;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 60;

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

      new Sender(cf, dest, syncq).start();

      int i = 0;
      while (sending) {
        Thread.sleep(10 * pause);

        pw.println("Kill the replica " + i);
        p[i].destroy();

        Thread.sleep(10 * pause);

        pw.println("Start the replica " + i);
        p[i] = CollocatedClient.startHACollocatedClient((short) 0, "" + i, type);

        i = ((i +1) %3);
      }
    } catch (Exception exc) {
      exc.printStackTrace(pw);
      error(exc);
    } finally {
      if (p[0] != null) p[0].destroy();
      if (p[1] != null) p[1].destroy();
      if (p[2] != null) p[2].destroy();
      endTest();
    }
    System.out.println("end");
  }

  public static void main(String args[]) {
    new HATest4().run();
  }

  public static class Sender extends Thread {
    private Connection cnx;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer consumer;

    Sender(ConnectionFactory cf,
           Destination dest,
           Destination ackq) throws JMSException {
      cnx = cf.createConnection("anonymous", "anonymous");
      session = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = session.createProducer(dest);
      consumer = session.createConsumer(ackq);
      cnx.start();
    }

    public void run() {
      try {
        long start, end;
        TextMessage msg = null;
        int idx = 0, idx2 = 0;
        for (int i=0; i<nbRound; i++) {
          int j;
          start = System.currentTimeMillis();
          for (j=0; j<msgPerRound; j++) {
            msg = session.createTextMessage("message #" + i + '.' + j);
            msg.setIntProperty("index", idx++);
            producer.send(msg);
          }
          end = System.currentTimeMillis();
          pw.println("Round #" + i + " - " + (end -start));
          
          Thread.sleep(pause);

          int nb = 0;
          while (true) {
            msg = (TextMessage) consumer.receive(5000);
            if (msg == null) break;
            if (msg.getText().equals("started")) continue;
            nb++;
            
            int x = msg.getIntProperty("index");
            assertTrue("Bad index", x == idx2);
            idx2 = x +1;
          }
          assertTrue("Round #" + i + ": " + nb  + " messages", nb == msgPerRound);
        }
      } catch (Exception exc) {
        exc.printStackTrace();
        error(exc);
      } finally {
        sending = false;
      }
    }
  }
}
