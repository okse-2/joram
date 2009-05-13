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
package joram.perfs;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.util.Configuration;

/**
 *CarrierIQ Scenario. Measure travel time
 */
public class Test27 extends framework.TestCase{
    static int NbMsg = 10000;
    static int MsgSize = 100;
    static int NbCollector = 4;
    static int NbConverter = 4;

    static Queue queue = null;
    static Topic topic = null;
    static ConnectionFactory cf = null;

    public static void main (String args[]) throws Exception {
	new Test27().run();
    }
    public void run(){
	try{
	    writeIntoFile("==================== start test ================");
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);

	    NbMsg = Integer.getInteger("NbMsg", NbMsg).intValue();
	    MsgSize = Integer.getInteger("MsgSize", MsgSize).intValue();
	    NbCollector = Integer.getInteger("NbCollector", NbCollector).intValue();
	    NbConverter = Integer.getInteger("NbConverter", NbConverter).intValue();

	    AdminModule.collocatedConnect("root", "root");

	    queue = Queue.create();
	    queue.setFreeReading();
	    queue.setFreeWriting();

	    topic = Topic.create();
	    topic.setFreeReading();
	    topic.setFreeWriting();

	    User user = User.create("anonymous", "anonymous", 0);

	    AdminModule.disconnect();

	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
	    writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
	    writeIntoFile("NbCollector=" + NbCollector + ", NbConverter=" + NbConverter);
	    writeIntoFile("NbMsg=" + NbMsg + ", MsgSize=" + MsgSize);
	    writeIntoFile("----------------------------------------------------");

	    ConnectionFactory cf1 =  TcpConnectionFactory.create("localhost", 16010);
	    ConnectionFactory cf2 =  LocalConnectionFactory.create();

	    Connection cnx1 = null;
	    //     Session session = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    //     MessageProducer producer = session.createProducer(queue);
	    Collector[] collector = new Collector[NbCollector];
	    for (int i=0; i<NbCollector; i++) {
		cnx1 = cf1.createConnection();
		collector[i] = new Collector(cnx1, queue, NbMsg/NbCollector, MsgSize);
	    }
	    cnx1.start();

	    Connection cnx2 = cf2.createConnection();
	    Converter converter[] = new Converter[NbConverter];
	    for (int i=0; i<NbConverter; i++) {
		converter[i] = new Converter(cnx2, queue, topic);
	    }
	    cnx2.start();

	    Connection cnx3 = cf2.createConnection();
	    Focus focus = new Focus(cnx3, topic, 3*NbMsg);
	    cnx3.start();

	    for (int i=0; i<NbCollector; i++) {
		new Thread(collector[i]).start();
	    }

	    long start = System.currentTimeMillis();
	    long dt = 0L;
	    int collectorMsgSent, converterMsgRecv, FocusMsgRecv;
	    do {
		collectorMsgSent = 0;
		converterMsgRecv = 0;
		FocusMsgRecv = 0;

		Thread.sleep(1000L);

		dt = System.currentTimeMillis() - start;
		for (int i=0; i<NbCollector; i++)
		    collectorMsgSent += collector[i].counter;
		for (int i=0; i<NbConverter; i++)
		    converterMsgRecv += converter[i].counter;
		FocusMsgRecv = focus.counter;
		
		//	System.out.println("" + (dt/1000L) + ',' + ((1000L * collectorMsgSent) / dt) + ',' + ((1000L * converterMsgRecv) / dt) + ',' + ((1000L * FocusMsgRecv) / dt));
		
	    } while (focus.counter != (3 * NbMsg));
	    
	    focus.ended();
	    
	    writeIntoFile("----------------------------------------------------");
	    for (int i=0; i<NbCollector; i++) {
		writeIntoFile	("| collector dt(us)=" +  (((collector[i].end -collector[i].start) *1000L)/(NbMsg/NbCollector)) + ", " +((1000L * (NbMsg/NbCollector)) / (collector[i].end -collector[i].start)) + "msg/s");
	    }
	    for (int i=0; i<NbConverter; i++) {
		writeIntoFile	("| converter dt(us)=" +  (((converter[i].last - converter[i].start) *1000L)/converter[i].counter) + ", travel time(ms)=" + (converter[i].travel/converter[i].counter) + ", " +((1000L * converter[i].counter) / (converter[i].last - converter[i].start)) + "msg/s");
	    }
	    writeIntoFile("| focus dt(us)=" +  (((focus.last - focus.start) *1000L)/(NbMsg)) + ", travel time(ms)=" + (focus.travel/(NbMsg)) + ", " +((1000L * 3 * NbMsg) / (focus.last - focus.start)) + "msg/s");
	    writeIntoFile("----------------------------------------------------");
	    
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}

class Collector implements Runnable {
    Connection cnx;
    Destination dest;
    int NbMsg;
    int MsgSize;
    
    Session session;
    MessageProducer producer;

    byte[] content = null;

    int transacted = 1;

    public Collector(Connection cnx,
		     Destination dest,
		     int NbMsg,
		     int MsgSize) throws Exception {
	this.cnx = cnx;
	this.dest = dest;
	this.NbMsg = NbMsg;
	this.MsgSize = MsgSize;

	session = cnx.createSession((transacted >1), Session.AUTO_ACKNOWLEDGE);
	producer = session.createProducer(dest);

	content = new byte[MsgSize];
	for (int i = 0; i< MsgSize; i++)
	    content[i] = (byte) (i & 0xFF);
    }

    long start = 0L;
    long end = 0L;

    int counter = 0;

    public void run() {
	try {
	    // System.out.println("run start "+NbMsg);
	    start = System.currentTimeMillis();
	    //System.out.println("start collector "+start);
	    for (counter=0; counter<NbMsg; counter++) {
		BytesMessage msg = session.createBytesMessage();
		msg.writeBytes(content);
		msg.setLongProperty("time", System.currentTimeMillis());
		producer.send(msg);
		//         if ((i%10000)==9999) {
		//           end = System.currentTimeMillis();
		//           System.out.println("| sender dt(us)=" +  (((end -start) *1000L)/counter) +
		//             ", " +((1000L*i) / (end -start)) + "msg/s");
		//         }
		if ((transacted >1) && ((counter%transacted) == 0))
		    session.commit();
	    }
	    end = System.currentTimeMillis();
	    //System.out.println("end collector "+end);
	} catch (Exception exc) {
	    exc.printStackTrace();
	    System.exit(-1);
	}
    }
}

class Converter implements MessageListener {
    Connection cnx;
    Queue queue;
    Topic topic;

    Session sess;
    MessageConsumer cons;
    MessageProducer prod;

    byte[] content = null;

    Converter(Connection cnx, Queue queue, Topic topic) throws Exception {
	this.cnx = cnx;
	this.queue = queue;
	this.topic = topic;

	sess = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
	cons = sess.createConsumer(queue);
	prod = sess.createProducer(topic);

	content = new byte[Test27.MsgSize];
	for (int i = 0; i< Test27.MsgSize; i++)
	    content[i] = (byte) (i & 0xFF);

	cons.setMessageListener(this);
    }

    int counter = 0;
    long travel = 0L;

    long start = 0L;
    long last = 0L;

    public synchronized void onMessage(Message m) {
	try {
	    last = System.currentTimeMillis();
	    if (counter == 0) start = last;
	    counter += 1;
	    BytesMessage msg = sess.createBytesMessage();
	    msg.writeBytes(content);
	    msg.setLongProperty("time", last);
	    prod.send(msg);
	    prod.send(msg);
	    prod.send(msg);
	    travel += (last - m.getLongProperty("time"));
	    sess.commit();
	    //       if ((counter%10000)==0)
	    //         System.out.println("| converter dt(us)=" +  (((last - start) *1000L)/(counter)) + ", travel time(ms)=" + (travel/counter) + ", " + ((1000L * counter) / (last - start)) + "msg/s");

	} catch (Throwable exc) {
	    exc.printStackTrace();
	}
    }
}

class Focus implements MessageListener {
    Connection cnx;
    Topic topic;

    Session sess;
    MessageConsumer cons;
    MessageProducer prod;

    int nbmsg;

    Focus(Connection cnx, Topic topic, int nbmsg) throws Exception {
	this.cnx = cnx;
	this.topic = topic;
	this.nbmsg = nbmsg;

	sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	cons = sess.createConsumer(topic);

	cons.setMessageListener(this);
    }

    int counter = 0;
    long travel = 0L;

    long start = 0L;
    long last = 0L;

    public synchronized void onMessage(Message m) {
	try {
	    BytesMessage msg = (BytesMessage) m;
	    last = System.currentTimeMillis();
	    if (counter == 0L) start = last;
	    counter += 1;
	    travel += (last - msg.getLongProperty("time"));
	    if (counter == nbmsg) notify();
	    //       if ((counter%10000)==0)
	    //         System.out.println("| focus dt(us)=" +  (((last - start) *1000L)/(counter)) + ", travel time(ms)=" + (travel/counter) + ", " + ((1000L * counter) / (last - start)) + "msg/s");
	} catch (Throwable exc) {
	    exc.printStackTrace();
	}
    }

    synchronized void ended() {
	while (counter != nbmsg) {
	    try {
		wait();
	    } catch (InterruptedException exc) {
	    }
	}
    }
}
