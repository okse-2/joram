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

public class HATest4 extends TestCase {
  public static int nbRound = 250;
  public static int msgPerRound = 25;

  public static int  pause = 1500;

  public String name = "topic";

  public HATest4() {
    super();
  }

  public static volatile boolean sending = true;

  public void run() {
    Process p[] = new Process[3];

    try {
      System.out.println("Start the replica 0");
      p[0] = startHACollocatedClient((short) 0, null, "0", name);
      
      Thread.sleep(2000);

      System.out.println("Start the replica 1");
      p[1] = startHACollocatedClient((short) 0, null, "1", name);

      System.out.println("Start the replica 2");
      p[2] = startHACollocatedClient((short) 0, null, "2", name);

      Thread.sleep(1000);

      nbRound = Integer.getInteger("nbRound", nbRound).intValue();
      msgPerRound = Integer.getInteger("msgPerRound", msgPerRound).intValue();
      pause = Integer.getInteger("pause", pause).intValue();

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
      ((HATcpConnectionFactory) cf).getParameters().cnxPendingTimer = 1000;
      ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 60;

      name = System.getProperty("name", name);
      Destination dest = null;
      if (name.equals("queue")) {
        dest = Queue.create(0, "queue");
      } else {
        dest = Topic.create(0, "topic");
      }
      dest.setFreeReading();
      dest.setFreeWriting();

      System.out.println("Destination " + dest);

      AdminModule.disconnect();

      new Sender(cf, dest, pause).start();      

      int i = 0;
      while (sending) {
        Thread.sleep(10 * pause);

        System.out.println("Kill the replica " + i);
        p[i].destroy();

        Thread.sleep(10 * pause);

        System.out.println("Start the replica " + i);
        p[i] = startHACollocatedClient((short) 0, null, "" + i, name);

        i = ((i +1) %3);
      }

      // Wait to enable the consumer to receive the messages.
      Thread.sleep(20 * pause);

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

  public static Process startHACollocatedClient(short sid,
                                                File dir,
                                                String rid,
                                                String name) throws Exception {
    String[] jvmargs = new String[] {
//       "-Xmx64m",
      "-DnbClusterExpected=2",
      "-DTransaction=fr.dyade.aaa.util.NullTransaction",
      "-D" + fr.dyade.aaa.util.Debug.DEBUG_DIR_PROPERTY + "=..",
      "-Dname=" + name};

    String[] args = new String[] { rid };

    Process p =  getAdmin().execAgentServer(sid, dir,
                                            jvmargs,
                                            "joram.ha.CollocatedClient",
                                            args);
    getAdmin().closeServerStream(p);

    return p;
  }

  public static void main(String args[]) {
    new HATest4().run();
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
          System.out.println("Round #" + i + " - " + (end -start));
          Thread.sleep(pause);
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
