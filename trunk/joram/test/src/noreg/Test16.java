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
package noreg;

import java.lang.reflect.Method;

import javax.jms.*;

import fr.dyade.aaa.agent.AgentServer;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

class ExcList16 implements ExceptionListener {
  String name = null;
  int nbexc;

  ExcList16(String name) {
    this.name = name;
    nbexc = 0;
  }

  public void onException(JMSException exc) {
    nbexc += 1;
//     System.err.println(name + ": " + exc.getMessage());
  }
}

/**
 *
 */
public class Test16 extends BaseTest {
    static ConnectionFactory cf;
    static Destination dest;
    static Connection cnx1;

    public static void main (String args[]) throws Exception {
	new Test16().run();
    }
    public void run(){
	try{
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
	    cnx2.setExceptionListener(new ExcList16("Sender"));
	    Session sess2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess2.createProducer(dest);
	    cnx2.start();

	    Sender16 sender = new Sender16(cnx2, sess2, producer);
	    new Thread(sender).start();

	    ExcList16 exclst = new ExcList16("Receiver");

	    int nb1 = 0; int nb2 = 0; int idx = 0;
	    for (int i=0; i<100; i++) {
		//       System.out.println("connecting#" + i);
		cnx1 = cf.createConnection();
		cnx1.setExceptionListener(exclst);
		Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer cons = sess1.createConsumer(dest);
		cnx1.start();
		//       System.out.println("connected#" + i);
		try {
		    while (true) {
			Message msg = cons.receive();
			if (msg == null) {
			    // this message consumer is concurrently closed (see JavaDoc)
			    continue;
			}
			nb2 += 1;
			int index = msg.getIntProperty("index");
			if (index != idx) {
			    //         System.out.println("recv#" + idx + '/' + index);
			    nb1 += 1;
			}
			idx = index +1;
		    }
		} catch (JMSException exc) {
		    //         System.out.println("end recv#" + i + '/' + nb + ": " + exc.getMessage());
		}
     
	    }

	    System.out.println("Test OK: " + exclst.nbexc + ", " + nb1 + ", " + nb2);

	    sender.waitEnd();
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}

class Sender16 implements Runnable {
  Connection cnx;
  Session session;
  MessageProducer producer;
  boolean ended;
  Object lock;

  public Sender16(Connection cnx,
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
      for (int i=0; i<5000; i++) {
        Message msg = session.createMessage();
        msg.setIntProperty("index", i);
        producer.send(msg);
        if ((i%50) == 49) {
          Test16.cnx1.close();
          Test16.cnx1 = null;
        }
      }
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
