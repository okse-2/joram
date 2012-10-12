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
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Test connection closure diagnostic.
 */
public class ClientTest22 extends TestCase {

  public static void main(String[] args) {
    new ClientTest22().run();
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
      
      stopAgentServer((short)0);

      Thread.sleep(3000);

      // Check that the connection is closed.

      JMSException e = null;
      try {
        connection.getMetaData();
      } catch (JMSException exc) {
	  // exc.printStackTrace();
        e = exc;
      }
      assertTrue("Exception expected", e != null);
      assertTrue(e instanceof javax.jms.IllegalStateException);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      endTest();
    }
  }
}
