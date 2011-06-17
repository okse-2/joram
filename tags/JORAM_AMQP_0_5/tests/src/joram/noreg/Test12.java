/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): Freyssinet Andre (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.BaseTestCase;

class ExcList12 implements ExceptionListener {
  String name = null;
  int nbInvalidDestExc;
  int nbIllegalStateExc;

  ExcList12(String name) {
    this.name = name;
  }

  public void onException(JMSException exc) {
    if (exc instanceof InvalidDestinationException) {
      nbInvalidDestExc++;
    } else if (exc instanceof IllegalStateException) {
      nbIllegalStateExc++;
    } else {
      BaseTestCase.error(exc);
    }
  }
}

class MsgList12 implements MessageListener {
  int nbReceived;

  public synchronized void onMessage(Message msg) {
    nbReceived++;
  }
}

/**
 * Check exceptions on message.send() when queue has been deleted. Two different
 * behaviors are expected if the queue is local and synchronous or
 * remote/asynchronous.
 */
public class Test12 extends BaseTest {
  public static void main(String args[]) throws Exception {
    new Test12().run();
  }

  public void run() {
    short sid = 0;
    try {
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000L);
      sid = Integer.getInteger("sid", 0).shortValue();
      boolean asynchronous = Boolean.getBoolean("async");

      if (sid != 0) {
        framework.TestCase.startAgentServer(sid);
      }
      System.out.println((sid == 0) ? "local" : "remote");
      System.out.println("async: " + asynchronous);

      AdminModule.connect("localhost", 16010, "root", "root", 60);

      User.create("anonymous", "anonymous", 0);

      Queue queue = Queue.create(sid);
      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

      Connection cnx1 = cf.createConnection();
      ExcList12 receiverExcListener = new ExcList12("Receiver");
      cnx1.setExceptionListener(receiverExcListener);
      Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = sess1.createConsumer(queue);
      MsgList12 msgListener = new MsgList12();
      cons.setMessageListener(msgListener);
      cnx1.start();

      Connection cnx2 = cf.createConnection();
      ExcList12 senderExcListener = new ExcList12("Sender");
      cnx2.setExceptionListener(senderExcListener);
      Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      ((org.objectweb.joram.client.jms.Session) sess2).setAsyncSend(asynchronous);
      MessageProducer producer = sess2.createProducer(queue);
      cnx2.start();

      // Send 2 messages
      Message msg = sess2.createMessage();
      producer.send(msg);
      msg = sess2.createMessage();
      producer.send(msg);

      Thread.sleep(1000);
      assertEquals(2, msgListener.nbReceived);

      // Delete the topic
      queue.delete();

      // Receivers should have been notified of topic deletion.
      Thread.sleep(1000);
      assertEquals(1, receiverExcListener.nbInvalidDestExc);

      // Send one message
      msg = sess2.createMessage();
      Exception expectedException = null;
      try {
        ((org.objectweb.joram.client.jms.MessageProducer) producer).send(msg);
      } catch (Exception exc) {
        expectedException = exc;
      }
      // The exception must have been caught only if the topic was local.
      // If remote or asynchronous, the exception listener should receive the exception.
      Thread.sleep(1000);
      if (sid == 0 && !asynchronous) {
        assertTrue(expectedException instanceof InvalidDestinationException);
        assertEquals(0, senderExcListener.nbInvalidDestExc);
      } else {
        assertNull(expectedException);
        assertEquals(1, senderExcListener.nbInvalidDestExc);
      }

      Thread.sleep(1000L);

      // Send a second message
      expectedException = null;
      msg = sess2.createMessage();
      try {
        producer.send(msg);
      } catch (Exception exc) {
        expectedException = exc;
      }
      // The exception must have been caught only if the topic was local.
      // If remote or asynchronous, the exception listener should receive the exception.
      Thread.sleep(1000);
      if (sid == 0 && !asynchronous) {
        assertTrue(expectedException instanceof InvalidDestinationException);
        assertEquals(0, senderExcListener.nbInvalidDestExc);
      } else {
        assertNull(expectedException);
        assertEquals(2, senderExcListener.nbInvalidDestExc);
      }

      Thread.sleep(1000L);
      cnx1.close();
      cnx2.close();

      Thread.sleep(1000L);
      // The exception listeners should have been notified of the close
      assertEquals(1, senderExcListener.nbIllegalStateExc);
      assertEquals(1, receiverExcListener.nbIllegalStateExc);

      AdminModule.disconnect();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      if (sid != 0)
        framework.TestCase.stopAgentServer(sid);
      AgentServer.stop();
      endTest();
    }
  }
}
