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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.AMQPAcquisitionTopic;
import org.objectweb.joram.client.jms.admin.AMQPDistributionTopic;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test: Creates 2 bridge topics through administration API and tests them.
 */
public class AdminTest1b extends TestCase {

  public static void main(String[] args) {
    new AdminTest1b().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);
      
      // Administration code
      {
        javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create("localhost", 16010);

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
        
        Topic joramInTopic = AMQPAcquisitionTopic.create(0, "BridgeInTopic", "amqpQueue", prop1);
        joramInTopic.setFreeReading();
        joramInTopic.setFreeWriting();
        System.out.println("joramInTopic = " + joramInTopic);

        // Setting the bridge properties
        Properties prop2 = new Properties();
        prop2.setProperty("period", "1000");      
        prop2.setProperty("amqp.Queue.DeclarePassive", "false");
        prop2.setProperty("amqp.Queue.DeclareExclusive", "false");
        prop2.setProperty("amqp.Queue.DeclareDurable", "true");
        prop2.setProperty("amqp.Queue.DeclareAutoDelete", "false");
        prop2.setProperty("amqp.ConnectionUpdatePeriod", "1000");

        Topic joramOutTopic = AMQPDistributionTopic.create(0, "BridgeOutTopic", "amqpQueue", prop2);
        joramOutTopic.setFreeWriting();
        System.out.println("joramOutTopic = " + joramOutTopic);

        jndiCtx.rebind("joramInTopic", joramInTopic);
        jndiCtx.rebind("joramOutTopic", joramOutTopic);
        jndiCtx.rebind("joramCF", joramCF);

        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }
      System.out.println("admin config ok");
      Thread.sleep(1000);
      
      // Gets administered objects from JNDI
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination joramInDest = (Destination) jndiCtx.lookup("joramInTopic");
      Destination joramOutDest = (Destination) jndiCtx.lookup("joramOutTopic");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();
      
      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramInDest);
      MessageProducer joramProd = joramSess.createProducer(joramOutDest);
      joramCnx.start(); 
      
      TextMessage msgOut = joramSess.createTextMessage();
      for (int i = 1; i < 11; i++) {
        msgOut.setText("Message number " + i);
        System.out.println("send msg = " + msgOut.getText());
        joramProd.send(msgOut);
      }

      int nbmsg = 0;
      TextMessage msgIn;
      for (int i = 1; i < 11; i++) {
        msgIn=(TextMessage) joramCons.receive(5000);
        if (msgIn != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msgIn.getText());
        assertEquals("Message number " + i, msgIn.getText());
      }
      assertEquals(10, nbmsg);

      joramCnx.close();      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      killAgentServer((short)0);
      endTest(); 
    }
  }
}

