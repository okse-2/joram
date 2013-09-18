/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2013 ScalAgent Distributed Technologies
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
package joram.perfs;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Configuration;

/**
 * Simple test for measure travel time. Several different parameter
 * edit build to modifie parameter 
 */
public class Test26 extends BaseTest {
    static int NbReceiver = 4;

    static int NbRound = 100;
    static int NbMsgPerRound = 100;
    static int MsgSize = 100;

    static Destination dest = null;
    static ConnectionFactory cf = null;

    static boolean MsgTransient = true;
    static boolean SubDurable = false;
    static boolean transacted = false;

    public static void main (String args[]) throws Exception {
	new Test26().run();
    }
    public void run(){
	try{
	    writeIntoFile("======================= start test ========================");

	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    
	    String baseclass = "joram.perfs.ColocatedBaseTest";
	    baseclass = System.getProperty("BaseClass", baseclass);

	    String destclass = System.getProperty("Destination",
						  "org.objectweb.joram.client.jms.Queue");

	    NbReceiver = Integer.getInteger("NbReceiver", NbReceiver).intValue();

	    NbRound = Integer.getInteger("NbRound", NbRound).intValue();
	    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
	    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();

	    MsgTransient = Boolean.getBoolean("MsgTransient");
	    SubDurable = Boolean.getBoolean("SubDurable");
	    transacted = Boolean.getBoolean("Transacted");

	    AdminConnect(baseclass);

	    dest = createDestination(destclass);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    User user = User.create("anonymous", "anonymous", 0);

	    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();

	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
        writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
	    writeIntoFile("baseclass: " + baseclass +
			       ", Transacted=" + Configuration.getBoolean("Transacted"));
	    writeIntoFile("Message: transient=" + MsgTransient);
	    writeIntoFile("Subscriber: durable=" + SubDurable);
	    writeIntoFile("NbReceiver=" + NbReceiver);
	    writeIntoFile("NbRound=" + NbRound + 
			       ", NbMsgPerRound=" + NbMsgPerRound +
			       ", MsgSize=" + MsgSize);
	    writeIntoFile("----------------------------------------------------");

	    ConnectionFactory cf =  createConnectionFactory(baseclass);
	    Connection cnx1 = cf.createConnection();
	    cnx1.setClientID("Test26");
	    Connection cnx2 = cf.createConnection();

	    Receiver26 receiver[] = new Receiver26[NbReceiver];
	    for (int i=0; i<NbReceiver; i++) {
		receiver[i] = new Receiver26(cnx1, dest);
		if (SubDurable)
		    receiver[i].durname = "durname#" + i;
		receiver[i].selector = "dest == " + i;
	    }

	    Session sess1 = cnx2.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess1.createProducer(dest);
    
	    Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    Topic temp = sess2.createTemporaryTopic();
	    MessageConsumer cons = sess2.createConsumer(temp);
	    SyncListener listener = new SyncListener();
	    cons.setMessageListener(listener);

	    cnx2.start();

	    long dt1 = 0L;
	    long dt2 = 0L;

	    long t1 = System.currentTimeMillis();
	    try {

		byte[] content = new byte[MsgSize];
		for (int i = 0; i< MsgSize; i++)
		    content[i] = (byte) (i & 0xFF);

		for (int i=0; i<NbRound; i++) {
		    long start = System.currentTimeMillis();
		    for (int j=0; j<NbMsgPerRound; j++) {
			BytesMessage msg = sess1.createBytesMessage();
			if (MsgTransient) {
			    msg.setJMSDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
			    producer.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
			}
			msg.writeBytes(content);
			msg.setLongProperty("time", System.currentTimeMillis());
			msg.setIntProperty("index", NbMsgPerRound-j-1);
			msg.setIntProperty("dest", NbMsgPerRound/NbReceiver);
			msg.setJMSReplyTo(temp);
			producer.send(msg);
			if (transacted && ((j%10) == 9)) sess1.commit();
		    }
		    long end = System.currentTimeMillis();
		    dt1 += (end-start);
		    listener.fxCtrl(i);
		}

		listener.fxCtrl(NbRound);
	    } catch (Exception exc) {
		exc.printStackTrace();
		System.exit(-1);
	    }
	    long t2 = System.currentTimeMillis();

	    long dt3 = 0L; long dt4 = 0L;
	    for (int i=0; i<NbReceiver; i++) {
		dt3 += (receiver[i].last - receiver[i].start);
		dt4 += receiver[i].travel;
	    }

	    long NbMsg = NbMsgPerRound * NbRound;
	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("| sender dt(us)=" +  ((dt1 *1000L)/(NbMsg)));
	    writeIntoFile("| receiver dt(us)=" + ((dt3 *1000L)/(NbMsg)));
	    writeIntoFile("| Mean travel time (ms)=" + (dt4/(NbMsg)));
	    writeIntoFile("| Mean time = " +
			       ((t2-t1)*1000L) / (NbMsg) + "us per msg, " +
			       ((1000L * (NbMsg)) / (t2-t1)) + "msg/s");
	    writeIntoFile("----------------------------------------------------");

	    for (int i=0; i<NbReceiver; i++) {
		System.out.print("" + receiver[i].counter + ",");
	    }
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}

class SyncListener implements MessageListener {
  int count = 0;
  long last;

  public synchronized long fxCtrl(int round) {
    while (round > count) {
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
    return last;
  }

  public synchronized void onMessage(Message msg) {
    try {
      count ++;
      last = msg.getLongProperty("time");
      notify();
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}

class Receiver26 extends BaseTest implements MessageListener {
  Connection cnx;
  Destination dest;

  boolean transacted;

  String selector = null;
  String durname = null;

  Session sess;
  MessageConsumer cons;
  MessageProducer prod;

  Receiver26(Connection cnx, Destination dest) throws Exception {
    this.cnx = cnx;
    this.dest = dest;

    transacted = Boolean.getBoolean("Transacted");

    sess = cnx.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
    if ((durname != null) && (dest instanceof Topic))
      cons = sess.createDurableSubscriber((Topic) dest, durname,
                                          selector, false);
    else
      cons = sess.createConsumer(dest, selector);
    prod = sess.createProducer(null);

    cons.setMessageListener(this);
    cnx.start();
  }

  int counter = 0;
  long travel = 0L;

  long start = 0L;
  long last = 0L;

  public synchronized void onMessage(Message m) {
    try {
      BytesMessage msg = (BytesMessage) m;

      last = System.currentTimeMillis();
      if (counter == 0) start = last;

      int index = msg.getIntProperty("index");
      counter += 1;

      if (index == 0) {
        // sends a flow-control message to sender
        javax.jms.Destination sender = msg.getJMSReplyTo();
        Message fx = sess.createMessage();
        fx.setLongProperty("time", last);
        prod.send(sender, fx);
      }

      if (transacted && (((counter%10) == 9) || (index == 0)))
        sess.commit();

      travel += (last - msg.getLongProperty("time"));
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
