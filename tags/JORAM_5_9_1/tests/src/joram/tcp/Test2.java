/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
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
package joram.tcp;

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;


/**
 * 3 agent servers: 0, 1, 2
 * 2 users and 2 connections with servers 0 and 2
 * 2 destinations on server 1: 1 queue and 1 topic
 * 
 * Mode transacted.
 * 
 * Producer MESSAGE_NUMBER messages from server 0.
 * Consume MESSAGE_NUMBER messages from server 2.
 *
 * @author feliot
 *
 */
public class Test2 extends TestCase {

  public static final int MESSAGE_NUMBER = 10;

  public Test2() {
    super();
  }

  public void run() {
    try {
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      startAgentServer((short) 2);
      
      AdminModule.connect("localhost", 2560,
                    "root", "root", 60);
      
      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 0);
      org.objectweb.joram.client.jms.admin.User user2 = 
        org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous", 2);
      
      org.objectweb.joram.client.jms.Topic topic = 
        org.objectweb.joram.client.jms.Topic.create(1);
      topic.setFreeReading();
      topic.setFreeWriting();

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(1);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf0 = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);
      ConnectionFactory cf2 = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2562);      

      // Test - Queue sender-receiver
      Connection cc0 = cf0.createConnection();
      Session s0 = cc0.createSession(true, 0);
      MessageProducer p0 = s0.createProducer(queue);
      TextMessage message = s0.createTextMessage();
      cc0.start();
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        message.setText("Message " + i);
        p0.send(message);
      }
      s0.commit();

      Connection cc2 = cf2.createConnection();
      Session s2 = cc2.createSession(true, 0);
      MessageConsumer mc2 = s2.createConsumer(queue);
      cc2.start();
      for (int i = 0; i < MESSAGE_NUMBER; i++) {
        message = (TextMessage)mc2.receive();
	// System.out.println("Receive " + message.getText());
	assertEquals("Message " + i, message.getText());
      }
      s2.commit();
      
      p0.close();
      s0.close();
      cc0.close();
      
      mc2.close();
      s2.close();
      cc2.close();
      
      AdminModule.disconnect();
    } catch (Exception exc) {
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      stopAgentServer((short) 1);
      stopAgentServer((short) 2);
      endTest();     
    }
  }

  public static void main(String args[]) {
    new Test2().run();
  }
}
