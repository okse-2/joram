/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): Feliot David  (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * Checks that a message listener can be closed
 * during a closure consumer and a closure session
 */
public class ClientTest19 extends TestCase{

    private static MessageConsumer msgCons;
    
    private static Session consSession;
    
    public static void main (String args[]) throws Exception {
	new  ClientTest19().run();
    }
    public static void run(){
	try{
	    AgentServer.init((short) 0, "./s0", null);
	    AgentServer.start();
	    
	    org.objectweb.joram.client.jms.admin.AdminModule.collocatedConnect(
									       "root", "root");
	    
	    Destination dest = org.objectweb.joram.client.jms.Queue.create(0);
	    dest.setFreeReading();
	    dest.setFreeWriting();
	    
	    User user = User.create("anonymous", "anonymous", 0);
	    org.objectweb.joram.client.jms.admin.AdminModule.disconnect();
	    
	    ConnectionFactory cf = 
		new org.objectweb.joram.client.jms.local.LocalConnectionFactory();
	    Connection cnx = cf.createConnection();
	    
	    consSession = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
	    Session prodSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
	    
	    MessageProducer msgProd = prodSession.createProducer(dest);
	    
	    cnx.start();
	    
	    for (int i = 0; i < 20; i++) {
		msgCons = consSession.createConsumer(dest);
		msgCons.setMessageListener(new MessageListener() {
			public void onMessage(Message msg) {
			    try {
				//System.out.println("Msg received: " + ((TextMessage)msg).getText());
			    } catch (Exception exc) {
				exc.printStackTrace();
			    }
			}
		    });
		
		TextMessage msg = prodSession.createTextMessage("test1-" + i);
		//System.out.println("Send msg: " + msg.getText());
		msgProd.send(msg);
		
		new Thread() {
		    public void run() {
			try {
			    //System.out.println("Closing consumer");
			    msgCons.close();
			    //System.out.println("Consumer closed");
			} catch (Exception exc) {
			    exc.printStackTrace();
			}
		    }
		}.start();
		
		Thread.sleep(500);
	    }
	    
	    for (int i = 0; i < 20; i++) {
		consSession = cnx.createSession(false, Session.CLIENT_ACKNOWLEDGE);
		msgCons = consSession.createConsumer(dest);
		msgCons.setMessageListener(new MessageListener() {
			public void onMessage(Message msg) {
			    try {
				//System.out.println("Msg received: " + ((TextMessage)msg).getText());
			    } catch (Exception exc) {
				exc.printStackTrace();
			    }
			}
		    });
		
		TextMessage msg = prodSession.createTextMessage("test2-" + i);
		//System.out.println("Send msg: " + msg.getText());
		msgProd.send(msg);
		
		new Thread() {
		    public void run() {
			try {
			    //System.out.println("Closing session");
			    consSession.close();
			    //System.out.println("Session closed");
			} catch (Exception exc) {
			    exc.printStackTrace();
			}
		    }
		}.start();
		
		Thread.sleep(1000);
	    }
	    
	    cnx.close();

	} catch (Throwable exc) {
	    exc.printStackTrace();
	    error(exc);
	} finally {
	    AgentServer.stop();
	    endTest();     
	}
	
    }
}
