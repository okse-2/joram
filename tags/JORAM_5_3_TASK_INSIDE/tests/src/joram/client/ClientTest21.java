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


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Redelivery : all messages receive by consumer 1 is redelivred because of abord.
 */
public class ClientTest21 extends TestCase {

  public static void main(String[] args) {
    new ClientTest21().run();
  }

  private static org.objectweb.joram.client.jms.Queue queue;
  
  private static Connection connection;

  private static Session recSession;

  private static MessageConsumer consumer;

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

      queue = org.objectweb.joram.client.jms.Queue.create(0, "test_queue");
      queue.setFreeReading();
      queue.setFreeWriting();
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      connection = cf.createConnection(
        "anonymous", "anonymous");
      
      recSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      
      Session sendSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      
      MessageConsumer consumer1 = recSession.createConsumer(queue);
      MessageConsumer consumer2 = recSession.createConsumer(queue);

      MessageProducer producer =
        sendSession.createProducer(queue);

      consumer1.setMessageListener(new MessageListener() {
          public void onMessage(Message msg) {
            // Abort the message receiving
	      /* try{
		  System.out.println("consumer1: on message " + ((TextMessage)msg).getText());
		  }catch(JMSException exc){}*/
            throw new RuntimeException("abort");
          }
        });

      consumer2.setMessageListener(new MessageListener() {
          public void onMessage(Message msg) {
	      /*  try{
	       System.out.println("consumer2: on message " + ((TextMessage)msg).getText());
	       }catch(JMSException exc){}*/
          }
        });

      connection.start();
      
      for (int i = 0; i < 5; i++) {
        producer.send(sendSession.createTextMessage("test" + i));
      }

      Thread.sleep(10000);

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();
    }
  }
}
