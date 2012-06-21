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
package joram.bridgeamqp;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.amqp.AmqpAcquisition;
import org.objectweb.joram.mom.dest.amqp.AmqpDistribution;

import framework.TestCase;

/**
 * Test: Use a bridge queue to send messages to a foreign AMQP queue.
 *       Test the bridge behavior with random kill and start of the Joram bridge server.
 *  - Sends messages.
 *  - Random kills then restarts the bridge server.
 *  - Receives the sent messages.
 */
public class Test3 extends TestCase {

  public static void main(String[] args) {
    new Test3().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);
      
      // Administration code
      {
        boolean async = Boolean.getBoolean("async");
        System.out.println("async=" + async);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

        AdminModule.connect(joramCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);

        // Setting the bridge properties
        Properties prop1 = new Properties();
        prop1.setProperty("acquisition.className", AmqpAcquisition.class.getName());
        prop1.setProperty("amqp.QueueName", "amqpQueue");
        prop1.setProperty("amqp.Queue.DeclarePassive", "false");
        prop1.setProperty("amqp.Queue.DeclareExclusive", "false");
        prop1.setProperty("amqp.Queue.DeclareDurable", "true");
        prop1.setProperty("amqp.Queue.DeclareAutoDelete", "false");
        prop1.setProperty("amqp.ConnectionUpdatePeriod", "1000");
        
        Queue joramInQueue = Queue.create(0, "queue", Queue.ACQUISITION_QUEUE, prop1);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        // Setting the bridge properties
        Properties prop2 = new Properties();
        prop2.setProperty("distribution.className", AmqpDistribution.class.getName());
        prop2.setProperty("amqp.QueueName", "amqpQueue");
        prop2.setProperty("amqp.Queue.DeclarePassive", "false");
        prop2.setProperty("amqp.Queue.DeclareExclusive", "false");
        prop2.setProperty("amqp.Queue.DeclareDurable", "true");
        prop2.setProperty("amqp.Queue.DeclareAutoDelete", "false");
        prop2.setProperty("amqp.ConnectionUpdatePeriod", "1000");
        
        prop2.setProperty("period", "1000");      
        prop2.put("distribution.async", "" + async);

        Queue joramOutQueue = Queue.create(0, "BridgeOutQueue", Queue.DISTRIBUTION_QUEUE, prop2);
        joramOutQueue.setFreeWriting();
        System.out.println("joramOutQueue = " + joramOutQueue);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramOutQueue", joramOutQueue);
        jndiCtx.rebind("joramCF", joramCF);

        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }
      System.out.println("admin config ok");
      Thread.sleep(1000);
      
      for (int i=0; i<10; i++)
        test(100);
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      endTest(); 
    }
  }
  

  final static long timeout = 5L;
  final static Object lock = new Object();
  
  void test(final int msgs) throws Exception {
    // Gets administered objects from JNDI
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
    Destination joramOutDest = (Destination) jndiCtx.lookup("joramOutQueue");
    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
    jndiCtx.close();
    
    // Creates needed Connection and Session objects
    Connection joramCnx = joramCF.createConnection();
    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer joramProd = joramSess.createProducer(joramOutDest);
    joramCnx.start(); 

    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep((2*timeout*msgs)/5);
          synchronized(lock) {
            crashAgentServer((short) 0);
          }
          System.out.println("Joram server stopped.");
          startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
          System.out.println("Joram server started.");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    
    int i = 0;
    try {
      // Send messages up to the server stop.
      TextMessage msgOut = joramSess.createTextMessage();
      for (; i < msgs; ) {
        msgOut.setText("Message number " + i);
        synchronized(lock) {
          joramProd.send(msgOut); i++;
        }
        System.out.println("send msg = " + msgOut.getText());
        Thread.sleep(timeout);
      }
    } catch (JMSException exc) {
      exc.printStackTrace();
    }
    Thread.sleep(1000);

    // Creates needed Connection and Session objects
    joramCnx = joramCF.createConnection();
    joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
    joramCnx.start(); 

    int nbmsg = 0;
    TextMessage msgIn;
    do { 
      msgIn = (TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + nbmsg, msgIn.getText());
        nbmsg += 1;
      }
    } while (msgIn != null);
    assertEquals(i, nbmsg);
    
    joramCnx.close();
  }

}

