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
 * Initial developer(s): Feliot David (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.dursub;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicSubscriber;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * see DurTest2.
 *
 */

public class DurTest2_2 extends TestCase {

  public static void main(String[] args) {
    new DurTest2_2().run();
  }

  public void run() {
    try {
      //System.out.println("AdminModule connect");
      AdminModule.connect("localhost", 2560, "root", "root", 60);

      //System.out.println("Create topic");
      Topic topic = Topic.create(0, "topic");
      topic.setFreeReading();
      topic.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      Connection connection = cf.createConnection("anonymous", "anonymous");

      Session sendSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = sendSession.createProducer(topic);

      Session recSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      TopicSubscriber consumer = recSession.createDurableSubscriber(topic, "test_sub");

      connection.start();

      //System.out.println("Consumer: receive msg2");
      TextMessage msg2 = (TextMessage)consumer.receive();
      //System.out.println("Consumer: msg = " + msg2);

      //System.out.println("Session close");
      recSession.close();
      sendSession.close();

      //System.out.println("Connection close");
      connection.close();
      AdminModule.disconnect();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServerExt((short)0);
      endTest();     
    }
  }
}

