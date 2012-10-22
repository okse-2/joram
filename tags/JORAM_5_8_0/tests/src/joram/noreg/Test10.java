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
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ConnectionMetaData;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Configuration;

/**
 *
 */
public class Test10 extends BaseTest {
    static int NbRound = 100;
    static ConnectionFactory cf = null;

    public static void main (String args[]) throws Exception {
	new Test10().run();
    }
    public void run(){
	try{
	    writeIntoFile("==================== start test ======================");
	    startServer();

	    String baseclass = "joram.noreg.TcpBaseTest";
	    baseclass = System.getProperty("BaseClass", baseclass);

	    NbRound = Integer.getInteger("NbRound", NbRound).intValue();

        ConnectionFactory cf = createConnectionFactory(baseclass);
        AdminModule.connect(cf);

	    Destination dest = createDestination("org.objectweb.joram.client.jms.Topic");
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    User.create("anonymous", "anonymous", 0);

	    AdminModule.disconnect();

	    Connection cnx = cf.createConnection();
	    ConnectionMetaData cnxmd = cnx.getMetaData();

	    Consumer10 c1 = new Consumer10(cnx, dest, "");
	    c1.start();

	    Consumer10 c2 = new Consumer10(cnx, dest,
					   "type=4 and hostname='192.168.11.117'");
	    c2.start();

	    Consumer10 c3 = new Consumer10(cf.createConnection(), dest, "");
	    c3.start();

	    Consumer10 c4 = new Consumer10(cf.createConnection(), dest,
					   "type=4 and hostname='192.168.11.117'");
	    c4.start();

	    writeIntoFile("----------------------------------------------------");
	    writeIntoFile("Provider: " + cnxmd.getJMSProviderName() + cnxmd.getProviderVersion());
	    writeIntoFile("Transaction: " + Configuration.getProperty("Transaction"));
	    writeIntoFile("Engine: " + Configuration.getProperty("Engine"));
	    writeIntoFile("baseclass: " + baseclass);
	    writeIntoFile("NbRound=" + NbRound);
	    writeIntoFile("----------------------------------------------------");

	    Session sess1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess1.createProducer(dest);
	    cnx.start();

	    int counter = 0;
	    for (int i=0; i<NbRound; i++) {
		Message msg = sess1.createMessage();
		if ((i%2) == 0) {
		    msg.setIntProperty("type", 4);
		    if ((i%4) == 0) {
			msg.setStringProperty("hostname", "192.168.11.117");
			counter += 1;
		    } else {
			msg.setStringProperty("hostname", "192.168.1.1");
		    }
		}
		producer.send(msg);
	    }

	    Thread.sleep(1000L);
	   
	    assertEquals(NbRound,c1.counter);
	    assertEquals(NbRound,c3.counter);
	    assertEquals(counter,c2.counter);
	    assertEquals(counter,c4.counter);
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}

class Consumer10 implements MessageListener {
  Connection cnx;
  Destination dest;
  String selector;

  Session sess;
  MessageConsumer cons;

  public Consumer10(Connection cnx, Destination dest, String selector) throws Exception {
    this.cnx = cnx;
    this.dest = dest;
    this.selector = selector;

    sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    cons = sess.createConsumer(dest, selector);
    cons.setMessageListener(this);
  }

  public void start() throws Exception {
    cnx.start();
  }

  int counter = 0;

  public synchronized void onMessage(Message msg) {
    try {
      counter += 1;
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }
}
