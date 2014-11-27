/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.perfs;


import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * 
 */
public class AsyncTest extends TestCase {

  public static final int LOOP_NB = 10000;

  public static void main(String[] args) {
    new AsyncTest().run();
  }

  private Destination dest;

  private ConnectionFactory cf;

  private ConnectionFactory asyncSendCf;

  private Connection connection;

  public void run() {
    try {
      writeIntoFile("==================== start test =====================");
      startAgentServer((short) 0, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });

      startAgentServer((short) 1, new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });
      Thread.sleep(2000);

      cf = TcpConnectionFactory.create("localhost", 2560);

      AdminModule.connect(cf);

      User.create("anonymous", "anonymous", 0);

      Topic localTopic = Topic.create(0);
      localTopic.setFreeReading();
      localTopic.setFreeWriting();

      Topic remoteTopic = Topic.create(1);
      remoteTopic.setFreeReading();
      remoteTopic.setFreeWriting();

      asyncSendCf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) asyncSendCf).getParameters().asyncSend = true;

      InitialContext ictx = new InitialContext();
      ictx.bind("asyncSendCf", asyncSendCf);

      // test the JNDI storage
      asyncSendCf = (ConnectionFactory)ictx.lookup("asyncSendCf");

      dest = localTopic;

      test();

      dest = remoteTopic;

      test();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      stopAgentServer((short) 1);
      endTest();
    }
  }

  private void test() throws Exception { 
    connection = cf.createConnection("anonymous", "anonymous");

    connection.start();

    // Check that Session.setAsyncSend() works
    // System.out.println("-- Async --");
    writeIntoFile("-- Async --");
    send(true);
    transactedSend(true);

    // System.out.println("-- Sync  --");
    writeIntoFile("-- Sync  --");
    send(false);
    transactedSend(false);

    connection.close();

    connection = asyncSendCf.createConnection("anonymous", "anonymous");

    connection.start();

    // Check that cf.parameters.asyncSend works
    // send(false) because it is set in the cf properties
    // System.out.println("-- Async --");
    writeIntoFile("-- Async --");
    send(false);
    transactedSend(false);

    connection.close();
  }

  private void send(boolean asyncSend) throws Exception {
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    if (asyncSend) {
      ((org.objectweb.joram.client.jms.Session)session).setAsyncSend(asyncSend);
    }

    //System.out.println("Create a producer");
    MessageProducer producer = session.createProducer(dest);

    long start = System.currentTimeMillis();

    for (int i = 0; i < LOOP_NB; i++) {
      producer.send(session.createTextMessage("msg#"));
    }

    long end = System.currentTimeMillis();

    session.close();

    long res = end - start;
    //    System.out.println("Sent " + LOOP_NB + " messages: " + res);
    writeIntoFile("| Sent " + LOOP_NB + " messages: " + res);
  }

  private void transactedSend(boolean asyncSend) throws Exception {
    Session session = connection.createSession(true, -1);
    if (asyncSend) {
      ((org.objectweb.joram.client.jms.Session)session).setAsyncSend(asyncSend);
    }

    //System.out.println("Create a producer");
    MessageProducer producer = session.createProducer(dest);

    long start = System.currentTimeMillis();

    for (int i = 0; i < LOOP_NB; i++) {
      producer.send(session.createTextMessage("msg #"));
      if (i%2 == 0) session.commit();
    }
    session.commit();

    long end = System.currentTimeMillis();

    session.close();

    long res = end - start;
    //System.out.println("Transacted sent " + LOOP_NB + " messages: " + res);
    writeIntoFile("| Transacted sent " + LOOP_NB + " messages: " + res);
  }

}
 
