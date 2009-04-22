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
public class Test16 extends BaseTest implements ExceptionListener {
  static Test16 test16 = null;
  
  boolean debug = false;

  ConnectionFactory cf;
  Destination dest;
  Connection cnx1;
  Session sess1;
  MessageConsumer cons;

  int nbExc = 0;
  int nbMsg = 0;
  int nbErr = 0;
  int idx = 0;
  int errors = 0;

  boolean ended = false;

  public void onException(JMSException exc) {
    if (debug) System.out.println("onException");
    nbExc += 1;
    assertEquals("javax.jms.IllegalStateException", exc.getClass().getName());
    if (! ended) connect();
  }

  public synchronized void connect() {
    try {
      if (cnx1 != null) return;
      
      if (debug) System.out.println("connect");
      cnx1 = cf.createConnection();
      cnx1.setExceptionListener(this);
      sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      if (dest instanceof org.objectweb.joram.client.jms.Queue)
        cons = sess1.createConsumer(dest);
      else
        cons = sess1.createDurableSubscriber((Topic) dest, "sub");
      cnx1.start();
    } catch (JMSException exc2) {
      exc2.printStackTrace();
      error(exc2);
      AgentServer.stop();
      endTest();
    }
  }

  public synchronized void close(boolean ended) {
    this.ended = ended;

    try {
      if (cnx1 == null) {
        try {
          if (debug) System.out.println("wait");
          wait(1000);
        } catch (InterruptedException exc) {}
        connect();
      } else {
        if (debug) System.out.println("close");
        if (cnx1 != null)
          cnx1.close();
        cnx1 = null;
        cons = null;
      }
    } catch (JMSException exc2) {
      exc2.printStackTrace();
      error(exc2);
      AgentServer.stop();
      endTest();
    }
  }

  public synchronized MessageConsumer getConsumer() {
    try {
      while (cons == null)
        wait(10);
    } catch (InterruptedException exc) {}

    return cons;
  }

  public static void main (String args[]) throws Exception {
    test16 = new Test16();
    test16.run();
  }

  public void run(){
    try{
      timeout = 30000L;

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

      Sender16 sender = new Sender16(cnx2, sess2, producer);
      new Thread(sender).start();

      Message msg = null;
      while (nbMsg < 500) {
        try {
          msg = getConsumer().receive();

          if (msg == null) {
            if (debug) System.out.println("msg==null");
            // this message consumer is concurrently closed (see JavaDoc)
            // wait for the connection opening.
            //            Thread.sleep(1000L);
            continue;
          }
          nbMsg += 1;
          int index = msg.getIntProperty("index");
          if (index != idx) {
            System.err.println("recv#" + idx + '/' + index);
            nbErr += 1;
          }
          idx = index +1;
        } catch (JMSException exc) {
          errors += 1;
          if (errors > 2) break;
          System.err.println("end recv#" + idx + ": " + exc.getMessage());
          Thread.sleep(10L);
        }
      }

      Thread.sleep(1000);

      assertEquals("nbExc=" + nbExc, 10, nbExc);
      assertEquals("nbErr=" + nbErr, 0, nbErr);
      assertEquals("nbMsg=" + nbMsg, 500, nbMsg);
      assertEquals("errors=" + errors, 0, errors);

      if (debug) System.out.println("Test OK: " + nbExc + ", " + nbErr + ", " + nbMsg);

      close(true);
    }catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    }finally{
      AgentServer.stop();
      endTest();
    }
  }
}

class Sender16 implements Runnable {
  Connection cnx;
  Session session;
  MessageProducer producer;

  public Sender16(Connection cnx,
                  Session session,
                  MessageProducer producer) throws Exception {
    this.cnx = cnx;
    this.session = session;
    this.producer = producer;
  }

  public void run() {
    try {
      for (int i=0; i<500; i++) {
        Message msg = session.createMessage();
        msg.setIntProperty("index", i);
        producer.send(msg);
        
        if ((i%50) == 49) {
          // Trying to close connection during receive
          Test16.test16.close(false);
        }
        
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    } finally {
      try {
        cnx.close();
      } catch (Exception exc) {
        exc.printStackTrace();
      }
    }
  }
}
