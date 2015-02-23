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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Test memory leak
 */
public class ClientTest23 extends TestCase {

  public static void main(String[] args) {
    new ClientTest23().run();
  }

  private Connection connection;

  private MessageProducer producer;

  private MessageConsumer consumer1;

  private MessageConsumer consumer2;

  private Session prodSession;

  public void run() {
    try {

      startAgentServer(
        (short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      Thread.sleep(1000);

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Topic topic = 
        org.objectweb.joram.client.jms.Topic.create(0, "test_topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      connection = cf.createConnection(
        "anonymous", "anonymous");

      Session consSession = connection.createSession(
        false, Session.AUTO_ACKNOWLEDGE);
      
      prodSession = connection.createSession(
        false, Session.AUTO_ACKNOWLEDGE);
      
      producer = prodSession.createProducer(topic);
      
      consumer1 = consSession.createConsumer(topic);
      consumer2 = consSession.createConsumer(topic);
      
      consumer1.setMessageListener(new MessageListener() {
          public void onMessage(Message msg) {
            try {
              TextMessage tm = (TextMessage)msg;
              //System.out.println("consumer1: " + tm.getText());
            } catch (Exception exc) {
		// exc.printStackTrace();
	      assertTrue(exc instanceof javax.jms.IllegalStateException);
              // bye bye
            }
          }
        });

      consumer2.setMessageListener(new MessageListener() {
          public void onMessage(Message msg) {
            try {
              TextMessage tm = (TextMessage)msg;
              //System.out.println("consumer2: " + tm.getText());
            } catch (Exception exc) {
		// exc.printStackTrace();
		assertTrue(exc instanceof javax.jms.IllegalStateException);
              // bye bye
            }
          }
        });

      connection.start();
        
      new Thread() {
        public void run() {
          int i = 0;
          try {
            while (true) {
		//System.out.println("producer: " + i);
              producer.send(
                prodSession.createTextMessage("test_" + i));
              Thread.sleep(10);
              i++;
            }
          } catch (Exception exc) {
	      //exc.printStackTrace();
	    assertTrue(exc instanceof javax.jms.IllegalStateException);
            // bye bye
          }
        }
      }.start();
      
      Thread.sleep(60000);

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
