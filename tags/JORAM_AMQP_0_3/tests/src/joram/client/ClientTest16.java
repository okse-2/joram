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
import javax.jms.Session;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Tests that when the connectingTimer of a
 * connection fails, the reliable subscriptions
 * are released.
 *
 * Create a durable subscription.
 * Kill the client (to make the TCP connection fail
 * without a close)
 * Wait for <connecting timer> (see build.xml)
 * Start the client again (ClientTest16_2)
 * Create a durable subscriber.
 *
 */
public class ClientTest16 extends TestCase {

  public static void main(String[] args) {
    new ClientTest16().run();
  }

  public void run() {
    try {
      AdminModule.connect("localhost", 2560,
                          "root", "root", 20);

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
      ((org.objectweb.joram.client.jms.tcp.TcpConnectionFactory)cf).
	  getParameters().cnxPendingTimer = 500; // milliseconds
      ((org.objectweb.joram.client.jms.tcp.TcpConnectionFactory)cf).
      getParameters().connectingTimer = 2; //seconds

      Connection connection = cf.createConnection(
        "anonymous", "anonymous");
      
      Session recSession = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);

      TopicSubscriber consumer = 
        recSession.createDurableSubscriber(topic, "test_sub");

      connection.start();

      System.out.println("Exit");
      System.exit(0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    }
  }
}
