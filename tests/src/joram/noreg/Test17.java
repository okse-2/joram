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

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;

class ExcList17 implements ExceptionListener {
    String name = null;
    int nbexc;

    ExcList17(String name) {
	this.name = name;
	nbexc = 0;
    }

    public void onException(JMSException exc) {
	nbexc += 1;
	//     System.err.println(name + ": " + exc.getMessage());
    }
}

class MsgList17 implements MessageListener {
    int nb1, nb2, idx;

    MsgList17() {
	nb1 = 0;
	nb2 = 0;
	idx = 0;
    }

    public synchronized void onMessage(Message msg) {
	try {
	    nb2 += 1;
	    int index = msg.getIntProperty("index");
	    if (index != idx) {
		//         System.out.println("recv#" + idx + '/' + index);
		nb1 += 1;
	    }
	    idx = index +1;
	} catch (Throwable exc) {
	    exc.printStackTrace();
	}
    }
}

/**
 *
 */
public class Test17 extends BaseTest {
    static Connection cnx1;
    static ConnectionFactory cf;
    static Destination dest;

    static ExcList17 exclst;
    static MsgList17 msglst;

    public static void main (String args[]) throws Exception {
	new Test17().run();
    }
    public void run(){
	try{
	    writeIntoFile("======================== start test ======================");
	    startServer();

	    String baseclass = "noreg.ColocatedBaseTest";
	    baseclass = System.getProperty("BaseClass", baseclass);

	    String destclass =  "org.objectweb.joram.client.jms.Queue";
	    destclass =  System.getProperty("Destination", destclass);

	    Thread.sleep(500L);
	    AdminConnect(baseclass);

	    User user = User.create("anonymous", "anonymous", 0);
	    dest = createDestination(destclass);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    cf =  createConnectionFactory(baseclass);
	    AdminModule.disconnect();

	    Connection cnx2 = cf.createConnection();
	    Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess2.createProducer(dest);
	    cnx2.start();

	    exclst = new ExcList17("Receiver");
	    cnx1 = cf.createConnection();
	    cnx1.setExceptionListener(exclst);
	    Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = sess1.createConsumer(dest);
	    msglst = new MsgList17();
	    cons.setMessageListener(msglst);
	    cnx1.start();

	    Sender17 sender = new Sender17(cnx2, sess2, producer);
	    new Thread(sender).start();

	    for (int i=0; i<50; i++) {
		Thread.sleep(500L);

		// System.out.println("closing");
		cnx1.close();
		Thread.sleep(100L);

		//System.out.println("connecting#");
		cnx1 = cf.createConnection();
		cnx1.setExceptionListener(exclst);
		sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
		cons = sess1.createConsumer(dest);
		cons.setMessageListener(msglst);
		cnx1.start();
		// System.out.println("connected#");
	    }

	    sender.waitEnd();
	    // System.out.println("Test OK: " + exclst.nbexc + ", " + msglst.nb1 + ", " + msglst.nb2);
	   
	    writeIntoFile("| nb_Exception :"+exclst.nbexc+
			  "\n| nb_NoOrder_receive :"+msglst.nb1+
			  "\n| nb_receive :"+msglst.nb2);

	    cnx1.close();

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}

class Sender17 implements Runnable {
    Connection cnx;
    Session session;
    MessageProducer producer;
    boolean ended;
    Object lock;

    public Sender17(Connection cnx,
		    Session session,
		    MessageProducer producer) throws Exception {
	this.cnx = cnx;
	this.session = session;
	this.producer = producer;

	ended = false;
	lock = new Object();
    }

    public void waitEnd() {
	synchronized (lock) {
	    while (! ended) {
		try {
		    lock.wait();
		} catch (InterruptedException exc) {
		}
	    }
	}
    }

    public void run() {
	try {
	    for (int i=0; i<50000; i++) {
		Message msg = session.createMessage();
		msg.setIntProperty("index", i);
		producer.send(msg);
		//         if ((i%100) == 99) {
		//           System.out.println("closing");
		//           if (Test17.cnx1 != null) {
		//             Test17.cnx1.close();
		//             System.out.println("closed");
		// //             Test17.cnx1 = null;
		//           }
		//         }
	    }
	    System.out.println("end");
	} catch (Exception exc) {
	    exc.printStackTrace();
	} finally {
	    try {
		cnx.close();
	    } catch (Exception exc) {
		exc.printStackTrace();
	    }
	    synchronized (lock) {
		ended = true;
		lock.notify();
	    }
	}
    }
}
