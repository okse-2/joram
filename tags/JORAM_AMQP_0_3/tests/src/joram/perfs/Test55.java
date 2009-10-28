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

package joram.perfs;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Configuration;
import framework.TestCase;

/**
 *
 */
public class Test55 extends TestCase {
  static int NbMsg = 10000;
  static int MsgSize = 100;
  static int min = 2500;
  static int max = 3000;
  
  // Processus de Traitement
  static int NbX1 = 4;
  static Destination dest1 = null;
  static X1[] x1= null;
  
  // Processus de Constitution des lots
  static Destination dest2 = null;
  
  // Processus de classement / conversion
  static Destination dest3 = null;
  
  // Processus de "protocoles de sortie"
  static Destination dest4 = null;
  
  // Processus de supervision
  static Destination dest5 = null;
  static Destination dest6 = null;
  
  static ConnectionFactory cf = null;

  static Sync sync;
  
  public void run(){
    try{
      writeIntoFile("==================== start test ================");
      AgentServer.init((short) 0, "./s0", null);
      AgentServer.start();

      Thread.sleep(1000L);

      NbMsg = Integer.getInteger("NbMsg", NbMsg).intValue();
      MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

      min = Integer.getInteger("min", min).intValue();
      max = Integer.getInteger("max", max).intValue();

      NbX1 = Integer.getInteger("NbX1", NbX1).intValue();

      AdminModule.collocatedConnect("root", "root");

      dest1 = Queue.create("dest1");
      dest1.setFreeReading();
      dest1.setFreeWriting();

      dest2 = Queue.create("dest2");
      dest2.setFreeReading();
      dest2.setFreeWriting();

      dest3 = Queue.create("dest3");
      dest3.setFreeReading();
      dest3.setFreeWriting();

      dest4 = Queue.create("dest4");
      dest4.setFreeReading();
      dest4.setFreeWriting();

      dest5 = Queue.create("dest5");
      dest5.setFreeReading();
      dest5.setFreeWriting();

      dest6 = Topic.create("dest6");
      dest6.setFreeReading();
      dest6.setFreeWriting();

      User user = User.create("anonymous", "anonymous", 0);

      AdminModule.disconnect();

      writeIntoFile("----------------------------------------------------");
      writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
      writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
      writeIntoFile("NbX1=" + NbX1 + ", min=" + min + ", max=" + max);
      writeIntoFile("NbMsg=" + NbMsg + ", MsgSize=" + MsgSize);
      writeIntoFile("----------------------------------------------------");

      ConnectionFactory cf1 =  TcpConnectionFactory.create("localhost", 16010);
//      ConnectionFactory cf1 =  LocalConnectionFactory.create();
      
      sync = new Sync(min, max);
      
      Connection cnx = cf1.createConnection();
      X6 x6 = new X6(cnx, dest6);
      cnx.start();
      
      cnx = cf1.createConnection();
      X5 x5 = new X5(cnx, dest5);
      cnx.start();

      cnx = cf1.createConnection();
      X2 x2 = new X2(cnx, dest2, dest3, dest6);
      cnx.start();
      
      x1 = new X1[NbX1];
      for (int i=0; i<NbX1; i++) {
        cnx = cf1.createConnection();
        x1[i] = new X1(cnx, dest1, dest2);
        cnx.start();
      }
      
      cnx = cf1.createConnection();
      Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod1 = sess.createProducer(dest1);
      MessageProducer prod2 = sess.createProducer(dest5);

      byte[] content1 = new byte[MsgSize];
      byte[] content2 = new byte[MsgSize/10];
      long start = System.currentTimeMillis();
      for (int i=0; i<NbMsg; i++) {
        BytesMessage msg1 = sess.createBytesMessage();
        msg1.writeBytes(content1);
        msg1.setLongProperty("time", System.currentTimeMillis());
        prod1.send(msg1);
        
        BytesMessage msg2 = sess.createBytesMessage();
        msg2.writeBytes(content2);
        msg2.setLongProperty("time", System.currentTimeMillis());
        prod2.send(msg2);
        
//        if (i%10 == 9) {
//          long dt = (i *500) - (System.currentTimeMillis() -start);
//          if (dt > 0) Thread.sleep(dt);
//        }
        
        sync.test();
      }
      sync.end();
      long dt = (System.currentTimeMillis() -start) /1000;
      
      Thread.sleep(1000L);
      
      writeIntoFile("----------------------------------------------------");
      writeIntoFile("Process " + ((MsgSize * NbMsg)/ (1024*1024)) + "Mb in " + dt + "seconds (" + (NbMsg / dt) + "msg/s");

    } catch(Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }

  public static void main(String[] args) {
    new Test55().run();
  }
}

class Sync {
  int max1, max2, nb;
  boolean locked;
  
  Sync(int max1, int max2) {
    this.max1 = max1;
    this.max2 = max2;
  }
  
  synchronized void end() {
    while (nb > 0) {
      try {
        wait(100);
      } catch (InterruptedException exc) {}
    }
  }
  
  synchronized void test() {
    while (nb > max2) {
//      System.out.println("locked");
      locked = true;
      try {
        wait();
//        System.out.println("freed");
      } catch (InterruptedException exc) {}
    }
    locked = false;
  }
  
  synchronized void sent() {
    nb += 1;
  }
  
  synchronized void received() {
    nb -= 1;
    if (locked && (nb <= max1)) notifyAll();
  }
}

class X1 implements MessageListener {
  Connection cnx;
  Destination in;
  Destination out;
  Session sess;
  MessageConsumer cons;
  MessageProducer prod;

  byte[] content = null;

  X1(Connection cnx, Destination in, Destination out) throws Exception {
    this.cnx = cnx;
    this.in = in;
    this.out = out;

    sess = cnx.createSession(true, 0);
    cons = sess.createConsumer(in);
    prod = sess.createProducer(out);

    cons.setMessageListener(this);
  }

  int counter = 0;
  long travel = 0L;

  long start = 0L;
  long last = 0L;

  public synchronized void onMessage(Message m) {
    try {
      last = System.currentTimeMillis();
      if (counter == 0) start = last;
      travel += (last - m.getLongProperty("time"));
      counter += 1;
      
      byte[] content = new byte[1500];
      while (((BytesMessage) m).readBytes(content, 1500) > 0) {
        BytesMessage msg = sess.createBytesMessage();
        msg.writeBytes(content);
        msg.setLongProperty("time", System.currentTimeMillis());
        prod.send(msg);
        Test55.sync.sent();
      }
      sess.commit();

      if ((counter%100)==0)
        System.out.println("| X1 #" + counter + " dt(us)=" +  (((last - start) *1000L)/(counter)) +
                           ", travel time(ms)=" + (travel/counter) + ", " + ((1000L * counter) / (last - start)) + "msg/s");
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

class X2 implements MessageListener {
  Connection cnx;
  Session sess;
  
  Destination in;
  MessageConsumer cons;
  
  Destination out;
  MessageProducer prod;

  Destination req1;
  Requestor requestor;
  
  byte[] content = null;

  X2(Connection cnx, Destination in, Destination out, Destination req1) throws Exception {
    this.cnx = cnx;
    this.in = in;
    this.out = out;
    this.req1 = req1;

    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    ((org.objectweb.joram.client.jms.Session) sess).setQueueMessageReadMax(500);
    cons = sess.createConsumer(in);
    prod = sess.createProducer(out);

    cons.setMessageListener(this);
    requestor = new Requestor(sess, req1);
  }

  int counter = 0;
  long travel = 0L;

  long start = 0L;
  long last = 0L;

  public synchronized void onMessage(Message m) {
    try {
      last = System.currentTimeMillis();
      if (counter == 0) start = last;
      counter += 1;
      travel += (last - m.getLongProperty("time"));

      if (counter%20 == 0) {
        requestor.request();
      }
      
      if ((counter%10000)==0)
        System.out.println("| X2 #" + counter + " dt(us)=" +  (((last - start) *1000L)/(counter)) +
                           ", travel time(ms)=" + (travel/counter) + ", " + ((1000L * counter) / (last - start)) + "msg/s");

      Test55.sync.received();
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

class Requestor {
  Session sess;
  
  Destination rep;
  MessageConsumer cons;
  
  Destination req;
  MessageProducer prod;

  int nbr = 0;
  long dt = 0L;
  
  Requestor(Session sess, Destination req) throws Exception {
    this.sess = sess;
    
    this.req = req;
    prod = sess.createProducer(req);
    
    this.rep = (Destination) sess.createTemporaryTopic();
    cons = sess.createConsumer(rep);
  }
  
  synchronized void request() throws JMSException {
    nbr += 1;
    Message req = sess.createMessage();
    req.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
    req.setJMSReplyTo(rep);
    long start = System.currentTimeMillis();
    Message rep = null;
    do {
      prod.send(req);
      rep = cons.receive(1000L);
    } while (rep == null);
    dt += System.currentTimeMillis() - start;
    
    if (nbr %1000 == 999)
      System.out.println("Requestor #" + nbr + " -> " + ((1000 *nbr) / dt));
  }
}

class X5 implements MessageListener {
  Connection cnx;
  Destination in;
  Session sess;
  MessageConsumer cons;

  byte[] content = null;

  X5(Connection cnx, Destination in) throws Exception {
    this.cnx = cnx;
    this.in = in;

    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cons = sess.createConsumer(in);

    cons.setMessageListener(this);
  }

  int counter = 0;
  long travel = 0L;

  long start = 0L;
  long last = 0L;

  public synchronized void onMessage(Message m) {
    try {
      last = System.currentTimeMillis();
      if (counter == 0) start = last;
      counter += 1;
      travel += (last - m.getLongProperty("time"));

      if ((counter%100)==0)
        System.out.println("| X5 #" + counter + " dt(us)=" +  (((last - start) *1000L)/(counter)) +
                           ", travel time(ms)=" + (travel/counter) + ", " + ((1000L * counter) / (last - start)) + "msg/s");
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

class X6 implements MessageListener {
  Connection cnx;
  Destination in;
  Session sess;
  MessageConsumer cons;
  MessageProducer prod;

  byte[] content = null;

  X6(Connection cnx, Destination in) throws Exception {
    this.cnx = cnx;
    this.in = in;

    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cons = sess.createConsumer(in);
    prod = sess.createProducer(null);
    
    cons.setMessageListener(this);
  }

  public synchronized void onMessage(Message request) {
    try {
      Message reply = sess.createMessage();
      reply.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
      reply.setJMSCorrelationID(request.getJMSMessageID());
      prod.send(request.getJMSReplyTo(), reply);
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

