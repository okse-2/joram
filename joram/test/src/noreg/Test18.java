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
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

class ExcList18 implements ExceptionListener {
    String name = null;
    int nbexc;

    ExcList18(String name) {
	this.name = name;
	nbexc = 0;
    }

    public void onException(JMSException exc) {
	nbexc += 1;
	System.err.println(name + ": " + exc.getMessage());
	Test18.assertTrue(exc instanceof javax.jms.IllegalStateException);
	//exc.printStackTrace();
    }
}

class MsgList18 implements MessageListener {
    public void onMessage(Message msg) {
	try {
	    //System.out.println("onMessage#1");
	    Thread.sleep(5000L);

	    Destination dest = msg.getJMSReplyTo();

	    Session ssn = Test18.cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    Message msg2 = ssn.createMessage();

	    //System.out.println("onMessage#2");
	    MessageProducer publisher = ssn.createProducer(dest);
	    publisher.send(msg2);
	    //System.out.println("onMessage#3");
	} catch(Exception ex) {
	    ex.printStackTrace();
	}
    }
}

/**
 *
 */
public class Test18 extends BaseTest{
    static Connection cnx1;

    static ExcList18 exclst;
    static MsgList18 msglst;

    public static void main (String args[]) throws Exception {
	new Test18().run();
    }
    public void run(){
	try{
	    AgentServer.init((short) 0, "s0", null);
	    AgentServer.start();

	    Thread.sleep(1000L);
	    short sid = Integer.getInteger("sid", 0).shortValue();
	    if (sid == 1)
		framework.TestCase.startAgentServer((short) 1);
	    
	    AdminModule.connect("localhost", 16010, "root", "root", 60);

	    User user1 = User.create("anonymous", "anonymous", 0);
	    User user2 = User.create("anonymous", "anonymous", sid);
	    Topic dest = Topic.create(0);
	    dest.setFreeReading();
	    dest.setFreeWriting();

	    ConnectionFactory cf1, cf2;
	    cf1 =  TcpConnectionFactory.create("localhost", 16010);
	    if (sid == 0) 
		cf2 = cf1;
	    else
		cf2 = TcpConnectionFactory.create("localhost", 16011);
	    AdminModule.disconnect();

	    exclst = new ExcList18("Receiver");
	    cnx1 = cf2.createConnection();
	    cnx1.setExceptionListener(exclst);
	    Session sess1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer cons = sess1.createConsumer(dest);
	    msglst = new MsgList18();
	    cons.setMessageListener(msglst);
	    cnx1.start();

	    Connection con = cf1.createConnection();
	    Session ssn = con.createSession(false, Session.AUTO_ACKNOWLEDGE);

	    Message msg = ssn.createMessage();

	    TemporaryTopic tempTopic = ssn.createTemporaryTopic();
	    msg.setJMSReplyTo(tempTopic);

	    MessageProducer publisher = ssn.createProducer(dest);

	    // * Create Consumer to receive on Temp Topic
	    // * Create and start thread, which calls consumer.recieive
	    // * Now Start the connection
	    // * Wait for the thread to either timeout or successfully return
	    // * Get the handle to the message received (null if timeout)

	    MessageConsumer consumer = ssn.createConsumer(tempTopic);
	    con.start();

	    System.out.println("before send");
	    publisher.send(msg);
	    System.out.println("after send");

	    Thread.sleep(1000L);
	    con.close();
	    System.out.println("after close#1");

	    Thread.sleep(15000L);
	    cnx1.close();
	    System.out.println("after close#2");

	    AgentServer.stop();
	    Thread.sleep(1500L);
	    if(sid==1)
		framework.TestCase.stopAgentServer((short)1);

	    
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    AgentServer.stop();
	    endTest();
	}
    }
}
