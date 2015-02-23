/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2015 ScalAgent Distributed Technologies
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

import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Test a bridge connection with error due to an already used ClientID.
 */
public class BridgeTest17x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest17x().run();
  }

  public void run() {
    try {
      System.out.println("server#1 central start");
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      javax.jms.ConnectionFactory centralCF = null;
      try{
        centralCF = TcpConnectionFactory.create("localhost", 16011);

        AdminModule.connect(centralCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 1);
        jndiCtx.rebind("centralCF", centralCF);

        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin#1 closed.");
      }catch(Exception exc){
        exc.printStackTrace();
      }

      // Creates a connection in order to forbid the bridge connection
      Connection centralCnx = centralCF.createConnection();
      centralCnx.setClientID("CLIENT1");
      centralCnx.start();

      System.out.println("server#0 bridge start");     
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
        javax.jms.ConnectionFactory bridgeCF = TcpConnectionFactory.create("localhost", 16010);

        AdminModule.connect(bridgeCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);

        // create The foreign destination and connectionFactory
        Queue foreignQueue = Queue.create(1, "foreignQueue");
        foreignQueue.setFreeReading();
        foreignQueue.setFreeWriting();
        System.out.println("foreign queue = " + foreignQueue);

        javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);
        ((org.objectweb.joram.client.jms.ConnectionFactory) foreignCF).getParameters().connectingTimer = 5;
        ((org.objectweb.joram.client.jms.ConnectionFactory) foreignCF).getParameters().cnxPendingTimer = 10000;

        // bind foreign destination and connectionFactory
        jndiCtx.rebind("foreignQueue", foreignQueue);
        jndiCtx.rebind("foreignCF", foreignCF);
      
        // Setting the bridge properties
        Properties prop = new Properties();
        prop.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop.setProperty("period", "1000");
        // Creating a Queue bridge on server 0:
        Queue joramInQueue = JMSAcquisitionQueue.create(0, "joramInQueue", "foreignQueue", prop);
        joramInQueue.setFreeReading();
        joramInQueue.setFreeWriting();
        System.out.println("joramInQueue = " + joramInQueue);

        jndiCtx.rebind("joramInQueue", joramInQueue);
        jndiCtx.rebind("joramCF", bridgeCF);
        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin#0 closed.");
      }catch(Exception exc){
        exc.printStackTrace();
      }
      Thread.sleep(30000);
      
      // L'objectif du test est de vérifier que les connexions ne s'empilent pas dans le
      // serveur (queue d'acquisition JMX, appel d'une methode statique au travers de l'admin
      // ou ajout d'une méthode d'admin permettant d'interogger un paramètre JMX)

      centralCnx.close();
      
      // Verify that now the configuration works
      
      System.out.println("test start..");
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination joramInDest = (Destination) jndiCtx.lookup("joramInQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
      Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");      
      jndiCtx.close();

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
      joramCnx.start(); 

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer foreignProd = foreignSess.createProducer(foreignDest);
      foreignCnx.start();

      BytesMessage msgBytesOut = foreignSess.createBytesMessage();
      msgBytesOut.writeBoolean(true);
      msgBytesOut.writeLong(1000L);
      foreignProd.send(msgBytesOut);

      System.out.println("sends message");

      BytesMessage msgBytesIn = (BytesMessage) joramCons.receive(5000L);
      assertTrue(msgBytesIn != null);
      if (msgBytesIn != null) {
        assertTrue(msgBytesIn.readBoolean());
        assertTrue(msgBytesIn.readLong() == 1000L);
      }

      System.out.println("message received");
      
      StreamMessage msgStreamOut = foreignSess.createStreamMessage();
      msgStreamOut.writeBoolean(true);
      msgStreamOut.writeLong(1000L);
      foreignProd.send(msgStreamOut);

      System.out.println("sends message");

      StreamMessage msgStreamIn = (StreamMessage) joramCons.receive(5000L);
      assertTrue(msgStreamIn != null);
      if (msgStreamIn != null) {
        assertTrue(msgStreamIn.readBoolean());
        assertTrue(msgStreamIn.readLong() == 1000L);
      }

      System.out.println("message received");
      
      Thread.sleep(1000);

      joramCnx.close();
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
}

