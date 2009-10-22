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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Tests a connection failure. The connection should
 * close itself.
 */
public class ClientTest4 extends TestCase {

  public static void main(String[] args) {
    new ClientTest4().run();
  }

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

      org.objectweb.joram.client.jms.Queue queue = 
        org.objectweb.joram.client.jms.Queue.create(0);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = 
        org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
          "localhost", 2560);
            
      Connection connection = cf.createConnection(
        "anonymous", "anonymous");

      Session session = connection.createSession(
        false,
        Session.AUTO_ACKNOWLEDGE);
      
      MessageConsumer consumer = session.createConsumer(queue);
      
      connection.start();
            
      stopAgentServer((short)0);
      
      try {
        Message msg = consumer.receive();
        System.out.println("msg = " + msg);
      } catch (JMSException exc) {
        System.out.println("OK -> " + exc);
	assertTrue(exc instanceof javax.jms.IllegalStateException);
        connection.close();
      }
    } catch (Throwable exc) {
      exc.printStackTrace();
      stopAgentServer((short)0);
      error(exc);
    } finally {
      endTest();     
    }
  }
}
