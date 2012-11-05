/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;

/**
 *
 */
public class Test17 extends BaseTest implements ExceptionListener, MessageListener {
  ConnectionFactory cf;
  Destination dest;
  Connection cnx1;
  Session sess1;
  MessageConsumer cons;

  int nbExc = 0;

  public void onException(JMSException exc) {
//    System.out.println("onException");
    nbExc += 1;
    assertEquals("javax.jms.IllegalStateException", exc.getClass().getName());
  }
  
  int nbMsg = 0;
  int nbErr = 0;
  int idx = 0;
  
  public void onMessage(Message msg) {
    try {
      nbMsg += 1;
      int index = msg.getIntProperty("index");
      if (index != idx) {
//        System.out.println("recv#" + idx + '/' + index);
        nbErr += 1;
      }
      idx = index +1;
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }

  public static void main (String args[]) throws Exception {
    new Test17().run();
  }
  
  public synchronized void connect() {
    try {
//      System.out.println("connect");
      cnx1 = cf.createConnection();
      cnx1.setExceptionListener(this);
      sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      if (dest instanceof org.objectweb.joram.client.jms.Queue)
        cons = sess1.createConsumer(dest);
      else
        cons = sess1.createDurableSubscriber((Topic) dest, "sub");
      cons.setMessageListener(this);
      cnx1.start();
    } catch (JMSException exc2) {
      exc2.printStackTrace();
      error(exc2);
      AgentServer.stop();
      endTest();
    }
  }
  
  public synchronized void close() {    
    try {
//      System.out.println("close");
      if (cnx1 != null)
        cnx1.close();
      cnx1 = null;
      cons = null;
    } catch (JMSException exc2) {
      exc2.printStackTrace();
      error(exc2);
      AgentServer.stop();
      endTest();
    }
  }

  public void run(){
    try{
      timeout = 300000L;
      
      startServer();

      String baseclass = "joram.noreg.ColocatedBaseTest";
      baseclass = System.getProperty("BaseClass", baseclass);

      String destclass =  "org.objectweb.joram.client.jms.Queue";
      destclass =  System.getProperty("Destination", destclass);

      Thread.sleep(500L);
      cf = createConnectionFactory(baseclass);
      AdminModule.connect(cf);

      User.create("anonymous", "anonymous", 0);
      dest = createDestination(destclass);
      dest.setFreeReading();
      dest.setFreeWriting();

      AdminModule.disconnect();

      connect();
      
      Connection cnx2 = cf.createConnection();
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sess2.createProducer(dest);
      cnx2.start();

      Sender17 sender = new Sender17(cnx2, sess2, producer);
      new Thread(sender).start();

      
      int nbClose = 0;

      while (nbMsg < 5000) {
        
        while (nbMsg < ((nbClose +1) *250))
          Thread.sleep(500L);

        nbClose += 1;
        close();
        Thread.sleep(500L);

        connect();
      }
      
      Thread.sleep(1000);
      
      assertEquals("nbExc=" + nbExc, nbClose, nbExc);
      assertEquals("nbErr=" + nbErr, 0, nbErr);
      assertEquals("nbMsg=" + nbMsg, 5000, nbMsg);
      
//      System.out.println("Test OK: " + nbExc + ", " + nbErr + ", " + nbMsg);

      close();
    } catch (Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}

class Sender17 implements Runnable {
  Connection cnx;
  Session session;
  MessageProducer producer;
  boolean ended;
  Object lock;

  public Sender17(Connection cnx,
                  Session session,
                  MessageProducer producer) throws Exception {
    this.cnx = cnx;
    this.session = session;
    this.producer = producer;

    ended = false;
    lock = new Object();
  }

  public void waitEnd() {
    synchronized (lock) {
      while (! ended) {
        try {
          lock.wait();
        } catch (InterruptedException exc) {
        }
      }
    }
  }

  public void run() {
    try {
      for (int i=0; i<5000; i++) {
        Message msg = session.createMessage();
        msg.setIntProperty("index", i);
        producer.send(msg);
      }
      System.out.println("end");
    } catch (Exception exc) {
      exc.printStackTrace();
    } finally {
      try {
        cnx.close();
      } catch (Exception exc) {
        exc.printStackTrace();
      }
      synchronized (lock) {
        ended = true;
        lock.notify();
      }
    }
  }
}
