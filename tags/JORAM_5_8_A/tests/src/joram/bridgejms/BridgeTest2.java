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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import framework.TestCase;

/**
 * Test: publisher on foreign topic and subscriber on bridge topic
 *  - sends 10 messages on foreign server, receives them on bridge destination
 *  - sends 10 messages on foreign server, receives them on bridge destination through
 *  a listener
 *  - stops then restarts the foreign server
 *  - sends anew 10 messages on foreign server, receives them on bridge destination through
 *  a listener
 */

class MsgListenerb implements MessageListener {
  String who;
  int count;
  public MsgListenerb(String who, int idx) {
    this.who = who;
    count=idx;
  }

  public void onMessage(Message msg) {
    try {
      count++;
      TextMessage msg2=(TextMessage) msg;
      BridgeTest2.assertEquals("topic Foreign message number " + count, msg2.getText());
      System.out.println(who+" receive msg = " + msg2.getText());
    }catch (JMSException exc) {
      System.err.println("Exception in listener: " + exc);
    }
  }
}

public class BridgeTest2 extends TestCase {


  public static void main(String[] args) {
    new BridgeTest2().run();
  }

  public void run() {
    try {
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      //admin();
      Thread.sleep(8000);

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
      Destination joramDest = (Destination) jndiCtx.lookup("joramTopic");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      Destination foreignDest = (Destination) jndiCtx.lookup("foreignTopic");
      ConnectionFactory foreignCF = (ConnectionFactory) jndiCtx.lookup("foreignCF"); 
      jndiCtx.close();

      System.out.println("admin config ok");

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramDest);
      joramCnx.start();  

      Connection foreignCnx = foreignCF.createConnection();
      Session foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer foreignSender = foreignSess.createProducer(foreignDest);

      TextMessage foreignMsg = foreignSess.createTextMessage();

      for (int i = 1; i < 11; i++) {
        foreignMsg.setText("topic Foreign message number " + i);
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
        assertEquals("topic Foreign message number "+i,msg.getText());
      }
      assertEquals(10, nbmsg);

      // Using a message listener :
      MsgListenerb listener = new MsgListenerb("topic joram", 10);
      joramCons.setMessageListener(listener);

      for (int i = 11; i < 21; i++) {
        foreignMsg.setText("topic Foreign message number " + i);
        System.out.println("send msg = " + foreignMsg.getText());
        foreignSender.send(foreignMsg);
      }

      Thread.sleep(2000);
      assertEquals(20, listener.count);

      foreignCnx.close();
      stopAgentServer((short)1);
      System.out.println("Foreign server stopped.");
      
      startAgentServer((short)1, new String[]{"-DTransaction.UseLockFile=false"});
      System.out.println("Foeign server started.");
      Thread.sleep(5000);
      
      foreignCnx = foreignCF.createConnection();
      foreignSess = foreignCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      foreignSender = foreignSess.createProducer(foreignDest);
      foreignCnx.start();
      for (int i = 21; i < 31; i++) {
        foreignMsg.setText("topic Foreign message number " + i);
        System.out.println("send msg = " + foreignMsg.getText());
        foreignSender.send(foreignMsg);
      }

      Thread.sleep(2000);
      assertEquals(10, nbmsg);
      assertEquals(30, listener.count);

      joramCnx.close();
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
}

