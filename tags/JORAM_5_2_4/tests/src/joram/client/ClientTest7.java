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


import java.io.File;

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
 * - close a transacted session -> rollback
 *
 */
public class ClientTest7 extends TestCase {

  public static final int LOOP_NB = 10;

  public static void main(String[] args) {
    new ClientTest7().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

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
        true,
        Session.SESSION_TRANSACTED);
      TemporaryQueue tmpQ = recSession.createTemporaryQueue();
      MessageConsumer consumer = recSession.createConsumer(tmpQ);

      Session sendSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(tmpQ);
      
      connection.start();

      for (int i = 0; i < LOOP_NB; i++) {
        TextMessage msg = sendSession.createTextMessage("msg#" + i);
        producer.send(msg);
      }

      for (int i = 0; i < LOOP_NB; i++) {
        TextMessage msg = (TextMessage)consumer.receive();
        System.out.println("Received message " + msg.getText());
      }

      System.out.println("Close the receiving session -> rollback");
      recSession.close();
      
      recSession = connection.createSession(
        true,
        Session.SESSION_TRANSACTED);
      consumer = recSession.createConsumer(tmpQ);
      
      for (int i = 0; i < LOOP_NB; i++) {
        TextMessage msg = (TextMessage)consumer.receive();
        System.out.println("Received message " + msg.getText());
      }

      System.out.println("close");
      producer.close();
      consumer.close();
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
