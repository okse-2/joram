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
package joram.base;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;


/**
 * check send and receive message with 500 queue
 *
 */
public class Test_Multi_Q extends framework.TestCase {
    static int NbDest = 500;
    static int NbMsg = 5;
    static Destination dest[] = null;
    static ConnectionFactory cf = null;

    public static void main (String args[])  {
	new Test_Multi_Q().run();
    }
    public void run(){
	try{
	    System.out.println("server start");
	    startAgentServer((short)0);

	    org.objectweb.joram.client.jms.admin.AdminModule.connect("localhost", 2560,
								     "root", "root", 60);
	    dest = new Destination[NbDest];
	    for (int i=0; i<NbDest; i++) {
		dest[i] = org.objectweb.joram.client.jms.Queue.create ("zz"+i);
		dest[i].setFreeReading();
		dest[i].setFreeWriting();
	    }
	    
	    User user = User.create("anonymous", "anonymous", 0);
	    cf =  org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);
	    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
	    
	    Connection cnx = cf.createConnection();
	    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    MessageProducer prod = sess.createProducer(null);
	    MessageConsumer cons[] = new MessageConsumer[NbDest];
	    for (int i=0; i<NbDest; i++)
		cons[i] = sess.createConsumer(dest[i]);
	    cnx.start();
	    	   
	   	    
	    TextMessage msg=null;
	    for (int i=0; i<NbMsg; i++) {
		for (int j=0; j<NbDest; j++) {
		    msg = sess.createTextMessage("messagedest"+i+j);
		    prod.send(dest[j], msg);
		}
		System.out.println("message sent: " + i);
	    }
	    
	    try {
		for (int i=0; i<NbMsg; i++) {
		    for (int j=0; j<NbDest; j++) {
			msg = (TextMessage) cons[j].receive();
			if(msg==null){
			    error(new Exception("msg == null"));
			    break;
			}
			assertTrue(msg.getText().startsWith("messagedest"));
		    }
		    
		    System.out.println("message received: " + i);
		}
	    } catch (Exception exc) {
	    exc.printStackTrace();
	    }
	    Thread.sleep(5000L);
	
	}catch(Throwable exc){
	    exc.printStackTrace();
	    error(exc);
	}finally{
	    System.out.println("Server stop ");
	    stopAgentServer((short)0);
	    endTest(); 
	}
    }
}
    
