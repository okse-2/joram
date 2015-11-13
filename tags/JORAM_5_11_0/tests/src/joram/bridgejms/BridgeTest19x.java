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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSAcquisitionTopic;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Test issue with flow regulation using AcquisitionQueue.
 */
public class BridgeTest19x extends TestCase implements MessageListener {
  public static void main(String[] args) {
    new BridgeTest19x().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      try{
        ConnectionFactory bridgeCF = TcpConnectionFactory.create("localhost", 16010);
        ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);

        AdminModule.connect(bridgeCF, "root", "root");
        javax.naming.Context jndiCtx = new javax.naming.InitialContext();

        User.create("anonymous", "anonymous", 0);
        User.create("anonymous", "anonymous", 1);

        // create The foreign destination and connectionFactory
        Queue foreignQueue1 = Queue.create(1, "foreignQueue1");
        foreignQueue1.setFreeReading();
        foreignQueue1.setFreeWriting();
        System.out.println("foreign queue#1 = " + foreignQueue1);

        // create The foreign destination and connectionFactory
        Queue foreignQueue2 = Queue.create(1, "foreignQueue2");
        foreignQueue2.setFreeReading();
        foreignQueue2.setFreeWriting();
        System.out.println("foreign queue#2 = " + foreignQueue2);

        // bind foreign destination and connectionFactory
        jndiCtx.rebind("foreignQueue1", foreignQueue1);
        jndiCtx.rebind("foreignQueue2", foreignQueue2);
        jndiCtx.rebind("foreignCF", foreignCF);
      
        // Setting the bridge properties
        Properties prop1 = new Properties();
        prop1.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop1.setProperty("period", "1000");
        prop1.setProperty("acquisition.max_msg", "2");
        prop1.setProperty("acquisition.min_msg", "0");
        
        // Creating a Queue bridge on server 0:
        Topic joramInTopic1 = JMSAcquisitionTopic.create(0, "joramInTopic1", "foreignQueue1", prop1);
        joramInTopic1.setFreeReading();
        joramInTopic1.setFreeWriting();
        System.out.println("joramInTopic1 = " + joramInTopic1);

        // Setting the bridge properties
        Properties prop2 = new Properties();
        prop2.setProperty("jms.ConnectionUpdatePeriod", "1000");
        prop2.setProperty("period", "1000");
        prop2.setProperty("acquisition.max_msg", "10");
        prop2.setProperty("acquisition.min_msg", "5");
        
        // Creating a Queue bridge on server 0:
        Topic joramInTopic2 = JMSAcquisitionTopic.create(0, "joramInTopic2", "foreignQueue2", prop2);
        joramInTopic2.setFreeReading();
        joramInTopic2.setFreeWriting();
        System.out.println("joramInTopic2 = " + joramInTopic2);

        jndiCtx.rebind("joramInTopic1", joramInTopic1);
        jndiCtx.rebind("joramInTopic2", joramInTopic2);
        jndiCtx.rebind("joramCF", bridgeCF);
        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }catch(Exception exc){
        exc.printStackTrace();
      }

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination joramInDest1 = (Destination) jndiCtx.lookup("joramInTopic1");
      System.out.println("joramInDest1 = " + joramInDest1);
      Destination joramInDest2 = (Destination) jndiCtx.lookup("joramInTopic2");
      System.out.println("joramInDest2 = " + joramInDest2);
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
      Destination foreignDest1 = (Destination) jndiCtx.lookup("foreignQueue1");
      System.out.println("foreignDest1 = " + foreignDest1);  
      Destination foreignDest2 = (Destination) jndiCtx.lookup("foreignQueue2");
      System.out.println("foreignDest2 = " + foreignDest2);      
      jndiCtx.close();

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer foreignProd1 = foreignSess.createProducer(foreignDest1);
      MessageProducer foreignProd2 = foreignSess.createProducer(foreignDest2);
      foreignCnx.start();

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons1 = joramSess.createConsumer(joramInDest1);
      joramCons1.setMessageListener(this);
      MessageConsumer joramCons2 = joramSess.createConsumer(joramInDest2);
      joramCons2.setMessageListener(this);
      joramCnx.start(); 

      for (int i=0; i<10000; i++) {
        TextMessage msg1 = foreignSess.createTextMessage("msg1#" + i);
        foreignProd1.send(msg1);
        TextMessage msg2 = foreignSess.createTextMessage("msg2#" + i);
        foreignProd2.send(msg2);
      }
      
      for (int i=0; i<60; i++)
        if (nbmsg != 20000)
          Thread.sleep(2000);
        else
          break;      
      assertTrue(nbmsg == 20000);

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

  volatile int nbmsg = 0;
  
  @Override
  public void onMessage(Message m) {
    try {
      TextMessage msg = (TextMessage) m;
      nbmsg += 1;
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  }
}
