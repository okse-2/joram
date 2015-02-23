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
package joram.perfs;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class MsgList29 implements MessageListener {
  int count = 0;

  long start = 0L;
  long end = 0L;

  public synchronized void waitForEnd(int nb) {
    while (count != nb) {
      try {
        wait();
      } catch (InterruptedException exc) {
      }
    }
  }

  public synchronized void onMessage(Message msg) {
    if (count == 0)
      start = System.currentTimeMillis();
    count += 1;
    end = System.currentTimeMillis();
    notify();
  }
}

/**
 *Several messages on some destination. Counts of msg/s for the receiver
 */
public class Test29 extends framework.TestCase {
    public static void main (String args[]) throws Exception {
	new Test29().run();
    }
    public void run(){
	Connection cnx = null;
	try {
	    writeIntoFile("================== start test ====================");
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);

	    int NbMsg = Integer.getInteger("NbMsg", 10000).intValue();
	    int NbDest = Integer.getInteger("NbDest", 90).intValue();

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    Queue[] dest = new Queue[NbDest];
	    for (int i=0; i<NbDest; i++) {
		dest[i] = Queue.create();
		dest[i].setFreeReading();
		dest[i].setFreeWriting();
	    }
	    User user = User.create("anonymous", "anonymous", 0);

	    ConnectionFactory cf =  LocalConnectionFactory.create();
	    AdminModule.disconnect();

	    cnx = cf.createConnection();

	    Session session1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MsgList29 listener = new MsgList29();
	    for (int i=0; i<NbDest; i++) {
		MessageConsumer cons1 = session1.createConsumer(dest[i]);
		cons1.setMessageListener(listener);
	    }

	    Session session2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod = session2.createProducer(null);

	    cnx.start();

	    for (int i=0; i<NbMsg; i++) {
		Message msg = session2.createMessage();
		//         msg.setIntProperty("index", i);
		prod.send(dest[i%NbDest], msg);
	    }

	    listener.waitForEnd(NbMsg);
	    //ystem.out.println("Receives NbDest=" + NbDest + ", " +((NbMsg * 1000L)/(listener.end - listener.start)) + "msg/s");
	    writeIntoFile("Receives NbDest=" + NbDest + ", " +
			  ((NbMsg * 1000L)/(listener.end - listener.start)) + "msg/s");
	    cnx.close();
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    AgentServer.stop();
	    endTest();
	}
    }
}
