/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */

package joram.reconf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Base class to check the reconfiguration methods.
 */
public class ReconfTestBase extends TestCase {
  /**
   * Creates a directory to deploy a new server and generates the corresponding
   * configuration file.
   * 
   * @param sid   the unique id of the server to deploy.
   * @param dir   the working directory of the new server.
   * @throws Exception
   */
  public static void deployAgentServer(short sid, String dir) throws Exception {
    String configXml = AdminModule.getConfiguration();

    File sdir = new File(dir);
    sdir.mkdir();
    
    File sconfig = new File(sdir, "a3servers.xml");
    
    FileOutputStream fos = new FileOutputStream(sconfig);
    PrintWriter pw = new PrintWriter(fos);
    pw.println(configXml);
    pw.flush();
    pw.close();
    fos.close();
  }

  /**
   * Check that the specified server is running. This method creates a queue on the
   * server then send and receive a message through this queue.
   * 
   * @param sid   the unique id of the server to check.
   * @throws Exception
   */
  public static void checkQueue(short sid) throws Exception {
    Queue queue = Queue.create(sid);
    queue.setFreeReading();
    queue.setFreeWriting();

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
    Connection connection = cf.createConnection("anonymous", "anonymous");
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = session.createProducer(queue);
    MessageConsumer consumer = session.createConsumer(queue);
    connection.start();

    TextMessage msg1 = session.createTextMessage("testcheck-" + System.currentTimeMillis());
    producer.send(msg1);

    TextMessage msg2 = (TextMessage) consumer.receive();
    assertEquals("check JMSMessageID", msg1.getJMSMessageID(), msg2.getJMSMessageID());
    assertEquals("check text", msg1.getText(), msg2.getText());
    
    connection.close();
  }

}
