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
package joram.dursub;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * check  unsubscribe null throw an exception
 *
 */
public class Test2 extends framework.TestCase {
    public static void main (String args[]) {
	new Test2().run();
    }
    public void run(){
	try {
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();

	    AdminModule.connect("localhost",2560,"root", "root", 60);
	    Topic topic = Topic.create("Topic");
	    topic.setFreeReading();
	    topic.setFreeWriting();
	    User user = User.create("anonymous", "anonymous", 0);
	    AdminModule.disconnect();

	    ConnectionFactory cf =  LocalConnectionFactory.create();

	    Connection cnx1 = cf.createConnection();
	    Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = sess1.createDurableSubscriber(topic, "subname");
	    sess1.close();

	    sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    try{
	    sess1.unsubscribe(null);
	    }catch(javax.jms.JMSException jms){
		assertTrue(jms instanceof javax.jms.JMSException);
	    }
	    sess1.close();
	    cnx1.close();
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
