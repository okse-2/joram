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
 * Contributor(s): 
 */
package joram.local;

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;


/**
 * Test the commit operation provided by Session.
 * Especially tests the local replies mechanism.
 * 
 * @author feliot
 *
 *
 */
public class CommitTest extends TestCase {

  public static void main(String[] args) {
    new CommitTest().run();
  }

  private void startServers() throws Exception {
    System.out.println("start server 0");

    startAgentServer(
                     (short)0, (File)null, 
                     new String[]{
                     "-DTransaction=fr.dyade.aaa.util.NTransaction"});

    System.out.println("start server 1");

    startAgentServer(
                     (short)1, (File)null, 
                     new String[]{
                     "-DTransaction=fr.dyade.aaa.util.NTransaction"});

    Thread.sleep(2000);
  }

  public void run() {
    try {
      startServers();

      AdminModule.connect("localhost", 2560,
                          "root", "root", 60);

      User user = User.create("anonymous", "anonymous", 0);

      Queue localQueue = Queue.create(0);
      localQueue.setFreeReading();
      localQueue.setFreeWriting();

      org.objectweb.joram.client.jms.Topic localTopic = 
        org.objectweb.joram.client.jms.Topic.create(0);
      localTopic.setFreeReading();
      localTopic.setFreeWriting();

      Queue remoteQueue = Queue.create(1);
      remoteQueue.setFreeReading();
      remoteQueue.setFreeWriting();

      org.objectweb.joram.client.jms.Topic remoteTopic = 
        org.objectweb.joram.client.jms.Topic.create(1);
      remoteTopic.setFreeReading();
      remoteTopic.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      Connection c = cf.createConnection();
      Session s = c.createSession(true, 0);
      MessageProducer lqp = s.createProducer(localQueue);
      MessageConsumer lqc = s.createConsumer(localQueue);
      MessageProducer ltp = s.createProducer(localTopic);
      MessageConsumer ltc = s.createDurableSubscriber(localTopic, "ltc");

      MessageProducer rqp = s.createProducer(remoteQueue);
      MessageConsumer rqc = s.createConsumer(remoteQueue);
      MessageProducer rtp = s.createProducer(remoteTopic);
      MessageConsumer rtc = s.createDurableSubscriber(remoteTopic, "rtc");

      System.out.println("Start test");
      c.start();

      // Producing towards remote destinations: no local replies
      rqp.send(s.createTextMessage("rq1"));
      rtp.send(s.createTextMessage("rt1"));
      System.out.println("Commit 1");
      s.commit();

      // Consuming from topics: no local replies
      TextMessage tm = (TextMessage)rtc.receive();
      System.out.println("Commit 2");
      s.commit();

      // Consuming from remote queues: no local replies
      tm = (TextMessage)rqc.receive();
      System.out.println("Commit 3");
      s.commit();

      // Producing towards local topic: local reply
      ltp.send(s.createTextMessage("lt1"));
      System.out.println("Commit 4");
      s.commit();

      // Consuming from local topic: no local reply
      tm = (TextMessage)ltc.receive();
      System.out.println("Commit 5");
      s.commit();

      // Producing towards local queue: local reply
      ltp.send(s.createTextMessage("lq1"));
      System.out.println("Commit 6");
      s.commit();

      // Consuming from local queue: no local reply (from ack)
      tm = (TextMessage)ltc.receive();
      System.out.println("Commit 7");
      s.commit();

      // Mixing everything
      rqp.send(s.createTextMessage("rq2"));
      rtp.send(s.createTextMessage("rt2"));
      ltp.send(s.createTextMessage("lt2"));
      lqp.send(s.createTextMessage("lq2"));
      System.out.println("Commit 8");
      s.commit();

      System.out.println(" -> receive rt2");
      tm = (TextMessage)rtc.receive();
      System.out.println(" -> receive rq2");
      tm = (TextMessage)rqc.receive();
      System.out.println(" -> receive lt2");
      tm = (TextMessage)ltc.receive();
      System.out.println(" -> receive lq2");
      tm = (TextMessage)lqc.receive();
      rqp.send(s.createTextMessage("rq3"));
      rtp.send(s.createTextMessage("rt3"));
      ltp.send(s.createTextMessage("lt3"));
      lqp.send(s.createTextMessage("lq3"));
      System.out.println("Commit 9");
      s.commit();

      tm = (TextMessage)rtc.receive();
      tm = (TextMessage)rqc.receive();
      tm = (TextMessage)ltc.receive();
      tm = (TextMessage)lqc.receive();
      System.out.println("Commit 10");
      s.commit();

      c.close();

      c = cf.createConnection();
      s = c.createSession(false, Session.AUTO_ACKNOWLEDGE);

      lqc = s.createConsumer(localQueue);
      ltc = s.createDurableSubscriber(localTopic, "ltc");
      rqc = s.createConsumer(remoteQueue);
      rtc = s.createDurableSubscriber(remoteTopic, "rtc");
      c.start();

      tm = (TextMessage)lqc.receiveNoWait();
      assertTrue("local queue not empty: " + tm, tm == null);

      tm = (TextMessage)ltc.receiveNoWait();
      assertTrue("local sub not empty: " + tm, tm == null);

      tm = (TextMessage)rqc.receiveNoWait();
      assertTrue("remote queue not empty: " + tm, tm == null);

      tm = (TextMessage)rtc.receiveNoWait();
      assertTrue("remote sub not empty: " + tm, tm == null);

      c.close();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      stopAgentServer((short)1);

      endTest();     
    }
  }

}
