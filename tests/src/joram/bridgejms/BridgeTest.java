/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 - 2012 ScalAgent Distributed Technologies
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
package joram.bridgejms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import framework.TestCase;

/**
 * Test: producer on foreign queue, consumer on bridge queue
 *  - sends 10 messages on foreign server, receives them on bridge destination
 *  - stops then restarts the foreign server
 *  - sends anew 10 messages on foreign server, receives them on bridge destination
 */
public class BridgeTest extends TestCase {

  public static void main(String[] args) {
    new BridgeTest().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(8000);
      // admin();

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
      ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF");
      Destination joramDest = (Destination) jndiCtx.lookup("joramQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();
      System.out.println("admin config ok");

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer joramCons = joramSess.createConsumer(joramDest);
      MessageProducer foreignSender = foreignSess.createProducer(foreignDest);
      foreignCnx.start();
      joramCnx.start(); 

      TextMessage foreignMsg = foreignSess.createTextMessage();
      for (int i = 1; i < 11; i++) {
        foreignMsg.setText("Foreign message number " + i);
        System.out.println("send msg = " + foreignMsg.getText());
        foreignSender.send(foreignMsg);
      }

      int nbmsg = 0;
      TextMessage msg;
      for (int i = 1; i < 11; i++) { 
        msg=(TextMessage) joramCons.receive(5000);
        if (msg != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msg.getText());
        assertEquals("Foreign message number "+i,msg.getText());
      }
      assertEquals(10, nbmsg);
      
      msg=(TextMessage) joramCons.receiveNoWait();
      assertTrue(msg == null);
      
      foreignMsg = foreignSess.createTextMessage();
      foreignMsg.setText("New foreign message");
      foreignSender.send(foreignMsg);
     
      Thread.sleep(1000L);
      
      msg = (TextMessage) joramCons.receive();
      assertTrue(msg != null);
      if (msg != null)
        assertEquals(foreignMsg.getText(), msg.getText());

      foreignCnx.close();
      stopAgentServer((short)1);
      System.out.println("Foreign server stopped.");
      
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Foreign server started.");
      Thread.sleep(5000);
      
      foreignCnx = foreignCF.createConnection();
      foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      foreignSender = foreignSess.createProducer(foreignDest);
      foreignCnx.start();

      foreignMsg = foreignSess.createTextMessage();
      for (int i = 1; i < 11; i++) {
        foreignMsg.setText("Foreign message number " + i);
        System.out.println("send msg = " + foreignMsg.getText());
        foreignSender.send(foreignMsg);
      }

//      killAgentServer((short)0);
//      System.out.println("Joram server stopped.");
//      startAgentServer((short)0, new File("."), new String[]{"-DTransaction.UseLockFile=false"});
//      System.out.println("Joram server started.");
//      Thread.sleep(10000);
//
//      joramCnx = joramCF.createConnection();
//      joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
//      joramCons = joramSess.createConsumer(joramDest);
//      joramCnx.start(); 

      for (int i = 1; i < 11; i++) { 
        msg=(TextMessage) joramCons.receive(5000);
        if (msg != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msg.getText());
        assertEquals("Foreign message number "+i,msg.getText());
      }
      
      assertEquals(20, nbmsg);

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

