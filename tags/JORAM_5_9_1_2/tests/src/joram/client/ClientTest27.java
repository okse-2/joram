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
public class ClientTest27 extends TestCase {
  public static void main(String[] args) {
    new ClientTest27().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous", 0);

      Topic topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
      jndiCtx.rebind("topic", topic);
      jndiCtx.rebind("cf", cf);
      jndiCtx.close();
      
//      Thread t = new Thread() {
//        public void run() {
//          try {
//            for (int i=0; i<10; i++)
//              test(nbmsgs);
//          } catch (Exception e) {
//            e.printStackTrace();
//          }
//        }
//      };
//      t.start();

      for (int i=0; i<10; i++)
        test(nbmsgs);

      int subt = -1;
      int i = 0;
      do {
        Thread.sleep(pending);
        Subscription[] subs = user.getSubscriptions();
        int subu = (subs==null)?0:subs.length;
        subt = topic.getSubscriptions();
        System.out.println(new Date() + " - Sub: " + subt + ", " + subu);
      } while ((subt != 0) && (i++ < 5));
      assertTrue(topic.getSubscriptions()==0);

      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();
    }
  }
  
  final static long timeout = 100L;
  final static int nbmsgs = 100;
  // With such a value for cnxPendingTimer the subscription cleaning of a round should always
  // occurs during the next round.
  final static int pending = (int) (nbmsgs*timeout/2);
  
  void test(final int msgs) throws Exception {
    try {
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Topic topic = (Topic) jndiCtx.lookup("topic");
      ConnectionFactory cf = (ConnectionFactory) jndiCtx.lookup("cf");
      jndiCtx.close();

      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = session.createProducer(topic);
      cnx.start();

      Thread t = new Thread() {
        public void run() {
          try {
            Process p = startProcess("joram.client.ClientTest27Listener", null, null);
            System.out.println(new Date() + " - start process");
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
        prod.send(msg);
        Thread.sleep(timeout);
      }
      cnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
