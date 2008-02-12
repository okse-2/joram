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
 * Initial developer(s):  ScalAgent D.T.
 * Contributor(s): 
 */
package joram.reconf;

import joram.framework.TestCase;

import java.io.*;
import java.util.*;
import javax.jms.*;

import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * Testing: server reconfiguration
 */
public class ReconfTest3 extends TestCase {

  public static void main(String[] args) {
    new ReconfTest3().run();
  }

  public void run() {
    try {
      startAgentServer(
        (short)0, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      startAgentServer(
        (short)1, (File)null, 
        new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});

      System.out.println("waiting");
      Thread.sleep(1000L);

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = 
        org.objectweb.joram.client.jms.admin.User.create(
          "anonymous", "anonymous", 0);

      checkQueue((short)1);

      System.out.println("Add server s2");
      AdminModule.addServer(2, "localhost", "D0", 17772, "s2");
      
      startServer((short)2, "s2");

      checkQueue((short)2);
      checkQueue((short)1);

      // First stop the server because it must be reachable
      // in order to be stopped.
//       System.out.println("Stop server s2");
      AdminModule.stopServer(2);
//       System.out.println("Server s2 stopped");

      // Then clean the configuration: 
      // the server is not reachable
      // anymore.
      System.out.println("Remove server s2");
      AdminModule.removeServer(2);
//       System.out.println("Server s2 removed");

      checkQueue((short)1);

      System.out.println("Add server s3");
      AdminModule.addServer(3, "localhost", "D0", 17773, "s3");
      
      startServer((short)3, "s3");

      checkQueue((short)3);
      checkQueue((short)1);

      // First stop the server because it must be reachable
      // in order to be stopped.
//       System.out.println("Stop server s3");
      AdminModule.stopServer(3);
//       System.out.println("Server s3 stopped");

      // Then clean the configuration: 
      // the server is not reachable
      // anymore.
      System.out.println("Remove server s3");
      AdminModule.removeServer(3);
//       System.out.println("Server s3 removed");

      checkQueue((short)1);

//       System.out.println("Stop server s1");
      AdminModule.stopServer(1);
//       System.out.println("Server s1 stopped");

      System.out.println("Remove server s1");
      AdminModule.removeServer(1);
//       System.out.println("Server s1 removed");

      System.out.println("Remove domain D0");
      AdminModule.removeDomain("D0");
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Stop server s0");
      stopAgentServer((short)0);
      endTest();     
    }
  }

  public static void startServer(short sid, String serverName) throws Exception {
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
    
//     System.out.println("Start server " + serverName);
    startAgentServer(
      sid, sdir, 
      new String[]{"-DTransaction=fr.dyade.aaa.util.NullTransaction"});
  }

  public static void checkQueue(short sid) throws Exception {
//     System.out.println("Create queue on site " + sid);
    org.objectweb.joram.client.jms.Queue queue = 
      org.objectweb.joram.client.jms.Queue.create(sid);
    queue.setFreeReading();
    queue.setFreeWriting();
    
    ConnectionFactory cf = 
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create(
        "localhost", 2560);
    
    Connection connection = cf.createConnection(
      "anonymous", "anonymous");
    connection.start();

    Session session = connection.createSession(
      false,
      Session.AUTO_ACKNOWLEDGE);
    
    MessageProducer producer = session.createProducer(queue);
    TextMessage msg = session.createTextMessage("testcheck");
    
//     System.out.println("send msg");
    producer.send(msg);
    
    MessageConsumer consumer = session.createConsumer(queue);
    
//     System.out.println("receive msg");
    msg = (TextMessage)consumer.receive();
    assertEquals("testcheck",msg.getText());
    connection.close();
  }
}
