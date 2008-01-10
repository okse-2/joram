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
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 *test rollback with queue. message is redelivred
 *
 */
public class Test39 extends BaseTest {
    static Connection cnx;
    static Session session;

    public static void main (String args[]) throws Exception {
	new Test39().run();
    }
    public void run(){
	try{
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    User user = User.create("anonymous", "anonymous");
	    Queue queue = Queue.create(0);
	    queue.setFreeReading();
	    queue.setFreeWriting();

	    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
	    AdminModule.disconnect();

	    cnx = cf.createConnection();
	    session = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod = session.createProducer(queue);
	    MessageConsumer cons = session.createConsumer(queue);
	    cnx.start();

	    Message msg = session.createMessage();
	    msg.setIntProperty("Index", 0);
	    prod.send(msg);

	    msg = session.createMessage();
	    msg.setIntProperty("Index", 1);
	    prod.send(msg);

	    session.commit();

	    msg = cons.receive();
	    int index = msg.getIntProperty("Index");
	    //System.out.println("receives  msg#" + index + " should be msg#0");
	    assertEquals(0,index);
	    session.rollback();

	    msg = cons.receive();
	    index = msg.getIntProperty("Index");
	    //System.out.println("receives  msg#" + index + " should be msg#0");
	    assertEquals(0,index);
	    session.commit();

	    session.close();
	    cnx.close();
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
