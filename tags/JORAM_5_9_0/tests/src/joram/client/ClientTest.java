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
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Testing:
 *
 * 1- an anticipated connection start with
 * late setup operations ended by a
 * message listener subscription that makes 
 * impossible any further operations. 
 * In particular if a client desires to have one 
 * thread producing messages while others
 * consume them, the client should use a separate session 
 * for its producing thread.
 * 
 * 2- a connection *stop* must wait until all 
 * of message listeners have returned before it may return. 
 * While these message listeners are completing, they must
 * have the full services of the connection available to them. 
 *
 * 3- During a connection *close*, if one or more of the 
 * connection's sessions' message listeners is processing a message 
 * at the time when connection close is invoked, all the facilities 
 * of the connection and its sessions must remain available to those 
 * listeners until they return control to the JMS provider. 
 *
 */
public class ClientTest extends TestCase {

  public static final int LOOP_NB = 1000;

  public static void main(String[] args) {
    new ClientTest().run();
  }

  private Connection connection;

  private Destination dest;

  public void run() {
    try {
      startAgentServer( (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

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

      //System.out.println("Anticipated connection start");
      connection.start();
      
      //System.out.println("Late connection setup");
      //System.out.println("Create a session");
      Session session = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      
      //System.out.println("Create a producer");
      MessageProducer producer = session.createProducer(dest);

      //System.out.println("Create a message listener");
      MessageConsumer consumer = session.createConsumer(dest);

      consumer.setMessageListener(
        new MessageListener() {
            public void onMessage(Message message) {              
              try {
                synchronized (ClientTest.this) {
                  ClientTest.this.notify();
                }
                
                // The message listener must have the full 
                // services of the connection.
                for (int i = 0; i < LOOP_NB; i++) {
		    //System.out.println("Create session#" + i);
                  Session s = connection.createSession(
                    false,
                    Session.AUTO_ACKNOWLEDGE);
                }
              } catch (JMSException exc) {
                exc.printStackTrace();
              }
            }
          });

      //System.out.println("Try to use the session with another control thread");
      try {
        producer.send(session.createTextMessage());
      } catch (javax.jms.IllegalStateException exc) {
        System.out.println("OK -> " + exc);
      } 
      
      //System.out.println("Create a separate session for the producer");
      Session producerSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      producer = producerSession.createProducer(dest);

      synchronized (this) {
        System.out.println("Send a message");
        producer.send(session.createTextMessage());
        wait();
      }

      //System.out.println("Concurrent stop of the connection");
      connection.stop();
      //System.out.println("Connection stopped");

      //System.out.println("Connection started");
      connection.start();

      synchronized (this) {
	  //System.out.println("Send a message");
        producer.send(session.createTextMessage());
        wait();
      }
      
      //System.out.println("Concurrent close of the connection");
      connection.stop();
      //System.out.println("Connection closed");
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();     
    }
  }
}

