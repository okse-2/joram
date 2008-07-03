/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import joram.framework.BaseTestCase;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class ExcList21 implements ExceptionListener {
  String name = null;
  int nbexc;

  ExcList21(String name) {
    this.name = name;
    nbexc = 0;
  }

  public void onException(JMSException exc) {
    nbexc += 1;
    System.err.println(name + ": " + exc.getMessage());
    //exc.printStackTrace();
  }
}

class MsgList21B implements MessageListener {
  public void onMessage(Message msg) {
    try {
      int index = msg.getIntProperty("Index");

      if (index == 0) {
        BaseTestCase.assertTrue(msg.getBooleanProperty("JMS_JORAM_UNDELIVERABLE"));
        System.out.println("msg#" + index + ", Undeliverable message: "
            + msg.getIntProperty("JMSXDeliveryCount"));
      } else if (index == 1) {
        BaseTestCase.assertTrue(msg.getBooleanProperty("JMS_JORAM_NOTWRITABLE"));
        System.out.println("msg#" + index + ", Non writable destination.");
      } else if (index == 2) {
        BaseTestCase.assertTrue(msg.getBooleanProperty("JMS_JORAM_DELETEDDEST"));
        System.out.println("msg#" + index + ", Destination does not exist.");
      } else {
        BaseTestCase.assertTrue(false);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
      BaseTestCase.error(exc);
    }
  }
}

/**
 *  check message send to DMQ when :
 *    -send to a queue with no write permission
 *    -send to a deleted queue
 */
public class Test21 extends BaseTest{
    static Connection cnx_a;
    static Connection cnx_b;

    static Session sess_a, sess_b;

    static ExcList21 exclst_a, exclst_b;
    static MsgList21B msglst_b;

    public static void main (String args[]) throws Exception {
	new Test21().run();
    }
    public void run(){
	try{
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);
	    short sid = Integer.getInteger("sid", 0).shortValue();

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    User user = User.create("anonymous", "anonymous");
	    Queue queue1 = Queue.create(0);
	    Queue queue2 = Queue.create(0);
	    Queue queue3 = Queue.create(0);

	    DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
	    dmq.setFreeReading();
	    AdminModule.setDefaultDMQ(0, dmq);

	    queue1.setFreeReading();
	    queue1.setFreeWriting();
	    queue1.setThreshold(2);

	    queue3.delete();

	    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
	    AdminModule.disconnect();

	    cnx_a = cf.createConnection();
	    cnx_a.setExceptionListener(new ExcList21("ClientA"));
	    sess_a = cnx_a.createSession(true, Session.CLIENT_ACKNOWLEDGE);
	    MessageConsumer cons_a = sess_a.createConsumer(queue1);
	    MessageProducer prod_a = sess_a.createProducer(null);
	    cnx_a.start();

	    Connection cnx_b = cf.createConnection();
	    cnx_b.setExceptionListener(new ExcList21("ClientB"));
	    sess_b = cnx_b.createSession(false, Session.CLIENT_ACKNOWLEDGE);
	    MessageConsumer cons_b = sess_b.createConsumer(dmq);
	    cons_b.setMessageListener(new MsgList21B());
	    cnx_b.start();

	    Message msg = sess_a.createMessage();
	    msg.setIntProperty("Index", 0);
	    prod_a.send(queue1, msg);
	    sess_a.commit();

	    msg = cons_a.receive();
	    int index = msg.getIntProperty("Index");
	    sess_a.rollback();
	    System.out.println("rollback msg#" + index);

	    msg = cons_a.receive();
	    index = msg.getIntProperty("Index");
	    sess_a.rollback();
	    System.out.println("rollback msg#" + index);

	    Thread.sleep(500L);

  
	    msg = sess_a.createMessage();
	    msg.setIntProperty("Index", 1);
	    prod_a.send(queue2, msg);
	    try{
	    sess_a.commit();
	    }catch(JMSException j){}

	    Thread.sleep(500L);
	   
   
	    msg = sess_a.createMessage();
	    msg.setIntProperty("Index", 2);
	    prod_a.send(queue3, msg);
	    try{
	    sess_a.commit();
	    }catch(JMSException jj){}

	    cnx_a.close();
	    cnx_b.close();

	    Thread.sleep(1000L);
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
