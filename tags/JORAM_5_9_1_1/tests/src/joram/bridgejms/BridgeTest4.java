/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import framework.TestCase;


/**
 * Test: producer on bridge queue and consumer on foreign queue
 *  - sends 10 messages on Joram server, receives them on foreign destination
 *  - stops then restarts the foreign server
 *  - sends anew 10 messages on Joram server, receives them on foreign destination
 *  - stops the foreign server
 *  - sends 10 messages on Joram server
 *  - starts the foreign server
 *  - receives them on foreign destination
 */
public class BridgeTest4 extends TestCase {

  public static void main(String[] args) {
    new BridgeTest4().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(8000);

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

      MessageProducer joramSender = joramSess.createProducer(joramDest);
      MessageConsumer foreignCons = foreignSess.createConsumer(foreignDest);
      foreignCnx.start();
      joramCnx.start(); 

      TextMessage msg = joramSess.createTextMessage();

      for (int i = 1; i < 11; i++) {
        msg.setText("Joram message number " + i);
        System.out.println("send msg = " + msg.getText());
        joramSender.send(msg);
      }

      int nbmsg = 0;
      for (int i = 1; i < 11; i++) { 
        msg=(TextMessage) foreignCons.receive(5000);
        if (msg != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msg.getText());
        assertEquals("Joram message number "+i,msg.getText());
      }
      assertEquals(10, nbmsg);

      foreignCnx.close();
      stopAgentServer((short)1);
      System.out.println("Bridge server stopped.");
      
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Bridge server started.");
      Thread.sleep(5000);
      
      foreignCnx = foreignCF.createConnection();
      foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      foreignCons = foreignSess.createConsumer(foreignDest);
      foreignCnx.start();

      msg = joramSess.createTextMessage();
      for (int i = 11; i < 21; i++) {
        msg.setText("Joram message number " + i);
        System.out.println("send msg = " + msg.getText());
        joramSender.send(msg);
      }
      
      for (int i = 11; i < 21; i++) { 
        msg=(TextMessage) foreignCons.receive(5000);
        if (msg != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msg.getText());
        assertEquals("Joram message number "+i,msg.getText());
      }
      assertEquals(20, nbmsg);

      foreignCnx.close();
      stopAgentServer((short)1);
      System.out.println("Bridge server stopped.");

      msg = joramSess.createTextMessage();
      for (int i = 21; i < 31; i++) {
        msg.setText("Joram message number " + i);
        System.out.println("send msg = " + msg.getText());
        joramSender.send(msg);
      }
      
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Bridge server started.");
      Thread.sleep(5000);
      
      foreignCnx = foreignCF.createConnection();
      foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      foreignCons = foreignSess.createConsumer(foreignDest);
      foreignCnx.start();
      
      for (int i = 21; i < 31; i++) { 
        msg=(TextMessage) foreignCons.receive(5000);
        if (msg != null) {
          nbmsg += 1;
        } else {
          assertTrue("Message not received", false);
          break;
        }
        System.out.println("receive msg = " + msg.getText());
        assertEquals("Joram message number "+i,msg.getText());
      }
      assertEquals(30, nbmsg);

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

