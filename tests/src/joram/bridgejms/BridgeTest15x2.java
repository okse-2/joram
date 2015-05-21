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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *  Test the JMS bridge with a specific architecture using 2 separate flows with distribution and 
 * acquisition destinations (see the architecture description in Test15x-Archi.jpg). This test
 * focuses on behavior with stop and restart of servers.
 */
public class BridgeTest15x2 extends TestCase {
  public static void main(String[] args) {
    new BridgeTest15x2().run();
  }

  static final int ROUND = 50;
  static final int NBMSG = 20;
  
  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)2, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(5000);

      AdminTest15x admin = new AdminTest15x();

      javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010); // s0
      admin.centralAdmin(cf, "A:B");

      javax.jms.ConnectionFactory cfA = TcpConnectionFactory.create("localhost", 16011); // s1
      admin.localAdmin(cfA, "A");

      javax.jms.ConnectionFactory cfB = TcpConnectionFactory.create("localhost", 16012); // s2
      admin.localAdmin(cfB, "B");

      Connection cnx1 = cf.createConnection();
      Forward15x2 fwdA = new Forward15x2(cnx1, "A");
      cnx1.start();
      Connection cnx2 = cf.createConnection();
      Forward15x2 fwdB = new Forward15x2(cnx2, "B");
      cnx2.start();
      
      for (int i=0; i<ROUND; i++) {
        Connection cnxA = cfA.createConnection();
        Receiver15x2 recvA = new Receiver15x2("A", cnxA, i);
        Sender15x2 sndA = new Sender15x2("A", cnxA);
        cnxA.start();

        Connection cnxB = cfB.createConnection();
        Receiver15x2 recvB = new Receiver15x2("B", cnxB, i);
        Sender15x2 sndB = new Sender15x2("B", cnxB);
        cnxB.start();

        for (int j=0; j<NBMSG; j++) {
            sndA.send("A#" + ((i*NBMSG) +j));
            sndB.send("B#" + ((i*NBMSG) +j));
          Thread.sleep(10, 0);
        }

        sndA.close(); sndB.close();

        recvA.waitForEnd(10000L);
        recvB.waitForEnd(10000L);
        
        recvA.close();
        recvB.close(); 
        
        cnxA.close();
        cnxB.close();
        
        
        killAgentServer((short)1);
        killAgentServer((short)2);

        Thread.sleep(2000L);
        
        startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
        startAgentServer((short)2, new String[]{"-DTransaction.UseLockFile=false"});

        Thread.sleep(10000L);
      }
      
      fwdA.close(); cnx1.close();
      fwdB.close(); cnx2.close();
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

class Forward15x2 implements MessageListener {
  Connection cnx = null;
  Session session = null;
  MessageConsumer cons = null;
  MessageProducer prod = null;
  Queue queue1, queue2;

  Forward15x2(Connection cnx, String client) throws JMSException {
    this.cnx = cnx;
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue1 = session.createQueue("queue1" + client);
    queue2 = session.createQueue("queue2" + client);

    cons = session.createConsumer(queue1);
    prod = session.createProducer(queue2);
    cons.setMessageListener(this);
  }

  @Override
  public void onMessage(Message msg) {
    try {
//      System.out.println("Central receives: " + msg);
      prod.send(msg);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
  
  public void close() throws JMSException {
    session.close();
  }
}

class Sender15x2 {
  String name = null;
  Connection cnx = null;
  Session session = null;
  MessageProducer prod = null;
  Queue queue1;
  
  Sender15x2(String name, Connection cnx) throws JMSException {
    this.name = name;
    this.cnx = cnx;
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue1 = session.createQueue("queue1");
    prod = session.createProducer(queue1);
  }
  
  void send(String text) throws JMSException {
    TextMessage msg = session.createTextMessage(text);
    prod.send(msg);
  }
    
  public void close() throws JMSException {
    session.close();
  }
}

class Receiver15x2 implements MessageListener {
  String name = null;
  Connection cnx = null;
  Session session = null;
  MessageConsumer cons = null;
  Queue queue2;

  int round = 0;
  int cpt = 0;
  
  static DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

  Receiver15x2(String name, Connection cnx, int round) throws JMSException {
    this.name = name + '#';
    this.cnx = cnx;
    this.round = round;
    this.cpt = round * BridgeTest15x2.NBMSG;
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

    queue2 = session.createQueue("queue2");

    cons = session.createConsumer(queue2);
    cons.setMessageListener(this);
  }

  @Override
  public void onMessage(Message m) {
    try {
      TextMessage msg = (TextMessage) m;
      String str = msg.getText();
      TestCase.assertTrue("Should receive #" + cpt, str.equals(name + cpt));
      if (! str.equals(name + cpt)) {
        System.out.println(name + cpt + " receives " + str + " - " + fmt.format(new Date()));
        cpt = Integer.parseInt(str.substring(name.length()));
      }
      cpt++;
      if ((cpt%100)==0) System.out.println(name + cpt + " - " + fmt.format(new Date()));

      synchronized(this) {
        if (cpt == ((round+1) * BridgeTest15x2.NBMSG))
          notify();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public synchronized void waitForEnd(long timeout) {
    try {
      int nbtry = 30;
      while ((cpt < ((round+1) * BridgeTest15x2.NBMSG)) && (nbtry > 0)) {
        System.out.println(name + cpt + " waits");
        wait(1000L); nbtry--;
      }
    } catch (InterruptedException exc) {}
    if (cpt < ((round+1) * BridgeTest15x2.NBMSG))
      System.out.println(name + cpt + " ends");
  }
  
  public void close() throws JMSException {
    if (cpt != ((round+1) * BridgeTest15x2.NBMSG))
      System.out.println(name + " receives " + cpt + " should be " + ((round+1) * BridgeTest15x2.NBMSG));
    TestCase.assertTrue(name + " receives " + cpt + " should receive " + ((round+1) * BridgeTest15x2.NBMSG), cpt == ((round+1) * BridgeTest15x2.NBMSG));
    session.close();
  }
}

