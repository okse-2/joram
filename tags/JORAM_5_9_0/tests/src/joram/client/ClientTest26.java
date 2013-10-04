/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 - ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Testing:
 * 
 * - Session recover from a message listener.
 * 
 * send one message with last property to false
 * send second message with last property to true
 * 
 * start MessageListener :
 * receive first message,
 * receive second and recover the session
 * 
 * expected re received the second message with the JMSRedelivered = true.
 * 
 * @see JORAM-41
 */
public class ClientTest26 extends TestCase {

  public static final int LOOP_NB = 10;

  public static void main(String[] args) {
    new ClientTest26().run();
  }

  private Connection connection;

  private Destination dest;

  private Session session;

  public void run() {
    try {
      startAgentServer((short) 0, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue queue = org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      dest = queue;

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      connection = cf.createConnection("anonymous", "anonymous");

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer consumer = session.createConsumer(dest);

      Session producerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = producerSession.createProducer(dest);

      connection.start();

      TextMessage msg = producerSession.createTextMessage("#" + 1);
      msg.setBooleanProperty("last", false);
      producer.send(msg);

      msg = producerSession.createTextMessage("#" + 2);
      msg.setBooleanProperty("last", true);
      producer.send(msg);

      consumer.setMessageListener(new MessageListener() {
        private int counter;

        public void onMessage(Message message) {
          try {
            synchronized (ClientTest26.this) {
              counter++;
              String text = ((TextMessage) message).getText();
              System.out.println("Received message " + text + ", last = "
                  + message.getBooleanProperty("last")
                  + ", getJMSRedelivered  = " + message.getJMSRedelivered());

              if (message.getBooleanProperty("last") == false) {
                System.out.println("first message isRedelivered = " + message.getJMSRedelivered());
                assertEquals("received first message, redelivered must be false", false, message.getJMSRedelivered());
              } else {
                if (message.getJMSRedelivered() == false) {
                  // received second message
                  System.out.println("==== second message: recover");
                  session.recover();
                } else {

                  // should be redelivered after recover
                  System.out.println("==== second message again as expected");
                  ClientTest26.this.notify();
                }
              }

              if (counter > 3) {
                ClientTest26.this.notify();
                fail("The message reiceved > 3.");
              }
            }
           
          } catch (Exception exc) {
            exc.printStackTrace();
          }
        }
      });

      synchronized (this) {
        // System.out.println("wait");
        wait(20000);
      }

      // System.out.println("close");
      connection.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
