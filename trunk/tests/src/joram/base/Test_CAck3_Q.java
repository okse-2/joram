/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):(ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.base;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * Test that the ack is in session level. If close consumer without ack, message not redelivred .
 * If close session without ack, message is redelivred.
 *
 */
public class Test_CAck3_Q extends TestCase{

  private static MessageConsumer msgCons;

  private static Session consSession;
    private static Enumeration enumMessage;
    private static QueueBrowser browser;

  public static void main (String args[])  {
      new Test_CAck3_Q().run();
  }
    public static void run(){
      try{
	  AgentServer.init((short) 0, "./s0", null);
	  AgentServer.start();
	  
	  org.objectweb.joram.client.jms.admin.AdminModule.collocatedConnect(
									     "root", "root");
	  
	  Destination dest = org.objectweb.joram.client.jms.Queue.create(0);
	  dest.setFreeReading();
	  dest.setFreeWriting();
	  
	  User user = User.create("anonymous", "anonymous", 0);
	  org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
	  
	  ConnectionFactory cf = 
	      new org.objectweb.joram.client.jms.local.LocalConnectionFactory();
	  Connection cnx = cf.createConnection();
	  
	  consSession = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
	  Session prodSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	  
	  MessageProducer msgProd = prodSession.createProducer(dest);
	  cnx.start();
	  
	  msgCons = consSession.createConsumer(dest);
	  
	  TextMessage msg = prodSession.createTextMessage("test1-1");
	  //System.out.println("Send msg: " + msg.getText());
	  msgProd.send(msg);
	  TextMessage msgC =(TextMessage) msgCons.receive();
	  assertEquals("test1-1",msgC.getText());
	  
	  //close consumer
	  msgCons.close();
	  msgCons= consSession.createConsumer(dest);
	  msg = prodSession.createTextMessage("test1-2");
	  //System.out.println("Send msg: " + msg.getText());
	  msgProd.send(msg);
	  // receive after restart consumer
	  msgC =(TextMessage) msgCons.receive();
	  // the message receive is message 2
	  assertEquals("test1-2",msgC.getText());
	  
	  // close and start a seesion
	  consSession.close();
	  consSession = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
	  msgCons= consSession.createConsumer(dest);
	  // message receive is 1, session close without ack -> message lost 
	  msgC =(TextMessage) msgCons.receive();
	  assertEquals("test1-1",msgC.getText());
	  
	  
	  cnx.close();
      }catch(Throwable exc) {
	  exc.printStackTrace();
	  error(exc);
      }
      finally {
	  System.out.println("Server stop ");
	  AgentServer.stop();
	  endTest(); 
      }
  }
}
