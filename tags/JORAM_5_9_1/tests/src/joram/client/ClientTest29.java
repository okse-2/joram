/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
package joram.client;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Test memory leak with subscription and connection closes.
 *  -
 */
public class ClientTest29 extends TestCase {
  public static void main(String[] args) {
    new ClientTest29().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous", 0);
      User root = User.create("root", "root", 0);

      Topic topic1 = Topic.create(0, "topic1");
      topic1.setFreeReading();
      topic1.setFreeWriting();

      Topic topic2 = Topic.create(0, "topic2");
      topic2.setFreeReading();
      topic2.setFreeWriting();

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
      jndiCtx.rebind("topic1", topic1);
      jndiCtx.rebind("topic2", topic2);
      jndiCtx.rebind("cf", cf);
      jndiCtx.close();

      Thread t = new Thread() {
        public void run() {
          try {
            for (int i=0; i<100; i++)
              test(nbmsgs);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      t.start();

      int subt1 = -1;
      int subt2 = -1;
      do {
        Thread.sleep(pending);
        Subscription[] subs = root.getSubscriptions();
        int subu = (subs==null)?0:subs.length;
        subt1 = topic1.getSubscriptions();
        subt2 = topic2.getSubscriptions();
        System.out.println(new Date() + " - Sub: " + subt1 + ", " + subt2 + ", " + subu);
      } while ((subt1 != 0) || (subt2 != 0));

      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();
    }
  }
  
  final static long timeout = 10L;
  final static int nbmsgs = 1000;
  // With such a value for cnxPendingTimer the subscription cleaning of a round should always
  // occurs during the next round.
  final static int pending = (int) (nbmsgs*timeout/2);
  
  void test(final int msgs) throws Exception {
    try {
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Topic topic1 = (Topic) jndiCtx.lookup("topic1");
      Topic topic2 = (Topic) jndiCtx.lookup("topic2");
      ConnectionFactory cf = (ConnectionFactory) jndiCtx.lookup("cf");
      jndiCtx.close();

      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod1 = session.createProducer(topic1);
      MessageProducer prod2 = session.createProducer(topic2);
      cnx.start();

      Thread t = new Thread() {
        public void run() {
          try {
            Process p = startProcess("joram.client.ClientTest29Listener", null, null);
            System.out.println(new Date() + " - start process " + p);
            Thread.sleep((2*timeout*msgs)/5);
            System.out.println(new Date() + " - destroy process");
            p.destroy();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      t.start();

      int i = 0;
      while (i < msgs) {
        i++;
        Message msg = session.createMessage();
        msg.setIntProperty("counter", i);
        prod1.send(msg);
        msg = session.createMessage();
        msg.setIntProperty("counter", i);
        prod2.send(msg);
        Thread.sleep(timeout);
      }
      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
