/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;



import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Testing:
 * - the deletion of a temporary queue: 
 * if there are existing receivers still using it, 
 * a JMSException will be thrown.
 * When queue is deleting there is InvalidDestinationException when try to send or receive
 *
 */
public class ClientTest6 extends TestCase {

  public static void main(String[] args) {
    new ClientTest6().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      
      Connection connection = cf.createConnection(
        "anonymous", "anonymous"); 
      
      Session recSession = connection.createSession(
        false,
        Session.CLIENT_ACKNOWLEDGE);
      TemporaryQueue tmpQ = recSession.createTemporaryQueue();
      MessageConsumer consumer = recSession.createConsumer(tmpQ);

      Session sendSession = connection.createSession(
        false,
        Session.CLIENT_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(tmpQ);

      connection.start();

      TextMessage msg = sendSession.createTextMessage("msg#1");
      producer.send(msg);

      msg = (TextMessage)consumer.receive();
      System.out.println("Received message " + msg.getText());
      
      try {
        System.out.println("Try to delete the temp queue");
        tmpQ.delete();
      } catch (Exception exc) {
        System.out.println("OK -> " + exc);
	assertTrue(exc instanceof javax.jms.JMSException);
      }

      consumer.close();

      System.out.println("Try to delete the temp queue");
      tmpQ.delete();
      System.out.println("Temp queue deleted");

      msg = sendSession.createTextMessage("msg#1");
      try {
        producer.send(msg);
      } catch (Exception exc) {
        System.out.println("OK -> " + exc);
	assertTrue(exc instanceof javax.jms.InvalidDestinationException);
      }
      
      consumer = recSession.createConsumer(tmpQ);
      try {
        System.out.println("Try to receive from the deleted temp queue");
        msg = (TextMessage)consumer.receive();
      } catch (Exception exc) {
        System.out.println("OK -> " + exc);
	assertTrue(exc instanceof javax.jms.InvalidDestinationException);
      }

      System.out.println("close connection");
      connection.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
