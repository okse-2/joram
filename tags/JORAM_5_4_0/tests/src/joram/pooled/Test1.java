/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package joram.pooled;

import java.util.Random;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.pool.PooledConnectionFactory;

import framework.TestCase;

/**
 * Test a PooledConnectionFactory with a TcpConnectionFactory
 */
public class Test1 extends TestCase {
  static ConnectionFactory cf, pcf;
  static Queue queue;
  static Object lock = new Object();
  static int nbt;

  public static void main(String[] args) {
    new Test1().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);

      // Creates the ConnectionFactory 
      cf = TcpConnectionFactory.create("localhost", 16010);
      pcf = new PooledConnectionFactory(cf);

      // Creates the administered objects
      AdminModule.connect(cf);

      // create a Queue and a different users  
      queue = Queue.create(0);
      User.create("anonymous", "anonymous");

      // set permissions
      queue.setFreeReading();
      queue.setFreeWriting();

      Context jndiCtx = new InitialContext();
      jndiCtx.bind("cf", cf);
      jndiCtx.bind("queue", queue);
      jndiCtx.close();

      AdminModule.disconnect();

      for (int i=0; i<15; i++) {
        new Thread() {
          public void run() {
            try {
              doTest(pcf, "anonymous", "anonymous", 50);
            } catch (JMSException exc) {
              exc.printStackTrace();
            }
          }
        }.start();
      }
      
      Thread.sleep(1000L);
      
      synchronized(lock) {
        while (nbt != 0) {
          try {
            lock.wait();
          } catch (InterruptedException exc) {}
        }
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  public void doTest(ConnectionFactory cf,
                     String user, String pass,
                     int nbloop) throws JMSException {
    Random rand = new Random();
    synchronized(lock) {
      nbt += 1;
    }
    
    for (int i=0; i<nbloop; i++) {
      System.out.println("loop#" + i);
      Connection cnx = null;
      if (user != null)
        cnx = cf.createConnection(user, pass);
      else
        cnx = cf.createConnection();
      
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(queue);
      producer.setTimeToLive(1000);
      
      for (int j=0; j<10; j++) {
        Message msg = session.createMessage();
        producer.send(msg);
        try {
          Thread.sleep(10);
        } catch (InterruptedException exc) {}
      }
      cnx.close();
      
      try {
        Thread.sleep(rand.nextInt(1000));
      } catch (InterruptedException exc) {}
    }
    
    synchronized(lock) {
      nbt -= 1;
      if (nbt == 0) lock.notify();
    }
  }
}
