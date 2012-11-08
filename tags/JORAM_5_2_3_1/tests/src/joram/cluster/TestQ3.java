/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.cluster;

import java.util.Hashtable;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.QueueConnectionFactory;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminHelper;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.ClusterQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;

import framework.TestCase;



/**
 * Test : fix big period and check no recieve any message 
 *   
 */
public class TestQ3 extends TestCase {

   
    public static void main(String[] args) {
	new TestQ3().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	    startAgentServer((short)1);
	    startAgentServer((short)2);
	   
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Queue queue0 = (Queue) ictx.lookup("queue0");
	    QueueConnectionFactory qcf0 = (QueueConnectionFactory) ictx.lookup("qcf0");
	    Queue queue1 = (Queue) ictx.lookup("queue1");
	    QueueConnectionFactory qcf1 = (QueueConnectionFactory) ictx.lookup("qcf1");

	    Destination clusterQueue = (Destination) ictx.lookup("clusterQueue");
	    QueueConnectionFactory qcf2 = (QueueConnectionFactory) ictx.lookup("qcf2");

	    Queue queue2 = (Queue) ictx.lookup("queue2");
	     
	    ictx.close();

	    MsgListenerCluster3 listener0 =new MsgListenerCluster3("recv0 ");
	  
	    Connection cnx0 = qcf0.createConnection("user0","user0");
	    Session sess0 = cnx0.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageConsumer recv0 = sess0.createConsumer(queue0);
	    recv0.setMessageListener(listener0);
	   	  
	    Connection cnx = qcf1.createConnection("user1","user1");
	    Session sess = cnx.createSession(false,Session.AUTO_ACKNOWLEDGE);
	    MessageProducer producer = sess.createProducer(null);

	    
	    System.setProperty("location","1");
	   

	    cnx0.start();
	    cnx.start();
	  
	    TextMessage msg = sess.createTextMessage();
	  	    
	    int j;
	    for (j = 0; j < 15; j++) {
		msg.setText("Test number " + j);
		producer.send(clusterQueue, msg);
	
	    }
	 
	    Thread.sleep(10000);
	  
	    assertTrue(listener0.nbMsg == 0);

	   
	    cnx0.close();
	    cnx.close();  
 
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
	    stopAgentServer((short)1);
	    stopAgentServer((short)2);
	    endTest(); 
	}
    }
    
    /**
     * Admin : Create queue and a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	AdminModule admin = new AdminModule();
	admin.connect("root", "root", 60);

	Properties prop = new Properties();
	prop.setProperty("period","10000");
	prop.setProperty("producThreshold","25");
	prop.setProperty("consumThreshold","2");
	prop.setProperty("autoEvalThreshold","true");
	prop.setProperty("waitAfterClusterReq","100");

	String ClusterQueueCN = "org.objectweb.joram.mom.dest.ClusterQueue";

	Queue queue0 = Queue.create(0, null, ClusterQueueCN, prop);
	Queue queue1 = Queue.create(1, null, ClusterQueueCN, prop);
	Queue queue2 = Queue.create(2, null, ClusterQueueCN, prop);
    

	
	System.out.println("queue0 = " + queue0);
	System.out.println("queue1 = " + queue1);
	System.out.println("queue2 = " + queue2);

	User user0 = User.create("user0", "user0", 0);
	User user1 = User.create("user1", "user1", 1);
	User user2 = User.create("user2", "user2", 2);


	javax.jms.QueueConnectionFactory cf0 =
	    QueueTcpConnectionFactory.create("localhost", 16010);
	javax.jms.QueueConnectionFactory cf1 =
	    QueueTcpConnectionFactory.create("localhost", 16011);
	javax.jms.QueueConnectionFactory cf2 =
	    QueueTcpConnectionFactory.create("localhost", 16012);

	AdminHelper.setQueueCluster(queue0,queue1);
	AdminHelper.setQueueCluster(queue0,queue2);
    
	queue0.addClusteredQueue(queue1);
	queue0.addClusteredQueue(queue2);
    
	Hashtable h = new Hashtable();
	h.put("0",queue0);
	h.put("1",queue1);
	h.put("2",queue2);

	ClusterQueue clusterQueue = new ClusterQueue(h);
	System.out.println("clusterQueue = " + clusterQueue);

	clusterQueue.setReader(user0);
	clusterQueue.setWriter(user0);
	clusterQueue.setReader(user1);
	clusterQueue.setWriter(user1);
	clusterQueue.setReader(user2);
	clusterQueue.setWriter(user2);

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("qcf0", cf0);
	jndiCtx.bind("qcf1", cf1);
	jndiCtx.bind("qcf2", cf2);
	jndiCtx.bind("clusterQueue", clusterQueue);
	jndiCtx.bind("queue0", queue0);
	jndiCtx.bind("queue1", queue1);
	jndiCtx.bind("queue2", queue2);
	jndiCtx.close();

	admin.disconnect();

    }
}


class MsgListenerCluster3 implements MessageListener
{
    String ident = null;
    public int nbMsg;

    public MsgListenerCluster3() {}
    
    public MsgListenerCluster3(String ident) {
	nbMsg=0;
	this.ident = ident;
    }
    
    public void onMessage(Message msg) {
	nbMsg++;
    }
}
