/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Checks that a message listener can send messages.
 */
public class ClientTest18 extends framework.TestCase{
    static int NbRound = 100;
    static int NbMsgPerRound = 5;

    static Destination dest = null;
    static ConnectionFactory cf = null;

    static void startServer() throws Exception {
	AgentServer.init((short) 0, "./s0", null);
	AgentServer.start();

	Thread.sleep(1000L);
    }

    public static void main (String args[]) throws Exception {
	new ClientTest18().run();
    }
    public void run(){
	try{
	    startServer();

	    org.objectweb.joram.client.jms.admin.AdminModule.collocatedConnect(
									       "root", "root");
    
	    dest = org.objectweb.joram.client.jms.Queue.create(0);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    User user = User.create("anonymous", "anonymous", 0);
	    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

	    ConnectionFactory cf = 
		new org.objectweb.joram.client.jms.local.LocalConnectionFactory();
	    Connection cnx = cf.createConnection();
    
	    Receiver7 receiver = new Receiver7(cnx, dest);
	    Sender7 sender = new Sender7(cnx, dest, NbRound, NbMsgPerRound);

	    cnx.start();

	    new Thread(sender).start();
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}
    }
}

class Sender7 implements Runnable {
  Connection cnx;
  Destination dest;
  int NbRound;
  int NbMsgPerRound;

  Session sess1, sess2;
  MessageProducer producer;
  
  TemporaryTopic topic;
  MessageConsumer cons;
  MsgListener listener;

  public Sender7(Connection cnx,
                Destination dest,
                int NbRound,
                int NbMsgPerRound) throws Exception {
    this.cnx = cnx;
    this.dest = dest;
    this.NbRound = NbRound;
    this.NbMsgPerRound = NbMsgPerRound;

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
      for (int i=0; i<NbRound; i++) {
        for (int j=0; j<NbMsgPerRound; j++) {
          Message msg = sess1.createMessage();
          msg.setIntProperty("index", NbMsgPerRound-j-1);
          msg.setJMSReplyTo(topic);
          producer.send(msg);
        }
        listener.fxCtrl(i);
      }
      
      listener.fxCtrl(NbRound);
    } catch (Exception exc) {
      exc.printStackTrace();
      System.exit(-1);
    }finally{
	AgentServer.stop();
	ClientTest18.endTest();
    }
  }

  /**
   * Implements the <code>javax.jms.MessageListener</code> interface.
   */
  static class MsgListener implements MessageListener {
    int count = 0;

    public synchronized void fxCtrl(int round) {
	//System.out.println("fxCtrl(" + round + ")");
      while (round > count) {
        try {
	    //System.out.println("wait() " + round + " / " + count);
          wait();
        } catch (InterruptedException exc) {
        }
      }
    }

    public synchronized void onMessage(Message msg) {
      try {
        count ++;
        //System.out.println("receive reply -> " + count);
        notify();
      } catch (Throwable exc) {
        exc.printStackTrace();
      }
    }
  }
}

class Receiver7 implements MessageListener {
  Connection cnx;
  Destination dest;

  Session sess;
  MessageConsumer cons;
  MessageProducer prod;

  public Receiver7(Connection cnx, Destination dest) throws Exception {
    this.cnx = cnx;
    this.dest = dest;

    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cons = sess.createConsumer(dest);
    prod = sess.createProducer(null);

    cons.setMessageListener(this);
  }

  public synchronized void onMessage(Message msg) {
    try {
      int index = msg.getIntProperty("index");

      if (index == 0) {
        // sends a flow-control message to sender
        javax.jms.Destination sender = msg.getJMSReplyTo();
        Message fx = sess.createMessage();
        //System.out.println("reply " + sender);
	ClientTest18.assertTrue(sender.toString().startsWith("TempTopic:"));
        prod.send(sender, fx);
      }
      //System.out.println("receive " + index);
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
