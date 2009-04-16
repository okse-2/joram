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
 * Initial developer(s):  ScalAgent D.T.
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

import joram.framework.TestCase;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Testing: basic server reconfiguration. Add one domain and one server then
 * remove them.
 */
public class ReconfTestBase extends TestCase {

  public static void main(String[] args) {
    new ReconfTestBase().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=2222,suspend=y" });

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      AdminModule.addDomain("D0", "fr.dyade.aaa.agent.PoolNetwork", 0, 17770);
      AdminModule.addServer(1, "localhost", "D0", 17771, "s1");

      User.create("anonymous", "anonymous", 0);
      
      startServer((short)1, "s1");

      checkQueue((short)1);

      AdminModule.stopServer(1);
      AdminModule.removeServer(1);
      AdminModule.removeDomain("D0");

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Stop server s0");
      stopAgentServer((short) 0);
      endTest();
    }
  }

  public static void startServer(short sid, String serverName) 
    throws Exception {
    String configXml = AdminModule.getConfiguration();

    File sdir = new File("./" + serverName);
    sdir.mkdir();
    File sconfig = new File(sdir, "a3servers.xml");
    FileOutputStream fos = new FileOutputStream(sconfig);
    PrintWriter pw = new PrintWriter(fos);
    pw.println(configXml);
    pw.flush();
    pw.close();
    fos.close();
    
    System.out.println("Start server " + serverName);
    startAgentServer(
      sid, sdir, 
      new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
  }

  public static void checkQueue(short sid) 
    throws Exception {
    System.out.println("Create queue on site " + sid);
    Queue queue = Queue.create(sid);
    queue.setFreeReading();
    queue.setFreeWriting();
    
    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

    Connection connection = cf.createConnection("anonymous", "anonymous");
    connection.start();

    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
    MessageProducer producer = session.createProducer(queue);
    TextMessage msg = session.createTextMessage("testcheck");
    
    System.out.println("send msg");
    producer.send(msg);
    
    MessageConsumer consumer = session.createConsumer(queue);
    
    System.out.println("receive msg");
    msg = (TextMessage)consumer.receive();
    assertEquals("testcheck",msg.getText());
    connection.close();
  }
}
