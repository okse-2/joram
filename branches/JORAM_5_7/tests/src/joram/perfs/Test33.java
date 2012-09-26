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

import java.util.Date;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 *Memory test with TTL
 *
 */
public class Test33 extends framework.TestCase{
    public static void main (String args[]) throws Exception {
	new Test33().run();
    }
    public void run(){
	Connection cnx = null;
	try {
	    writeIntoFile("======================= start test ===================");
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    Properties prop = new Properties();
	    prop.setProperty("period", "30000");
	    Queue dest = Queue.create(0, prop);
	    dest.setFreeReading();
	    dest.setFreeWriting();
	    User user = User.create("anonymous", "anonymous", 0);

	    ConnectionFactory cf =  LocalConnectionFactory.create();

	    cnx = cf.createConnection();

	    Session session1 = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = session1.createConsumer(dest);
	    MessageProducer prod = session1.createProducer(dest);

	    cnx.start();
	    writeIntoFile("------------------------------------------------------");
	    writeIntoFile("| Before sending: " + new Date() + ", " +
			       (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);

	    Message msg = null;
	    for (int i=0; i<5000; i++) {
		prod.setTimeToLive(3000L);
		msg = session1.createMessage();
		prod.send(msg);
	    }

	    writeIntoFile("| after sending: " + new Date() + ", " +
			       (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
	    Thread.sleep(3600L);

	    writeIntoFile("| after cleaning: " + new Date() + ", " +
			       (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
	    System.gc();
	    writeIntoFile("| after gc: " + new Date() + ", " +
			       (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1024);
	    writeIntoFile("------------------------------------------------------");
	    AdminModule.disconnect();
	    cnx.close();
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally {
	    AgentServer.stop();
	    endTest();
	}
	System.exit(0);
    }
}
