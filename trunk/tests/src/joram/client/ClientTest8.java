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
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import framework.TestCase;

/**
 * Testing:
 * - receive a message from a temporary
 * queue with a timeout. Then once the timeout has expired
 * deleting the queue should not raise an 
 * InvalidDestinationException.
 * 
 */
public class ClientTest8 extends TestCase {

    Exception excp=null;
  public static void main(String[] args) {
    new ClientTest8().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      User.create("anonymous", "anonymous", 0);

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
      
      Connection connection = cf.createConnection(
        "anonymous", "anonymous");
      connection.setExceptionListener(
        new ExceptionListener() {
            public void onException(JMSException exc) {
              excp = exc;
            }
          });
      
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

      // TextMessage msg = sendSession.createTextMessage("msg#1");
//       producer.send(msg);

      consumer.receive(1000);

      consumer.close();
      tmpQ.delete();
      System.out.println("Temp queue deleted");

      // Let the exception coming
      Thread.sleep(5000);
     
      assertEquals(null,excp);

      connection.close();
      Thread.sleep(5000);
      // exception on close
      assertTrue(excp instanceof javax.jms.IllegalStateException);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
