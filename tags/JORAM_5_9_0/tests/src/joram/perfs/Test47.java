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
import javax.jms.TemporaryTopic;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.common.Configuration;

/**
 *
 */
public class Test47 extends BaseTest {
    static int NbRound = 100;
    static int NbMsgPerRound = 100;
    static int MsgSize = 100;

    static short sid;
    static Queue dest = null;
    static ConnectionFactory cf0 = null;
    static ConnectionFactory cf1 = null;

    static boolean MsgTransient = false;

    static boolean SubDurable = false;

    public static void main (String args[]) {
	new Test47().run();
    }
    public void run(){
	try{
	    NbRound = Integer.getInteger("NbRound", NbRound).intValue();
	    NbMsgPerRound = Integer.getInteger("NbMsgPerRound", NbMsgPerRound).intValue();
	    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
	    sid = Integer.getInteger("sid", 0).shortValue();

	    MsgTransient = Boolean.getBoolean("MsgTransient");
	    SubDurable = Boolean.getBoolean("SubDurable");
	    writeIntoFile("======================== start test 47 =====================");
	    framework.TestCase.startAgentServer((short) 1);
	    startServer();
	    Thread.sleep(500L);

	    AdminModule.collocatedConnect("root", "root");
	    User user0 = User.create("anonymous", "anonymous", 0);
	    User user1 = User.create("anonymous", "anonymous", 1);
	    dest = Queue.create(sid);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    cf0 = LocalConnectionFactory.create();
	    cf1 = TcpConnectionFactory.create("localhost", 16011);
	    AdminModule.disconnect();

	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
	    writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
	    writeIntoFile("Transacted=" + Configuration.getBoolean("Transacted"));
	    writeIntoFile("Message: transient=" + MsgTransient);
	    writeIntoFile("Subscriber: durable=" + SubDurable);
	    writeIntoFile("NbRound=" + NbRound + 
			       ", NbMsgPerRound=" + NbMsgPerRound +
			       ", MsgSize=" + MsgSize);
	    writeIntoFile("----------------------------------------------------");

	    Connection cnx0 = cf0.createConnection();
	    cnx0.setClientID("Test47");
	    Connection cnx1 = cf1.createConnection();
    
	    Receiver47 receiver = new Receiver47(cnx0, dest, NbMsgPerRound);

	    Lock47 lock = new Lock47(1);
	    Sender47 sender = new Sender47(cnx1, dest,
					   NbRound, NbMsgPerRound, MsgSize,
					   lock, MsgTransient);

	    cnx0.start();
	    cnx1.start();

	    long t1 = System.currentTimeMillis();
	    new Thread(sender).start();
	    lock.ended();
	    long t2 = System.currentTimeMillis();

	    long dt1 = sender.dt1;

	    long dt3 = 0L; long dt4 = 0L;
	    dt3 = receiver.last - receiver.start;
	    dt4 = receiver.travel;

	    long NbMsg = NbMsgPerRound * NbRound;
	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("| sender dt(us)=" +  ((dt1 *1000L)/(NbMsg)));
	    writeIntoFile("| receiver dt(us)=" + ((dt3 *1000L)/(NbMsg)));
	    writeIntoFile("| Mean travel time (ms)=" + (dt4/(NbMsg)));
	    writeIntoFile("| Mean time = " +
			       ((t2-t1)*1000L) / (NbMsg) + "us per msg, " +
			       ((1000L * (NbMsg)) / (t2-t1)) + "msg/s");
	    writeIntoFile("| Errors = " + receiver.errors);

	    writeIntoFile("----------------------------------------------------");

   

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    framework.TestCase.stopAgentServer((short) 1);
	    endTest();
	}
    }
}

class Lock47 {
    int count;

    Lock47(int count) {
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

class Receiver47 implements MessageListener {
    Connection cnx;
    Destination dest;
    int NbMsgPerRound;

    boolean transacted;
    boolean durable;
    boolean dupsOk;

    Session sess;
    MessageConsumer cons;
    MessageProducer prod;

    public Receiver47(Connection cnx,
		      Destination dest,
		      int NbMsgPerRound) throws Exception {
	this.cnx = cnx;
	this.dest = dest;
	this.NbMsgPerRound = NbMsgPerRound;

	next = this.NbMsgPerRound -1;

	transacted = Boolean.getBoolean("Transacted");
	durable = Boolean.getBoolean("SubDurable");
	dupsOk = Boolean.getBoolean("dupsOk");
    
	int sessionMode;
	if (dupsOk) {
	    sessionMode = Session.DUPS_OK_ACKNOWLEDGE;
	} else {
	    sessionMode = Session.AUTO_ACKNOWLEDGE;
	}
	sess = cnx.createSession(transacted, sessionMode);
    
	if (durable && dest instanceof Topic) {
	    cons = sess.createDurableSubscriber((Topic)dest, "dursub");
	} else {
	    cons = sess.createConsumer(dest);
	}
	prod = sess.createProducer(null);

	cons.setMessageListener(this);
    }

    int counter = 0;
    long travel = 0L;

    long start = 0L;
    long last = 0L;

    int next = -1;
    int errors = 0;

    public void onMessage(Message m) {
	try {
	    BytesMessage msg = (BytesMessage) m;

	    last = System.currentTimeMillis();
	    if (counter == 0) start = last;
	    counter += 1;

	    int index = msg.getIntProperty("index");
	    int round = msg.getIntProperty("round");
	    if (index != next) {
		errors += 1;
		//System.out.println("(" + round + ") " + new Date() + " receive " + index + " should be " + next);
		next = index;
	    }
	    next -= 1;

	    if (index == 0) {
		// sends a flow-control message to sender
		javax.jms.Destination sender = msg.getJMSReplyTo();
		Message fx = sess.createMessage();
		fx.setLongProperty("time", last);
		prod.send(sender, fx);
		//System.out.println("receive#1 end round #" + round + " - " + counter);
		next = this.NbMsgPerRound -1;
	    }

	    if (transacted && (((counter%10) == 9) || (index == 0)))
		sess.commit();

	    travel += (last - msg.getLongProperty("time"));
	} catch (Throwable exc) {
	    exc.printStackTrace();
	}
    }
}

class Sender47 extends BaseTest implements Runnable {
    Connection cnx;
    Destination dest;
    int NbRound;
    int NbMsgPerRound;
    int MsgSize;

    boolean transacted;

    Session sess1, sess2;
    MessageProducer producer;
  
    TemporaryTopic topic;
    MessageConsumer cons;
    MsgListener listener;

    Lock47 lock;

    boolean MsgTransient;

    public Sender47(Connection cnx,
		    Destination dest,
		    int NbRound,
		    int NbMsgPerRound,
		    int MsgSize,
		    Lock47 lock,
		    boolean MsgTransient) throws Exception {
	this.cnx = cnx;
	this.dest = dest;
	this.NbRound = NbRound;
	this.NbMsgPerRound = NbMsgPerRound;
	this.MsgSize = MsgSize;

	this.lock = lock;

	this.MsgTransient = MsgTransient;

	transacted = Boolean.getBoolean("Transacted");

	sess1 = cnx.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
	producer = sess1.createProducer(dest);
    
	sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	topic = sess2.createTemporaryTopic();
	cons = sess2.createConsumer(topic);
	listener = new MsgListener();
	cons.setMessageListener(listener);
    }

    long dt1 = 0L;
    long dt2 = 0L;

    public void run() {

	try {
	    long t1 = System.currentTimeMillis();

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
		    msg.setIntProperty("round", i);
		    msg.setIntProperty("index", NbMsgPerRound-j-1);
		    msg.setJMSReplyTo(topic);
		    producer.send(msg);
		    if (transacted && ((j%10) == 9)) sess1.commit();
		}
		long end = System.currentTimeMillis();
		dt1 += (end-start);
		listener.fxCtrl(i);
	    }

	    listener.fxCtrl(NbRound);

	    long t2 = System.currentTimeMillis();
	    dt2 = t2 - t1;
	} catch (Exception exc) {
	    exc.printStackTrace();
	    System.exit(-1);
	}


	lock.exit();
    }

    /**
     * Implements the <code>javax.jms.MessageListener</code> interface.
     */
    static class MsgListener implements MessageListener {
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
		//System.out.println("receive#2 - " + count);
	    } catch (Throwable exc) {
		exc.printStackTrace();
	    }
	}
    }
}
