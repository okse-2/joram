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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Deadlock close-rollback
 */
public class ClientTest20 extends TestCase {

  public static void main(String[] args) {
    new ClientTest20().run();
  }

  private static org.objectweb.joram.client.jms.Topic topic;
      
  private static Connection connection;

  private static Session recSession;

  private static MessageConsumer consumer;

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      topic = org.objectweb.joram.client.jms.Topic.create(0, "test_topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      connection = cf.createConnection(
        "anonymous", "anonymous");
      connection.start();

      // Test 1: consumer closure
      init(new Runnable() {
          public void run() {
            try {
		//System.out.println("consumer close");
              consumer.close();
              //System.out.println("consumer closed");
            } catch (Exception exc) {} 
          }
        });

      Thread.sleep(1000);

      // Test 2: session closure
      init(new Runnable() {
          public void run() {
            try {
		// System.out.println("session close");
              recSession.close();
	      // System.out.println("session closed");
            } catch (Exception exc) {}
          }
        });

      Thread.sleep(1000);

      // Test 3: connection closure
      init(new Runnable() {
          public void run() {
            try {
		//System.out.println("connection close");
              connection.close();
              //System.out.println("connection closed");
            } catch (Exception exc) {}
          }
        });

      Thread.sleep(3000);

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();
    }
  }

  private static void init(final Runnable runnable) throws Exception {
    recSession = connection.createSession(true, 0);

    Session sendSession = connection.createSession(
      false,
      Session.AUTO_ACKNOWLEDGE);

    consumer = recSession.createConsumer(topic);

    MessageProducer producer =
      sendSession.createProducer(topic);

    producer.send(sendSession.createTextMessage("test"));

    consumer.setMessageListener(new MessageListener() {
        public void onMessage(Message msg) {
          try {
            new Thread(runnable).start();
            Thread.sleep(1000);
            //System.out.println("rollback");
            recSession.rollback();
            //System.out.println("rollbacked");
          } catch (Exception exc) {
	      // System.out.println("exception : "+exc);
	      assertTrue(exc instanceof  javax.jms.IllegalStateException);
	      // Illegal control thread for consumer and session
	  }
        }
      });
  }
}
