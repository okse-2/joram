/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.BaseTestCase;

class ExcList19 implements ExceptionListener {
  String name = null;
  int nbexc;

  ExcList19(String name) {
    this.name = name;
    nbexc = 0;
  }

  public void onException(JMSException exc) {
    nbexc += 1;
    System.err.println(name + ": " + exc.getMessage());
  }
}

class MsgList19A implements MessageListener {
  public void onMessage(Message msg) {
    try {
      Destination dest = msg.getJMSReplyTo();
      MessageProducer publisher = Test19.sess_a.createProducer(dest);

      Message msg2 = Test19.sess_a.createMessage();
      msg2.setBooleanProperty("SynchroEnd", false);
      msg2.setIntProperty("Index", 0);
      publisher.send(msg2);
      msg2 = Test19.sess_a.createMessage();
      msg2.setBooleanProperty("SynchroEnd", false);
      msg2.setIntProperty("Index", 1);
      publisher.send(msg2);
      msg2 = Test19.sess_a.createMessage();
      msg2.setBooleanProperty("SynchroEnd", false);
      msg2.setIntProperty("Index", 2);
      publisher.send(msg2);
      msg2 = Test19.sess_a.createMessage();
      msg2.setBooleanProperty("SynchroEnd", true);
      msg2.setIntProperty("Index", 3);
      publisher.send(msg2);
    } catch(Exception exc) {
      exc.printStackTrace();
    }
  }
}

class MsgList19B implements MessageListener {
  public void onMessage(Message msg) {
    Exception excp = null;
    boolean end = false;
    try {
      end = msg.getBooleanProperty("SynchroEnd");
      int index = msg.getIntProperty("Index");
      System.out.println("receives " + index + ", " + end);
      if (end)
        Test19.tempTopic.delete();
    } catch (Exception exc) {
      excp = exc;
    }
    if (end)
      BaseTestCase.assertTrue(excp instanceof javax.jms.JMSException);
  }
}

/**
 * test delete temptopic on onMessage()
 */
public class Test19 extends BaseTest {
  static Connection cnx_a;
  static Connection cnx_b;

  static Session sess_a, sess_b;

  static ExcList19 exclst_a, exclst_b;
  static MsgList19A msglst_a;
  static MsgList19B msglst_b;

  static TemporaryTopic tempTopic;

  public static void main(String args[]) throws Exception {
    new Test19().run();
  }

  public void run() {
    try {
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000L);

      AdminModule.connect("localhost", 16010, "root", "root", 60);

      User.create("anonymous", "anonymous");
      Topic synchro = Topic.create(0);
      synchro.setFreeReading();
      synchro.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      AdminModule.disconnect();

      cnx_a = cf.createConnection();
      ExcList19 excListenerA = new ExcList19("ClientA");
      cnx_a.setExceptionListener(excListenerA);
      sess_a = cnx_a.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_a = sess_a.createConsumer(synchro);
      cons_a.setMessageListener(new MsgList19A());
      cnx_a.start();

      Connection cnx_b = cf.createConnection();
      ExcList19 excListenerB = new ExcList19("ClientB");
      cnx_b.setExceptionListener(excListenerB);
      sess_b = cnx_b.createSession(false, Session.AUTO_ACKNOWLEDGE);

      tempTopic = sess_b.createTemporaryTopic();

      MessageConsumer cons_b = sess_b.createConsumer(tempTopic);
      cons_b.setMessageListener(new MsgList19B());

      Session sess = cnx_b.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer pub = sess.createProducer(synchro);

      cnx_b.start();

      Message msg = sess.createMessage();
      msg.setJMSReplyTo(tempTopic);

      pub.send(msg);

      Thread.sleep(10000L);
      
      assertEquals(0, excListenerA.nbexc);
      assertEquals(0, excListenerB.nbexc);
      cnx_a.close();
      cnx_b.close();

      Thread.sleep(5000L);
      assertEquals(1, excListenerA.nbexc);
      assertEquals(1, excListenerB.nbexc);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}
