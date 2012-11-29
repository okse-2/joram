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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.MessageErrorConstants;

import fr.dyade.aaa.agent.AgentServer;

class ExcList20 implements ExceptionListener {
  String name = null;
  int nbexc;

  ExcList20(String name) {
    this.name = name;
    nbexc = 0;
  }

  public void onException(JMSException exc) {
    nbexc += 1;
    System.err.println(name + ": " + exc.getMessage());
    //exc.printStackTrace();
  }
}

class MsgList20B implements MessageListener {
  public void onMessage(Message msg) {
    try {
      Test20.assertEquals(1, msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      Test20.assertEquals(MessageErrorConstants.EXPIRED, msg.getIntProperty("JMS_JORAM_ERRORCODE_1"));
    } catch(Exception exc) {
      exc.printStackTrace();
    }
  }
}

/**
 * check message send to DMQ when ttl expire
 *
 */
public class Test20 extends BaseTest{
  static Connection cnx_a;
  static Connection cnx_b;

  static Session sess_a, sess_b;

  static ExcList20 exclst_a, exclst_b;
  static MsgList20B msglst_b;

  public static void main (String args[]) throws Exception {
    new Test20().run();
  }
  public void run(){
    try{
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000L);
      short sid = Integer.getInteger("sid", 0).shortValue();

      AdminModule.connect("localhost", 16010, "root", "root", 60);

      User user = User.create("anonymous", "anonymous");
      Queue queue = Queue.create(0);
      DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
      dmq.setFreeReading();
      AdminModule.setDefaultDMQ(0, dmq);

      queue.setFreeReading();
      queue.setFreeWriting();

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      AdminModule.disconnect();

      cnx_a = cf.createConnection();
      cnx_a.setExceptionListener(new ExcList20("ClientA"));
      sess_a = cnx_a.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_a = sess_a.createConsumer(queue);
      MessageProducer prod_a = sess_a.createProducer(queue);
      prod_a.setTimeToLive(1000L);
      cnx_a.start();

      Connection cnx_b = cf.createConnection();
      cnx_b.setExceptionListener(new ExcList20("ClientB"));
      sess_b = cnx_b.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons_b = sess_b.createConsumer(dmq);
      cons_b.setMessageListener(new MsgList20B());
      cnx_b.start();

      Message msg = sess_a.createMessage();
      msg.setIntProperty("Index", 0);
      prod_a.send(msg);

      msg = sess_a.createMessage();
      msg.setIntProperty("Index", 1);
      prod_a.send(msg);

      msg = sess_a.createMessage();
      msg.setIntProperty("Index", 2);
      prod_a.send(msg);

      Thread.sleep(2000L);

      msg = sess_a.createMessage();
      msg.setIntProperty("Index", 3);
      prod_a.send(msg);

      msg = cons_a.receive();
      int index = msg.getIntProperty("Index");
      //System.out.println("receives msg#" + index);

      Thread.sleep(1000L);

      cnx_a.close();
      cnx_b.close();
    } catch(Throwable exc){
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}
