/*
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package joram.distrib;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * Test distributed architecture. Use 2 servers. The producer is attached to
 * server 0. The consumer is attached to server 1. The topic is created on
 * server 0.
 */
public class Distrib_2serv_T_BadNetwork extends TestCase {
  
  static int NbRound = 100;
  static int NbMsgPerRound = 100;
  static int MsgSize = 100;

  static Topic dest = null;
  static ConnectionFactory cf0 = null;
  static ConnectionFactory cf1 = null;

  static void startServer() throws Exception {
    AgentServer.init((short) 0, "./s0", null);
    AgentServer.start();
    Thread.sleep(1000L);
  }

  public static void main(String args[]) {
    new Distrib_2serv_T_BadNetwork().run();
  }

  public void run() {
    try {
      NbRound = Integer.getInteger("NbRound", NbRound).intValue();
      NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
      MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

      startAgentServer((short) 1);
      startServer();

      AdminModule.collocatedConnect("root", "root");
      User.create("anonymous", "anonymous", 0);
      User.create("anonymous", "anonymous", 1);
      dest = Topic.create(0);
      dest.setFreeReading();
      dest.setFreeWriting();

      cf0 = LocalConnectionFactory.create();
      cf1 = TcpConnectionFactory.create("localhost", 2561);
      AdminModule.disconnect();

      Connection cnx0 = cf0.createConnection();
      Connection cnx1 = cf1.createConnection();

      new Receiver(cnx1, dest, NbMsgPerRound);

      Lock lock = new Lock(1);
      Sender sender = new Sender(cnx0, dest, NbRound, NbMsgPerRound, MsgSize, lock);

      cnx0.start();
      cnx1.start();

      new Thread(sender).start();
      lock.ended();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      framework.TestCase.stopAgentServer((short) 1);
      endTest();
    }
  }
  
  class Lock {
    int count;

    Lock(int count) {
      this.count = count;
    }

    synchronized void exit() {
      count -= 1;
      notify();
    }

    synchronized void ended() {
      while (count != 0) {
        try {
          wait();
        } catch (InterruptedException exc) {
        }
      }
    }
  }
  
  class Receiver implements MessageListener {
    Connection cnx;
    Destination dest;
    int NbMsgPerRound;

    Session sess;
    MessageConsumer cons;
    MessageProducer prod;

    public Receiver(Connection cnx, Destination dest, int NbMsgPerRound) throws Exception {
      this.cnx = cnx;
      this.dest = dest;
      this.NbMsgPerRound = NbMsgPerRound;

      next = this.NbMsgPerRound - 1;
      sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

      cons = sess.createConsumer(dest);
      prod = sess.createProducer(null);

      cons.setMessageListener(this);
    }

    int counter = 0;

    int next = -1;
    int errors = 0;

    public void onMessage(Message m) {
      try {
        BytesMessage msg = (BytesMessage) m;

        counter++;

        int index = msg.getIntProperty("index");

        if (index != next) {
          errors++;
          next = index;
        }
        next--;

        if (index == 0) {
          // sends a flow-control message to sender
          javax.jms.Destination sender = msg.getJMSReplyTo();
          Message fx = sess.createMessage();
          prod.send(sender, fx);
          //System.out.println("receive#1 end round #" + round + " - " + counter);
          next = this.NbMsgPerRound - 1;
        }

      } catch (Throwable exc) {
        exc.printStackTrace();
      }
    }
  }

  class Sender implements Runnable {
    Connection cnx;
    Destination dest;
    int NbRound;
    int NbMsgPerRound;
    int MsgSize;

    Session sess1, sess2;
    MessageProducer producer;

    TemporaryTopic topic;
    MessageConsumer cons;
    MsgListener listener;

    Lock lock;

    public Sender(Connection cnx, Destination dest, int NbRound, int NbMsgPerRound, int MsgSize, Lock lock)
        throws Exception {
      this.cnx = cnx;
      this.dest = dest;
      this.NbRound = NbRound;
      this.NbMsgPerRound = NbMsgPerRound;
      this.MsgSize = MsgSize;

      this.lock = lock;

      sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = sess1.createProducer(dest);

      sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      topic = sess2.createTemporaryTopic();
      cons = sess2.createConsumer(topic);
      listener = new MsgListener();
      cons.setMessageListener(listener);
    }

    public void run() {

      try {
        byte[] content = new byte[MsgSize];
        for (int i = 0; i < MsgSize; i++)
          content[i] = (byte) (i & 0xFF);

        for (int i = 0; i < NbRound; i++) {
          for (int j = 0; j < NbMsgPerRound; j++) {
            BytesMessage msg = sess1.createBytesMessage();
            msg.writeBytes(content);
            msg.setIntProperty("index", NbMsgPerRound - j - 1);
            msg.setJMSReplyTo(topic);
            producer.send(msg);
          }
          listener.fxCtrl(i);
        }

        listener.fxCtrl(NbRound);

      } catch (Exception exc) {
        exc.printStackTrace();
        System.exit(-1);
      }

      lock.exit();
    }

    /**
     * Implements the <code>javax.jms.MessageListener</code> interface.
     */
    class MsgListener implements MessageListener {
      int count = 0;

      public synchronized void fxCtrl(int round) {
        while (round > count) {
          try {
            wait();
          } catch (InterruptedException exc) {
          }
        }
      }

      public synchronized void onMessage(Message msg) {
        try {
          count++;
          notify();
        } catch (Throwable exc) {
          exc.printStackTrace();
        }
      }
    }
  }
}




