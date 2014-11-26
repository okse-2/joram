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
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


import org.objectweb.joram.client.jms.XidImpl;

import framework.TestCase;
/**
 * Test :
 *    
 */

public class XABridgeTest3 extends TestCase {


  public static void main(String[] args) {
    new XABridgeTest3().run();
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
      Destination foreignDest = (Destination) jndiCtx.lookup("foreignQueue");
      XAConnectionFactory foreignCF = (XAConnectionFactory) jndiCtx.lookup("foreignCF");

      Destination joramDest = (Destination) jndiCtx.lookup("joramQueue");
      ConnectionFactory joramCF = (ConnectionFactory) jndiCtx.lookup("joramCF");
      jndiCtx.close();

      XAConnection foreignCnx = foreignCF.createXAConnection();
      XASession foreignSess = foreignCnx.createXASession();
      MessageConsumer foreignCons = foreignSess.createConsumer(foreignDest);
      XAResource resource = ((XASession) foreignSess).getXAResource();

      Connection joramCnx = joramCF.createConnection();
      Session joramSess = joramCnx.createSession(true, 0);
      MessageProducer joramSender = joramSess.createProducer(joramDest);
      foreignCnx.start();   

      TextMessage msg = joramSess.createTextMessage();

      for (int i = 1; i < 11; i++) {
        msg.setText("Joram message number " + i);
        System.out.println("send msg = " + msg.getText());
        joramSender.send(msg);
      }

      joramSess.commit();


      Xid xid = new XidImpl(new byte[0], 1, new String(""+System.currentTimeMillis()).getBytes());
      resource.start(xid, XAResource.TMNOFLAGS);
      System.out.println("resource = " + resource);


      for (int i = 1; i < 11; i++) {
        msg =(TextMessage) foreignCons.receive();
        if (msg != null){
          System.out.println("reiceive : " + msg.getText());
          assertEquals("Joram message number " + i, msg.getText());
        }else{
          System.out.println("msg = null");
          error(new Exception("msg == null"));
        }
      }

      System.out.println("commit xid = " + xid);
      resource.end(xid, XAResource.TMSUCCESS);
      resource.prepare(xid);
      resource.commit(xid, false);

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

