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
package joram.bridge;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnectionFactory;

import framework.TestCase;

/**
 * Test :
 *    
 */

public class XABridgeTest2 extends TestCase {


  public static void main(String[] args) {
    new XABridgeTest2().run();
  }

  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0);
      startAgentServer((short)1);
      Thread.sleep(8000);
      //admin();
      System.out.println("admin config ok");

      javax.naming.Context jndiCtx = new javax.naming.InitialContext();
      Destination foreignDest = (Destination) jndiCtx.lookup("foreignTopic");
      XAConnectionFactory foreignCF = (XAConnectionFactory) jndiCtx.lookup("foreignCF");

      Destination joramDest = (Destination) jndiCtx.lookup("joramTopic");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();
      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer joramCons = joramSess.createConsumer(joramDest);

      Connection foreignCnx = foreignCF.createXAConnection();
      Session foreignSess = foreignCnx.createSession(true, 0);
      MessageProducer foreignSender = foreignSess.createProducer(foreignDest);
      joramCnx.start();
      TextMessage foreignMsg = foreignSess.createTextMessage();

      for (int i = 1; i < 11; i++) {
        foreignMsg.setText("topic Foreign message number " + i);
        System.out.println("send msg = " + foreignMsg.getText());
        foreignSender.send(foreignMsg);
      }
      foreignSess.commit();



      TextMessage msg;
      for (int i = 1; i < 11; i++) { 
        msg=(TextMessage)joramCons.receive();
        System.out.println("receive msg = " + msg.getText());
        assertEquals("topic Foreign message number "+i,msg.getText());
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

