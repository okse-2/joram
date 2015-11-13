/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2015 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.bridgejms;

import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.JMSDistributionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *  Test the JMS bridge with a specific architecture. This test use a unique flow with distribution and 
 * acquisition destinations and focuses on behavior with stop and restart of servers.
 */
public class BridgeTest15x3 extends TestCase {
  static BridgeTest15x3 test = null;
  
  public static void main(String[] args) {
    test = new BridgeTest15x3();
    test.run();
  }

  static final int ROUND = 50;
  static final int NBMSG = 5;
  
  int cpt = 0;
  
  public boolean testMsg(int m) {
    TestCase.assertTrue("Receices " + m + " should be " + cpt, cpt == m);
    if (m != cpt) {
      System.out.println("Receices " + m + " should be " + cpt);
      cpt = m;
    }
    cpt++;
    if ((cpt%100)==0) System.out.println("receives: " + cpt);

    synchronized(this) {
      if (cpt == ((round+1) * NBMSG))
        notify();
    }
    return true;
  }
  
  public synchronized void waitForEnd(long timeout) {
    try {
      if (cpt < ((round+1) * NBMSG)) {
        System.out.println("wait");
        wait(timeout);
      }
      System.out.println("wakeup: "+ cpt);
    } catch (InterruptedException exc) {}
  }

  int round = 0;
  
  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)2, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(5000);

      AdminTest15x3 admin = new AdminTest15x3();

      javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010); // s0
      admin.central(cf);

      javax.jms.ConnectionFactory cfA = TcpConnectionFactory.create("localhost", 16011); // s1
      admin.admin1(cfA);

      javax.jms.ConnectionFactory cfB = TcpConnectionFactory.create("localhost", 16012); // s2
      admin.admin2(cfB);

      System.out.println("admin done");
      
      for (; round<ROUND; round++) {
        if (round != 0) {
          Thread.sleep(2000L);
          System.out.println("start servers");

          startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
          startAgentServer((short)2, new String[]{"-DTransaction.UseLockFile=false"});

          Thread.sleep(5000L);
        }
        System.out.println("run");
        
        Sender15x3 sndA = new Sender15x3(cfA);
        Receiver15x3 recvB = new Receiver15x3(cfB);

        for (int j=0; j<NBMSG; j++) {
            sndA.send("" + ((round*NBMSG) +j));
          Thread.sleep(10, 0);
        }

        waitForEnd(5000L);
        sndA.close();
        recvB.close();
        
        Thread.sleep(1000L);
        
        killAgentServer((short)1);
        killAgentServer((short)2);
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      killAgentServer((short)1);
      killAgentServer((short)2);
      endTest(); 
    }
  }
}

class AdminTest15x3 {
  javax.naming.Context jndiCtx = null;
  
  AdminTest15x3 () throws Exception {
    // Initializes JNDI context
    jndiCtx = new javax.naming.InitialContext();
  }
  
  public void admin1(ConnectionFactory cf) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    int sid = AdminModule.getLocalServerId();
    
    // Creates the local user
    User.create("anonymous", "anonymous");
    
    // Creates the distribution queue
    Properties props = new Properties();
    props.setProperty("period", "1000");     
    props.setProperty("jms.ConnectionUpdatePeriod", "1000");
    props.setProperty("distribution.async", "true");
    org.objectweb.joram.client.jms.Queue queue = JMSDistributionQueue.create(sid, "queue", "queue", props);
    queue.setFreeWriting();
    
    AdminModule.disconnect();
  }

  public void central(ConnectionFactory cf) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    
    jndiCtx.rebind("centralCF", cf);

    // Creates the local user
    User.create("anonymous", "anonymous");
    
    org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create("queue");
    queue.setFreeReading();
    queue.setFreeWriting();
    jndiCtx.rebind("queue", queue);

    jndiCtx.close();
    AdminModule.disconnect();
  }
  
  public void admin2(ConnectionFactory cf) throws Exception {
    // Connects to the server
    AdminModule.connect(cf);
    int sid = AdminModule.getLocalServerId();
    
    // Creates the local user
    User.create("anonymous", "anonymous");
    
    
    // Creates the acquisition queue
    Properties props = new Properties();
    props.setProperty("jms.ConnectionUpdatePeriod", "1000");
    props.setProperty("persistent", "true");
    props.setProperty("acquisition.max_msg", "50");
    props.setProperty("acquisition.min_msg", "20");
    props.setProperty("acquisition.max_pnd", "200");
    props.setProperty("acquisition.min_pnd", "50");
    org.objectweb.joram.client.jms.Queue queue = JMSAcquisitionQueue.create(sid, "queue", "queue", props);
    queue.setFreeReading();
    
    AdminModule.disconnect();
  }
}

class Sender15x3 {
  Connection cnx = null;
  Session session = null;
  MessageProducer prod = null;
  Queue queue;
  
  Sender15x3(ConnectionFactory cf) throws JMSException {
    this.cnx = cf.createConnection();
    cnx.start();
    
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    queue = session.createQueue("queue");
    prod = session.createProducer(queue);
    
    try {
      AdminModule.connect(cf);
      long recv = getCpt();
      if (recv != (BridgeTest15x3.test.round * BridgeTest15x3.NBMSG))
        System.out.println("NbMsgsReceiveSinceCreation=" + recv + " should be " + (BridgeTest15x3.test.round * BridgeTest15x3.NBMSG));
      TestCase.assertTrue("NbMsgsReceiveSinceCreation=" + recv + " should be " + (BridgeTest15x3.test.round * BridgeTest15x3.NBMSG),
                          recv == (BridgeTest15x3.test.round * BridgeTest15x3.NBMSG));
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
  
  void send(String text) throws JMSException {
    TextMessage msg = session.createTextMessage(text);
    prod.send(msg);
  }
  
  long getCpt() {
    try {
      ((org.objectweb.joram.client.jms.Queue) queue).setWrapper(AdminModule.getWrapper());
      Hashtable stats = ((org.objectweb.joram.client.jms.Queue) queue).getStatistics("DeliveredMessageCount,NbMsgsDeliverSinceCreation,NbMsgsReceiveSinceCreation,NbMsgsSentToDMQSinceCreation,PendingMessageCount");
      System.out.println(stats);
      return (Long) stats.get("NbMsgsReceiveSinceCreation");
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    return -1;
  }
  
  public void close() throws JMSException {
    try {
      long recv = getCpt();
      if (recv != ((BridgeTest15x3.test.round +1) * BridgeTest15x3.NBMSG))
        System.out.println("NbMsgsReceiveSinceCreation=" + recv + " should be " + ((BridgeTest15x3.test.round +1) * BridgeTest15x3.NBMSG));
      TestCase.assertTrue("NbMsgsReceiveSinceCreation=" + recv + " should be " + ((BridgeTest15x3.test.round +1) * BridgeTest15x3.NBMSG),
                          recv == ((BridgeTest15x3.test.round +1) * BridgeTest15x3.NBMSG));

      AdminModule.disconnect();
    } catch (Exception exc) {
      exc.printStackTrace();
    }

    session.close();
    cnx.close();
  }
}

class Receiver15x3 implements MessageListener {
  Connection cnx = null;
  Session session = null;
  MessageConsumer cons = null;
  Queue queue;
  
  Receiver15x3(ConnectionFactory cf) throws JMSException {
    this.cnx = cf.createConnection();
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue = session.createQueue("queue");

    cons = session.createConsumer(queue);
    cons.setMessageListener(this);
    
    cnx.start();
  }

  @Override
  public void onMessage(Message m) {
    try {
      TextMessage msg = (TextMessage) m;
      String str = msg.getText();
      BridgeTest15x3.test.testMsg(Integer.parseInt(str));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void close() throws JMSException {
    session.close();
    cnx.close();
  }
}

