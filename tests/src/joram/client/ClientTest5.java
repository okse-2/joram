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
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Testing:
 *
 * 1- Session recover from a message listener.
 */
public class ClientTest5 extends TestCase {

  public static final int LOOP_NB = 10;

  public static void main(String[] args) {
    new ClientTest5().run();
  }

  private Connection connection;

  private Destination dest;

  private Session session;

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

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      dest = queue;

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      connection = cf.createConnection(
       "anonymous", "anonymous"); 

      session = connection.createSession(
        false,
        Session.CLIENT_ACKNOWLEDGE);

      MessageConsumer consumer = session.createConsumer(dest);

      Session producerSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = producerSession.createProducer(dest);

      connection.start();

      for (int i = 0; i < LOOP_NB; i++) {
        TextMessage msg = producerSession.createTextMessage("#" + i);
        producer.send(msg);
      }
      
      consumer.setMessageListener(
        new MessageListener() {
            private int counter;

            public void onMessage(Message message) {
              try {
                synchronized (ClientTest5.this) {
                  counter++;
                  //System.out.println("counter = " + counter);
                  //String text = ((TextMessage)message).getText();
                  //System.out.println("Received message " + text);
                  if (counter == LOOP_NB/2) {
		      System.out.println("recover !");
                    session.recover();
                  }                  
                  if (counter == LOOP_NB + LOOP_NB/2) {
		      //System.out.println("notify");
                    ClientTest5.this.notify();
                  }
                }
              } catch (Exception exc) {
                exc.printStackTrace();
              }
            }
          });
      
      synchronized (this) {
	  //System.out.println("wait");
        wait();
      }

      //System.out.println("close");
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

