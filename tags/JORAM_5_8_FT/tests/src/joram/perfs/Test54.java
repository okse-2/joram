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

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

class MsgList54 implements MessageListener {
  public void onMessage(Message msg) {
    try {
      int index = msg.getIntProperty("Index");
      Test54.lock.count();
    } catch(Throwable exc) {
      exc.printStackTrace();
    }
  }
}

class Lock54 {
  int count;

  Lock54(int count) {
    this.count = count;
  }

  synchronized void count() {
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

public class Test54 extends BaseTest {
  static Lock54 lock = null;

    public static void main (String args[]) {
	new Test54().run();
    }
    public void run(){
	try {
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    Thread.sleep(1000L);
	    writeIntoFile("===================== start test 54 =====================");
	    int nbtry = 10000;
	    lock = new Lock54(nbtry);
	    
	    AdminModule.connect("root", "root", 60);
	    User user = User.create("anonymous", "anonymous");
	    Topic topic = Topic.create(0);
	    topic.setFreeReading();
	    topic.setFreeWriting();
	    AdminModule.disconnect();
	    
	    ConnectionFactory cf =  TcpConnectionFactory.create("localhost", 16010);

	    Connection cnx = cf.createConnection();

	    Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);

	    Session sess2 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons2 = sess2.createConsumer(topic);
	    cons2.setMessageListener(new MsgList54());

	    cnx.start();

	    MessageProducer[] prod = new MessageProducer[nbtry];

	    Runtime.getRuntime().gc();
	    writeIntoFile("memory before create producer and send message :" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

	    for (int i=0; i<nbtry; i++) {
		prod[i] = sess1.createProducer(topic);
		Message msg = sess1.createMessage();
		msg.setIntProperty("Index", i);
		prod[i].send(msg);
	    }
	    // waits for all messages
	    lock.ended();

	    Runtime.getRuntime().gc();
	    writeIntoFile("memory after all message sent :" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	    // sess1.close();
      
	    for (int i=0; i<nbtry; i++) {
		prod[i].close();
		prod[i] = null;
	    }

	    Runtime.getRuntime().gc();
	   writeIntoFile ("memory after close producer :" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

	    sess1.close();

	    Runtime.getRuntime().gc();
	    writeIntoFile("memory after close session :" + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

	    cons2.close();
	    sess2.close();
	    cnx.close();

	    Thread.sleep(1000L);
	    AgentServer.stop();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    fr.dyade.aaa.agent.AgentServer.stop();
	    endTest(); 
	}
    }
}
