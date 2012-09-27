/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 - 2012 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Kills the server during the messages production, with Exit interceptor.
 * The producer must throw a JMSException.
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
  
  int nbexc = 0;
  
  public void run() {
    try {
      System.out.println("Start agent server");
      startAgentServer((short)0, new String[]{"-DTransaction=fr.dyade.aaa.ext.NGTransaction"});
      
      System.out.println("Connect");
      AdminModule.connect("localhost", 2560, "root", "root", 60);

      System.out.println("create user");
      User user =  User.create("anonymous", "anonymous", 0);
      //  user.addInterceptorsIN("joram.client.Exit1");

      System.out.println("create queue");
      Queue queue = Queue.create(0);
      queue.addInterceptors("joram.client.Exit1");
      queue.setFreeReading();
      queue.setFreeWriting();

      AdminModule.disconnect();

      dest = queue;

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);

      System.out.println("create connection");
      connection = cf.createConnection("anonymous", "anonymous");
      connection.start();

      connection.setExceptionListener(new MsgExceptionListener());

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      producer = session.createProducer(dest);
      consumer = session.createConsumer(dest);

      System.out.println("connected");

      try {
        TextMessage msg = session.createTextMessage("hello");
        producer.send(msg);
        System.out.println("send 0");
        assertTrue("NO Exception !", false);
      } catch (Exception exc) {
        System.out.println("Exception on sending: " + exc);
        assertTrue("unexpected exception on sending: " + exc, exc instanceof javax.jms.JMSException);
      }

      Thread.sleep(5000);
      
      assertTrue("bad number of exception: " + nbexc, (nbexc == 1));

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
  
  class MsgExceptionListener implements ExceptionListener {
		@Override
    public void onException(JMSException exc) {
		  System.out.println("onException: " + exc);
		  nbexc += 1;
    }
  }
}
