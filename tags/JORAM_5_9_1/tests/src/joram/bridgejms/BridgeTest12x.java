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

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSDistributionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSDistribution;

import framework.TestCase;

/**
 * Test: Tests various behavior about the distribution queue and JMSConnectionService.
 *  - Declares a JMSConnection without name (see a3servers4.xml).
 *  - Sends a message to the bridge and verify that it is received by the foreign.
 *  - sends a message to the bridge routing by an inexistent connection.
 *  - Verify that it is not received by the foreign.
 *  - Adds the wanted connection and verify that the message is received by the foreign.
 */
public class BridgeTest12x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest12x().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
        boolean async = Boolean.getBoolean("async");
        System.out.println("async=" + async);

        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

        AdminModule.connect(joramCF, "root", "root");

        User.create("anonymous", "anonymous", 0);
        User.create("anonymous", "anonymous", 1);

        // create The foreign destination and connectionFactory
        Queue foreignQueue = Queue.create(1, "foreignQueue");
        foreignQueue.setFreeReading();
        foreignQueue.setFreeWriting();

        javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

        // bind foreign destination and connectionFactory
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();
        jndiCtx.rebind("foreignQueue", foreignQueue);
        jndiCtx.rebind("foreignCF", foreignCF);
        jndiCtx.rebind("addedForeignCF", foreignCF);
        jndiCtx.close();
      
        // Setting the bridge properties
        Properties prop = new Properties();
        prop.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop.setProperty("period", "1000");
        prop.put("distribution.async", "" + async);
        Queue joramOutQueue = JMSDistributionQueue.create(0, "BridgeOutQueue", "foreignQueue", prop);
        joramOutQueue.setFreeWriting();
        
        System.out.println("admin done.");
        Thread.sleep(1000);
        
        Connection joramCnx = joramCF.createConnection();
        Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer joramProd = joramSess.createProducer(joramOutQueue);
        joramCnx.start();
        
        TextMessage msgOut = joramSess.createTextMessage("Coucou");
        joramProd.send(msgOut);
        System.out.println("send a message = " + msgOut.getText());

        Connection foreignCnx = foreignCF.createConnection();
        Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer foreignCons = foreignSess.createConsumer(foreignQueue);
        foreignCnx.start();

        TextMessage msgIn = (TextMessage) foreignCons.receive(5000);
        assertTrue("Should receive a message: " + msgIn, (msgIn != null));
        if (msgIn != null)
          System.out.println("Receive message: " + msgIn.getText());

        msgOut = joramSess.createTextMessage("Coucou from addedForeignCF");
        msgOut.setStringProperty("jms.Routing", "addedForeignCF");
        joramProd.send(msgOut);
        System.out.println("send a message = " + msgOut.getText());
        
//        Be Careful, if a message cannot be routed the next one is not delivered. 
//        msgOut = joramSess.createTextMessage("Coucou");
//        joramProd.send(msgOut);
//        System.out.println("send a message = " + msgOut.getText());

        msgIn = (TextMessage) foreignCons.receive(5000);
        assertTrue("Should not receive message: " + msgIn, (msgIn == null));
        if (msgIn != null)
          System.out.println("Should not receive message: " + msgIn);

        System.out.println("Adds the cnx1 connection");
        AdminModule.addJMSBridgeConnection(0, "scn://localhost:16400/?name=cnx1&cf=addedForeignCF&jndiFactoryClass=fr.dyade.aaa.jndi2.client.NamingContextFactory");
        Thread.sleep(1000);
        
        msgIn = (TextMessage) foreignCons.receive(5000);
        assertTrue("Should receive a message: " + msgIn, (msgIn != null));
        if (msgIn != null)
          System.out.println("Receive message: " + msgIn.getText());
     
        AdminModule.disconnect();
      }catch(Exception exc){
      }
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

