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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * Verifies that the opening and closing of a great number of connections does not
 * cause memory leaks (see JORAM-20). This test is derived from the test number 60,
 * but adds the session creation and exchange of messages between the connections.
 * 
 * TODO (AF): How to verify that there is no memory leak ?
 */
public class Test62 extends TestCase {

  public static void main(String[] args) {
    new Test62().run();
  }

  public void run() {
    try {
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      Thread.sleep(1000);
      
      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
      
      AdminModule.connect(cf, "root", "root");
      
      // Create Queue
      Queue queue = Queue.create(0, "queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      // Create the anonymous user needed for test
      User.create("anonymous", "anonymous");
      
      AdminModule.disconnect();
      Thread.sleep(1000);

      System.gc();
      long m1 = Runtime.getRuntime().maxMemory();
      long m2 = Runtime.getRuntime().freeMemory();
      
      int tc0 = Thread.activeCount();
      System.out.println("Threads count = " + tc0);
//      Thread[] tarray = new Thread[50];
//      int tc = Thread.enumerate(tarray);
//      for (int i=0; i<tc; i++)
//        System.out.println("Thread[" + i + "] = " + tarray[i].getName());
      long start = System.currentTimeMillis();
      
      int tc1 = -1; int tc2 = -1;
      
      Consumer[] cons = new Consumer[10];
      Producer prod = new Producer(cf, queue);
      Thread prodt = new Thread(prod);
      prodt.start();
      for (int i=0; i<10000; i++) {
        if (i>9) {
          tc1 = Thread.activeCount();
          cons[i%10].close();
          cons[i%10] = null;
        }
        cons[i%10] = new Consumer(cf, queue);
        
//        if ((i%100) == 99) {
//          tc2 = Thread.activeCount();
//          assertTrue("Bad number of threads: " + tc2 + " != " + tc1, (tc2 == tc1));
//          System.out.println("#" + i + " - Threads count = " + tc2);
//        }
        Thread.sleep(5);
      }
      
      for (int i=0; i<10; i++) {
        cons[i].close();
        cons[i] = null;
      }
      prod.close();
      
      Thread.sleep(1000);
      
      long end = System.currentTimeMillis();
      tc2 = Thread.activeCount();
      assertTrue("Bad number of final threads: " + tc2 + " != " + tc0, (tc2 == tc0));
      System.out.println("Threads count = " + tc2);
//      tc = Thread.enumerate(tarray);
//      for (int i=0; i<tc; i++)
//        System.out.println("Thread[" + i + "] = " + tarray[i].getName());

      System.gc();
      System.gc();
      long m3 = Runtime.getRuntime().maxMemory();
      long m4 = Runtime.getRuntime().freeMemory();

      System.out.println("dt=" + (end-start) + ", m1=" + m1 + ", m2=" + m2 + ", m3=" + m3 + ", m4=" + m4);
//      Thread.sleep(120000);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();     
    }
  }
}

class Producer implements Runnable {
  Connection cnx = null;
  Session session = null;
  MessageProducer prod = null;
  
  Producer(ConnectionFactory cf, Destination dest) throws JMSException {
    cnx = cf.createConnection("anonymous", "anonymous");
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    prod = session.createProducer(dest);
    cnx.start();
  }
  
  public boolean running = false;
  
  public void run() {
    try {
      running = true;
      while (running) {
        Message msg = session.createMessage();
        prod.send(msg);
        Thread.sleep(1);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    } finally {
      try {
        cnx.close();
      } catch (JMSException exc) {
        exc.printStackTrace();
      }
    }
  }
  
  void close() throws JMSException {
    running = false;
  }
}

class Consumer implements MessageListener {
  Connection cnx = null;
  Session session = null;
  
  Consumer(ConnectionFactory cf, Destination dest) throws JMSException {
    cnx = cf.createConnection("anonymous", "anonymous");
    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer cons = session.createConsumer(dest);
    cons.setMessageListener(this);
    cnx.start();
  }
  
  public void onMessage(Message msg) {
  }
  
  void close() throws JMSException {
    cnx.close();
  }
}
