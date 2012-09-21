/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2012 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.bridgejms;

import java.net.ConnectException;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSDistribution;

import framework.TestCase;

/**
 * Test: Creates and configures a distribution queue as the foreign server is not already running.
 *  - Creates and configures the distribution queue.
 *  - Starts the foreign server then creates the foreign destination.
 *  - Sends 10 messages to the bridge queue.
 *  - Receives the messages on foreign queue.
 */
public class BridgeTest11x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest11x().run();
  }

  public void run() {
    try {
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("server0 started");
      Thread.sleep(1000);

      adminBridge();
      System.out.println("adminBridge ok");
      Thread.sleep(5000);
      
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("server1 started");
      Thread.sleep(1000);
      
      adminForeign();
      System.out.println("adminForeign ok");
      Thread.sleep(1000);

      // Sends 10 messages to the bridge
      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer joramProd = joramSess.createProducer(joramOutQueue);
      joramCnx.start();
      
      try {
        TextMessage msgOut = joramSess.createTextMessage();
        for (int i=0; i < 10; i++) {
          msgOut.setText("Message number " + i);
          joramProd.send(msgOut);
          System.out.println("send msg = " + msgOut.getText());
        }
      } catch (Exception exc) {
        System.out.println("Error during sending messages.");
      }
      joramCnx.close();
      
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
      Destination foreignQueue = (Destination) jndiCtx.lookup("foreignQueue");      
      jndiCtx.close();

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer foreignCons = foreignSess.createConsumer(foreignQueue);
      foreignCnx.start();

      TextMessage msgIn = null;
      for (int i = 0; i < 10; i++) {
        msgIn = (TextMessage) foreignCons.receive(1000);
        assertTrue("Not received message #" + i, (msgIn != null));
        if (msgIn == null)
          System.out.println("Not received message #" + i);
        else
          System.out.println("receive msg = " + msgIn.getText());
      }
      foreignCnx.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      killAgentServer((short)1);
      endTest(); 
    }
  }
  
  javax.jms.ConnectionFactory joramCF = null;
  Queue joramOutQueue = null;
  
  public void adminBridge() throws ConnectException, AdminException {
    // Be careful, do not use JNDI as server #1 is not running
    boolean async = Boolean.getBoolean("async");
    System.out.println("async=" + async);

    joramCF = TcpConnectionFactory.create("localhost", 16010);
    AdminModule.connect(joramCF, "root", "root");

    User.create("anonymous", "anonymous", 0);

    // Setting the bridge properties
    Properties prop = new Properties();
    prop.setProperty("jms.DestinationName", "foreignQueue");
    prop.setProperty("jms.ConnectionUpdatePeriod", "1000");
    prop.setProperty("period", "1000");
    prop.setProperty("distribution.async", "" + async);
    prop.setProperty("distribution.className", JMSDistribution.class.getName());

    joramOutQueue = Queue.create(0, "BridgeOutQueue", Queue.DISTRIBUTION_QUEUE, prop);
    joramOutQueue.setFreeWriting();
    System.out.println("joramOutQueue = " + joramOutQueue);

    AdminModule.disconnect();
    System.out.println("adminBridge end.");
  }
  
  public void adminForeign() throws ConnectException, AdminException, NamingException {
    javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);
    AdminModule.connect(foreignCF, "root", "root");

    User.create("anonymous", "anonymous", 1);

    // create The foreign destination and connectionFactory
    Queue foreignQueue = Queue.create(1, "foreignQueue");
    foreignQueue.setFreeReading();
    foreignQueue.setFreeWriting();
    System.out.println("foreign queue = " + foreignQueue);
    
    // bind foreign destination and connectionFactory
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.rebind("foreignQueue", foreignQueue);
    jndiCtx.rebind("foreignCF", foreignCF);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("adminForeign end.");
  }
}

