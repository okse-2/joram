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
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.JMSDistributionQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import framework.TestCase;

/**
 * Test: Test the bridge behavior during stop / restart of Joram server.
 *  - Sends 50 messages through a JMS DistributionQueue.
 *  - Stops the Joram server during sending, then restarts it.
 *  - Receives the sent messages on foreign queue.
 */
public class BridgeTest10x extends TestCase {
  public static void main(String[] args) {
    new BridgeTest10x().run();
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
        prop.put("distribution.async", "" + async);
        Queue joramOutQueue = JMSDistributionQueue.create(0, "BridgeOutQueue", "foreignQueue", prop);
        joramOutQueue.setFreeWriting();
        System.out.println("joramOutQueue = " + joramOutQueue);

        jndiCtx.rebind("joramOutQueue", joramOutQueue);
        jndiCtx.rebind("joramCF", joramCF);
        jndiCtx.close();

        AdminModule.disconnect();
        System.out.println("Admin closed.");
      }catch(Exception exc){
      }

      System.out.println("admin config ok");
      Thread.sleep(1000);

      for (int i=0; i<20; i++)
        test(100);
      
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
  
  final static long timeout = 5L;

  void test(final int msgs) throws Exception {
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
    Destination joramOutDest = (Destination) jndiCtx.lookup("joramOutQueue");
    ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
    ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
    Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");      
    jndiCtx.close();

    Connection joramCnx = joramCF.createConnection();
    Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer joramProd = joramSess.createProducer(joramOutDest);
    joramCnx.start();

    Thread t = new Thread() {
      public void run() {
        try {
          Thread.sleep((2*timeout/5)*msgs);
          killAgentServer((short) 0);
//          System.out.println("Joram server stopped.");
          startAgentServer((short) 0, new String[]{"-DTransaction.UseLockFile=false"});
//          System.out.println("Joram server started.");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
    
    int nbmsg = 0;
    try {
      TextMessage msgOut = joramSess.createTextMessage();
      for (; nbmsg < msgs; nbmsg++) {
        msgOut.setText("Message number " + nbmsg);
        joramProd.send(msgOut);
        System.out.println("send msg = " + msgOut.getText());
        Thread.sleep(timeout);
      }
    } catch (Exception exc) {
      System.out.println("Error during sending msg " + nbmsg + ", " + exc.getMessage());
    }

    Connection foreignCnx = foreignCF.createConnection();
    Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer foreignCons = foreignSess.createConsumer(foreignDest);
    foreignCnx.start();

    TextMessage msgIn;
    for (int i = 0; i < msgs; i++) {
      msgIn = (TextMessage) foreignCons.receive(5000);
      if (msgIn != null) {
        if (! msgIn.getText().equals("Message number " + i)) {
          if (((i == nbmsg) || (i == (nbmsg+1))) && (msgIn.getText().equals("Message number " + (i-1)))) {
            // The last message can be sent twice to the foreign server, one time before the crash
            // and a second time after (the first time the implicit acknowledge is lost due to the
            // crash). The last incoming message in the server could be the nbmsg-1 as the nbmsg could
            // be lost between the JMS client and the broker s0.
            System.out.println("Receive twice last message");
          } else {
            assertEquals("Message number " + i, msgIn.getText());
            System.out.println("Error during receiving msg " + i + ": " + msgIn.getText());
            throw new Exception();
          }
        }
      } else {
        // The server failure could have happened just after the transmission of the message to
        // the server and before it was implicitly acquitted to the client. In this case i should
        // be equals to nbmsg+1, otherwise it should be equals to nbmsg.
        System.out.println("Error not receiving msg " + i);
        assertTrue("Number of message received " + i + " should be " + nbmsg, (nbmsg <= i));
        if (i < nbmsg) 
          throw new Exception();
        break;
      }
      System.out.println("receive msg = " + msgIn.getText());
    }
    
    joramCnx.close();
  }
}

