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
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.dest.jms.JMSDistribution;

import framework.TestCase;


/**
 * Test: producer on bridge queue and consumer on foreign queue
 *  - stops the foreign server
 *  - sends a message on Joram server
 *  - restarts the foreign server
 *  - receives the message on foreign destination
 */
public class BridgeTestX extends TestCase {

  public static void main(String[] args) {
    new BridgeTestX().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(5000);

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
      System.out.println("foreign queue = " + foreignQueue);

      javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

      // bind foreign destination and connectionFactory
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
      jndiCtx.rebind("foreignQueue", foreignQueue);
      jndiCtx.rebind("foreignCF", foreignCF);
      jndiCtx.close();
      
      // Setting the bridge properties
      Properties prop2 = new Properties();
      prop2.setProperty("jms.DestinationName", "foreignQueue");
      prop2.setProperty("jms.ConnectionUpdatePeriod", "1000");
      prop2.setProperty("period", "1000");      
      prop2.put("distribution.async", "" + async);
      prop2.setProperty("distribution.className", JMSDistribution.class.getName());

      Queue joramQueue = Queue.create(0, "joramQueue", Queue.DISTRIBUTION_QUEUE, prop2);
      joramQueue.setFreeWriting();
      System.out.println("joramQueue = " + joramQueue);

      AdminModule.disconnect();
      System.out.println("admin config ok");

      stopAgentServer((short)1);
      System.out.println("Bridge server stopped.");

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer joramSender = joramSess.createProducer(joramQueue);
      joramCnx.start(); 

      TextMessage msg = joramSess.createTextMessage();
      msg.setText("Joram message");
      System.out.println("send msg = " + msg.getText());
      joramSender.send(msg);

      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Bridge server started.");
      Thread.sleep(5000);

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer foreignCons = foreignSess.createConsumer(foreignQueue);
      foreignCnx.start();

      msg=(TextMessage) foreignCons.receive(30000);
      if (msg != null) {
        System.out.println("receive msg = " + msg.getText());
        assertEquals("Joram message", msg.getText());
      } else {
        assertTrue("Message not received", false);
      }

      foreignCnx.close();
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

