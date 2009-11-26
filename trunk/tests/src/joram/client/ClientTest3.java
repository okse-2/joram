/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
package joram.client;



import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Tests a concurrent close while receiving messages
 * from a queue in a synchronous way. This used to block
 * with Joram 4.1. This bug has been reported by Anton Koinov
 * (it was actually a well-known bug). 
 * The following test is close to the one he proposed
 * (see the joram mailing list).
 */
public class ClientTest3 extends TestCase {

  public static final int LOOP_NB = 100;

  public static void main(String[] args) {
    new ClientTest3().run();
  }

  private volatile Connection connection;

  public void run() {
    try {
      startAgentServer(
                       (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      Queue queue = Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      for (int i = 0; i < LOOP_NB; i++) {
        //System.out.println("+ Iteration #" + i);

        connection = cf.createConnection("anonymous", "anonymous");

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        MessageProducer producer = session.createProducer(queue);
        MessageConsumer consumer = session.createConsumer(queue);

        connection.start();

        producer.send(session.createTextMessage());

        new Thread() {
          public void run() {
            try {
              connection.close();
            } catch (JMSException exc) {}
          }
        }.start();

        try {
          //System.out.println("| before receive()");
          consumer.receive();
          //System.out.println("| after receive()");
        } catch (JMSException exc) {
          //System.out.println("| OK -> Receive aborted: " + exc);
          assertTrue(exc instanceof javax.jms.IllegalStateException);
        }
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}
