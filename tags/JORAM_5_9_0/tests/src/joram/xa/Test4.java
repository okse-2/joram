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
package joram.xa;

import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;


import org.objectweb.joram.client.jms.XidImpl;

import framework.TestCase;

/**
 * Test : recover 
 *            - on send
 *            - on receive
 * 
 */
public class Test4 extends TestCase {

   
    public static void main(String[] args) {
	new Test4().run();
    }
          
    public void run() {
	try {
	    System.out.println("server start");
	    startAgentServer((short)0);
	   
	    admin();
	    System.out.println("admin config ok");
	    
	    Context  ictx = new InitialContext();
	    Queue queue = (Queue) ictx.lookup("queue");
   	    XAConnectionFactory cf = (XAConnectionFactory) ictx.lookup("cf");
	    ictx.close();

	    XAConnection cnx = cf.createXAConnection();
	    XASession sessionp = cnx.createXASession();
	    XASession sessionc = cnx.createXASession();

	    // create a producer and a consumer
	    MessageProducer producer = sessionp.createProducer(queue);
	    MessageConsumer consumer = sessionc.createConsumer(queue);

	    XAResource producerRes = sessionp.getXAResource();
	    Xid xid = new XidImpl(new byte[0], 1, 
				  new String(""+System.currentTimeMillis()).getBytes());
	     producerRes.start(xid, XAResource.TMNOFLAGS);
	   
	    XAResource resource =sessionc.getXAResource();
	    
	    Xid xid1 = new XidImpl(new byte[0], 1, 
				   new String(""+System.currentTimeMillis()).getBytes());
	    resource.start(xid1, XAResource.TMNOFLAGS);
	    cnx.start();


	    // test recover on send

	    // create a text message send to the queue by the pruducer 
	    TextMessage msg = sessionp.createTextMessage();
	    msg.setText("message_type_text");
	    producer.send(msg);

	    producerRes.end(xid, XAResource.TMSUCCESS);
	    
	    producerRes.prepare(xid);
	    
	    TextMessage msg1=(TextMessage) consumer.receive(3000L);
	    assertEquals(null,msg1);
	   
	    // close connection without commit
	    cnx.close();
	    
	    cnx = cf.createXAConnection();
	    sessionp = cnx.createXASession();
	    sessionc = cnx.createXASession(); 
	    producerRes = sessionp.getXAResource();
	    consumer = sessionc.createConsumer(queue);
	    cnx.start();
	    
	    // recover transaction
	    Xid xidtab[]=producerRes.recover(XAResource.TMNOFLAGS);
	    assertEquals(xid.getFormatId(),xidtab[0].getFormatId());
	    
	    producerRes.commit(xidtab[0],false);
	    


	    //test recover on receive
	    resource =sessionc.getXAResource();
	    resource.start(xid1, XAResource.TMNOFLAGS);
	   

	    msg1=(TextMessage) consumer.receive();
	    assertEquals("message_type_text",msg1.getText());
	    
	    resource.end(xid1, XAResource.TMSUCCESS);
	    resource.prepare(xid1);
	    cnx.close();

	    cnx = cf.createXAConnection();
	   
	    sessionc = cnx.createXASession(); 
	    resource = sessionc.getXAResource();
	    consumer = sessionc.createConsumer(queue);
	    cnx.start();
	    
	    // recover transaction
	    Xid xidtab1[]=resource.recover(XAResource.TMNOFLAGS);
	    System.out.println("apres recover"+xidtab1[0].getFormatId());
	    assertEquals(xid1.getFormatId(),xidtab1[0].getFormatId());
	   
	    resource.rollback(xidtab1[0]);
	    resource.start(xidtab1[0], XAResource.TMNOFLAGS);
	    
	    msg1=(TextMessage) consumer.receive();
	    assertEquals("message_type_text",msg1.getText());
	     
	    resource.end(xidtab1[0], XAResource.TMSUCCESS);
	    resource.prepare(xidtab1[0]);
	    resource.commit(xidtab1[0],false);
	    
	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
	    endTest(); 
	}
    }
    
    /**
     * Admin : Create queue and a user anonymous
     *   use jndi
     */
    public void admin() throws Exception {
	// conexion 
	org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
								 "root", "root", 60);
	// create a Queue   
	org.objectweb.joram.client.jms.Queue queue =
	    (org.objectweb.joram.client.jms.Queue) org.objectweb.joram.client.jms.Queue.create("queue"); 

        // create a user
	org.objectweb.joram.client.jms.admin.User user =
	    org.objectweb.joram.client.jms.admin.User.create("anonymous", "anonymous");
	// set permissions
	queue.setFreeReading();
	queue.setFreeWriting();

      	javax.jms.XAConnectionFactory cf = org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory.create("localhost", 2560);

	javax.naming.Context jndiCtx = new javax.naming.InitialContext();
	jndiCtx.bind("cf", cf);
	jndiCtx.bind("queue", queue);
	jndiCtx.close();
	   
	org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
    }
}

