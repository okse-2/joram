/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Test memory leak, receive 100000 messages
 */
public class TopicListener extends TestCase {

  public static void main(String[] args) {
    new TopicListener().run();
  }

  private static org.objectweb.joram.client.jms.Topic topic;
  
  private Connection connection;

  private Object lock;

  public void run() {
    lock = new Object();

    try {
      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      topic = org.objectweb.joram.client.jms.Topic.create(0, "test_topic");
      topic.setFreeReading();
      topic.setFreeWriting();
      
      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);

      connection = cf.createConnection(
        "anonymous", "anonymous");
      
      Session session = connection.createSession(
        false, Session.AUTO_ACKNOWLEDGE);

      MessageConsumer cons = session.createConsumer(topic);

      cons.setMessageListener(new MessageListener() {
          public void onMessage(Message msg) {
            try {
              int i = msg.getIntProperty("counter");
	      // System.out.println("receive: msg#" + i);
	      assertTrue(((TextMessage)msg).getText().startsWith("test_"));
              if (i == TopicPublisher.NB_MSG) {
                synchronized (lock) {
                  lock.notify();
                }
              }
            } catch (Exception exc) {
              exc.printStackTrace();
            }
          }
        });

      connection.start();

      synchronized (lock) {
        lock.wait();
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
     
    }finally{
	 endTest();
    }
  }
}
