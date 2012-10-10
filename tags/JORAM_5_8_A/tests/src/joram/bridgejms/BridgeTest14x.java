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

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.JMSException;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSAcquisitionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Test the bridge with Bytes, Stream or Map messages (JORAM-80).
 */
public class BridgeTest14x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest14x().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

        AdminModule.connect(joramCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);
        User.create("anonymous", "anonymous", 1);

        // create The foreign destination and connectionFactory
        Queue foreignQueue = Queue.create(1, "foreignQueue");
        foreignQueue.setFreeReading();
        foreignQueue.setFreeWriting();
        System.out.println("foreign queue = " + foreignQueue);

        javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

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
        jndiCtx.rebind("joramCF", joramCF);
        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }catch(Exception exc){
        exc.printStackTrace();
      }
      Thread.sleep(1000);

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

      BytesMessage msgBytesIn = (BytesMessage) joramCons.receive(5000L);
      assertTrue(msgBytesIn != null);
      if (msgBytesIn != null) {
        assertTrue(msgBytesIn.readBoolean());
        assertTrue(msgBytesIn.readLong() == 1000L);
      }

      StreamMessage msgStreamOut = foreignSess.createStreamMessage();
      msgStreamOut.writeBoolean(true);
      msgStreamOut.writeLong(1000L);
      foreignProd.send(msgStreamOut);

      StreamMessage msgStreamIn = (StreamMessage) joramCons.receive(5000L);
      assertTrue(msgStreamIn != null);
      if (msgStreamIn != null) {
        assertTrue(msgStreamIn.readBoolean());
        assertTrue(msgStreamIn.readLong() == 1000L);
      }

      MapMessage msgMapOut = foreignSess.createMapMessage();
      msgMapOut.setBoolean("myBoolean", true);
      msgMapOut.setLong("myLong", 1000L);
      foreignProd.send(msgMapOut);
      foreignCnx.close();

      MapMessage msgMapIn = (MapMessage) joramCons.receive(5000L);
      assertTrue(msgMapIn != null);
      if (msgMapIn != null) {
        assertTrue(msgMapIn.getBoolean("myBoolean"));
        assertTrue(msgMapIn.getLong("myLong") == 1000L);
      }
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

