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
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.AMQPAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Producer on AMQP server, Consumer on Joram through AMQP acquisition queue.
 *  - Sends 10.000 messages on AMQP queue.
 *  - Receives 10.000 messages on Joram AMQP acquisition queue.
 */
public class Test4 extends TestCase {
  public static boolean debug = false;
  public static int nbloop = 10;
  
  public static void main(String[] args) {
    new Test4().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);
      
      debug = Boolean.getBoolean("DEBUG");
      nbloop = Integer.getInteger("nbloop", nbloop);
      
      // Administration code
      {
        boolean async = Boolean.getBoolean("async");
        System.out.println("async=" + async);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);
        ((TcpConnectionFactory) joramCF).getParameters().connectingTimer = 60;

        AdminModule.connect(joramCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);

        // Setting the bridge properties
        Properties prop1 = new Properties();
        prop1.setProperty("amqp.Queue.DeclarePassive", "false");
        prop1.setProperty("amqp.Queue.DeclareExclusive", "false");
        prop1.setProperty("amqp.Queue.DeclareDurable", "true");
        prop1.setProperty("amqp.Queue.DeclareAutoDelete", "false");
        prop1.setProperty("amqp.ConnectionUpdatePeriod", "1000");

        Queue joramInQueue = AMQPAcquisitionQueue.create(0, "queue", "amqpQueue", prop1);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramCF", joramCF);

        jndiCtx.close();

        AdminModule.disconnect();
      }
      System.out.println("admin config ok");
      Thread.sleep(1000);
      
      AMQPSender sender = new AMQPSender("amqpQueue", true, 1000, 0);
      new Thread(sender).start();
      
      // Gets administered objects from JNDI
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();
      
      // Creates needed Connection and Session objects
      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
      joramCnx.start();
      
      TextMessage msgIn;
      for (int i=0; i<1000; i++) { 
        msgIn = (TextMessage) joramCons.receive(5000);
        if (msgIn != null) {
          if (debug) System.out.println("receive msg = " + msgIn.getText());
          assertEquals("Message number " + i, msgIn.getText());
        }
      }      
      joramCnx.close();
  
      for (int i=0; i<nbloop; i++) {
        test(1000, 0);
        if (failureCount()+errorCount() != 0) break;
      }
      
//      test(1000, 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      endTest(); 
    }
  }
  

  final static long timeout = 1L;
  final static Object lock = new Object();
  
  void test(final int msgs, int first) throws Exception {
    // Gets administered objects from JNDI
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
    jndiCtx.close();
    
    AMQPSender sender = new AMQPSender("amqpQueue", true, msgs, timeout);
    new Thread(sender).start();
    
    Thread.sleep((3*timeout*msgs)/5);
    killAgentServer((short) 0);
    System.out.println("Joram server stopped.");
    Thread.sleep(250);
    startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
    System.out.println("Joram server started.");
    
    // wait all the messages are acquired by the bridge queue.
    Thread.sleep(1000);
    
    // Creates needed Connection and Session objects
    Connection joramCnx = joramCF.createConnection();
    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
    joramCnx.start(); 

    int i = 0;
    TextMessage msgIn;
    do {
      msgIn = (TextMessage) joramCons.receive(5000);
      if (msgIn != null) {
        if (debug) System.out.println("receive msg#" + (first+i) + " = " + msgIn.getText());
        assertEquals("Message number " + (first+i), msgIn.getText());
        i += 1;
      }
    } while (msgIn != null);
    assertEquals(i, msgs);
    
    joramCnx.close();
  }
}

