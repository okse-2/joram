/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Check that sending an empty ObjectMessage
 * leads to receiving an ObjectMessage (and not a simple Message)
 */
public class ClientTest24 extends TestCase {

  public static void main(String[] args) {
    new ClientTest24().run();
  }

  private Connection connection;

  private Destination dest;

  public void run() {
    try {
      startAgentServer((short) 0, (File) null,
          new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf);

      User.create("anonymous", "anonymous", 0);

      Queue queue = Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      dest = queue;

      connection = cf.createConnection("anonymous", "anonymous");
      connection.start();

      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      //System.out.println("Create a producer");
      MessageProducer producer = session.createProducer(dest);

      //System.out.println("Create a message listener");
      MessageConsumer consumer = session.createConsumer(dest);

      ObjectMessage objMsg = session.createObjectMessage();
      producer.send(objMsg);
      //System.out.println("objMsg = " + objMsg);
      
      Message msg = consumer.receive();
      
      assertTrue("Not ObjectMessage: msg=" + msg, msg instanceof ObjectMessage);

      //System.out.println("Concurrent stop of the connection");
      connection.stop();
      //System.out.println("Connection stopped");
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
