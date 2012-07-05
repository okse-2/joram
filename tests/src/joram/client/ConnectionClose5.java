/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s): 
 */
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;


/**
 * Stop the server during the producer's, with Exit interceptor.
 * The producer must be throw a JMSException.
 */
public class ConnectionClose5 extends TestCase {
  
  public static void main(String[] args) {
    new ConnectionClose5().run();
  }
  
  private Connection connection;
  private Destination dest;
  private Session session;
  private MessageProducer producer;
  private MessageConsumer consumer;
  
  public void run() {
  	try {
  		startAgentServer();
  		connect();

  		try {
				TextMessage msg = session.createTextMessage("hello");
				producer.send(msg);
				System.out.println("send 0");
				assertTrue("NO Exception !", false);
			} catch (Exception exc) {
				System.out.println("Expected exception: " + exc);
				assertTrue("unexpected exception: " + exc, exc instanceof javax.jms.JMSException);
			}
			
			Thread.sleep(5000);
			
  		System.out.println("Close connection");
  		connection.close();
  	} catch (Throwable exc) {
  		exc.printStackTrace();
  		error(exc);
  	} finally {
//  		stopAgentServer((short)0);
  		endTest();     
  	}
  }
  
  private void startAgentServer() throws Exception {
      System.out.println("Start agent server");
    startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.util.NTransaction"});
  }
  
  private void connect() throws Exception {
      System.out.println("Connect");
    AdminModule.connect("localhost", 2560, "root", "root", 60);

    System.out.println("create user");
    org.objectweb.joram.client.jms.admin.User user = 
      org.objectweb.joram.client.jms.admin.User
        .create("anonymous", "anonymous", 0);
//    user.addInterceptorsIN("joram.client.Exit1");

    System.out.println("create queue");
    org.objectweb.joram.client.jms.Queue queue = 
      org.objectweb.joram.client.jms.Queue.create(0);
    queue.addInterceptors("joram.client.Exit1");
    queue.setFreeReading();
    queue.setFreeWriting();
    
    AdminModule.disconnect();

    dest = queue;
    
    ConnectionFactory cf = 
      org.objectweb.joram.client.jms.tcp.TcpConnectionFactory.create("localhost", 2560);
    
    System.out.println("create connection");
    connection = cf.createConnection("anonymous", "anonymous");
    connection.start();
    
    connection.setExceptionListener(new MsgExceptionListener());
    
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = session.createProducer(dest);
    consumer = session.createConsumer(dest);
    
    System.out.println("connected");
  }
  
  class MsgExceptionListener implements ExceptionListener {
		@Override
    public void onException(JMSException exc) {
	   System.out.println("onException: EXCEPTION" + exc);
    }
  	
  }
}
