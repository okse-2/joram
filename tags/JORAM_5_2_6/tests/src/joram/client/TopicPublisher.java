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


import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Test memory leak
 */
public class TopicPublisher extends TestCase {

  public static int NB_MSG = 100000;

  public static void main(String[] args) {
    new TopicPublisher().run();
  }

  private static org.objectweb.joram.client.jms.Topic topic;
  
  private static Connection connection;

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

      MessageProducer prod = session.createProducer(topic);

      connection.start();

      int i = 0;
      while (i < NB_MSG) {
        i++;
        TextMessage tm = session.createTextMessage("test_" + i);
        tm.setIntProperty("counter", i);
	// System.out.println("send: msg#" + i);
        prod.send(tm);
      }

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
