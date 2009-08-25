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
package joram.recovery;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 *
 */
public class Test8_3 extends framework.TestCase{
    static Connection cnx;
    static Session session;

    public static void main (String args[]) throws Exception {
	new Test8_3().run();
    }
    public void run(){
	try{
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);
	    short sid = Integer.getInteger("sid", 0).shortValue();

	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    User user = User.create("anonymous", "anonymous");
	    Properties prop = new Properties();
	    prop.setProperty("period", "500");
	    Queue queue = Queue.create(0, prop);
	    DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
	    dmq.setFreeReading();
	    AdminModule.setDefaultDMQ(0, dmq);

	    queue.setFreeReading();
	    queue.setFreeWriting();

	    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
	    AdminModule.disconnect();

	    cnx = cf.createConnection();
	    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod = session.createProducer(queue);
	    MessageConsumer cons = session.createConsumer(dmq);
	    prod.setTimeToLive(1000L);
	    cnx.start();

	    Message msg = session.createMessage();
	    msg.setIntProperty("Index", 0);
	    prod.send(msg);

	    msg = session.createMessage();
	    msg.setIntProperty("Index", 1);
	    prod.send(msg);

	    Thread.sleep(3000L);

	    msg = cons.receive();
	    int index = msg.getIntProperty("Index");
	    //System.out.println("receives from DMQ msg#" + index + " should be msg#0");
	    assertEquals(0,index);

	    session.close();
	    cnx.close();

	    Thread.sleep(1000L);
	    System.out.println("Stop Server#0");
	    AgentServer.stop();
	    AgentServer.reset();

	    Thread.sleep(1000L);
	    System.out.println("Start Server#0");
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);

	    cnx = cf.createConnection();
	    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    prod = session.createProducer(queue);
	    cons = session.createConsumer(dmq);
	    prod.setTimeToLive(1000L);
	    cnx.start();

	    msg = session.createMessage();
	    msg.setIntProperty("Index", 2);
	    prod.send(msg);

	    Thread.sleep(3000L);

	    msg = cons.receive();
	    index = msg.getIntProperty("Index");
	    //System.out.println("receives from DMQ msg#" + index + " should be msg#1");
	    assertEquals(1,index);
	    session.close();
	    cnx.close();
	    Thread.sleep(1000L);

	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
