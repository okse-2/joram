/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;


import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;


/**
 * @author feliot
 * queueMessageReadMax = 10. So consumer read max  10 message with 1 request. and then 
 * consumer2 read max 10 messages with 1 request
 *
 */
public class QueueReadMaxTest extends TestCase {

  public static final int LOOP_NB = 500;

  public static void main(String[] args) {
    new QueueReadMaxTest().run();
  }
  
  private int counter;
  
  public void run() {
    try {
      startAgentServer((short) 0, (File) null,
          new String[] { "-DTransaction=fr.dyade.aaa.util.NullTransaction" });
      
      AdminModule.connect("localhost", 2560, "root", "root", 60);

      org.objectweb.joram.client.jms.admin.User user = org.objectweb.joram.client.jms.admin.User
          .create("anonymous", "anonymous", 0);

      org.objectweb.joram.client.jms.Queue localQueue = org.objectweb.joram.client.jms.Queue
          .create(0);
      localQueue.setFreeReading();
      localQueue.setFreeWriting();
      
      ConnectionFactory cf = org.objectweb.joram.client.jms.tcp.TcpConnectionFactory
      	.create("localhost", 2560);
      ((org.objectweb.joram.client.jms.tcp.TcpConnectionFactory)cf)
   	    .getParameters().queueMessageReadMax = 10;
      
      InitialContext ictx = new InitialContext();
      ictx.bind("cf", cf);
      
      // test the JNDI storage
      cf = (ConnectionFactory)ictx.lookup("cf");
      
      Connection connection = cf.createConnection(
          "anonymous", "anonymous");
      
      connection.start();
      
      Session session = connection.createSession(false,
          Session.AUTO_ACKNOWLEDGE);

      Session recSession = connection.createSession(false,
          Session.AUTO_ACKNOWLEDGE);

      Session recSession2 = connection.createSession(false,
	  Session.AUTO_ACKNOWLEDGE);
      MessageProducer producer = session.createProducer(localQueue);

      MessageConsumer consumer = recSession.createConsumer(localQueue);

      MessageConsumer consumer2 = recSession2.createConsumer(localQueue);
       for (int i = 0; i < LOOP_NB; i++) {
        producer.send(session.createTextMessage("msg#" + i));
      }
      consumer.setMessageListener(new MessageListener() {
	      public void onMessage(Message message) {
		  try {
		      synchronized (QueueReadMaxTest.this) {
			  //System.out.println("msg#" + counter);
			  counter++;
			  if (counter == LOOP_NB) {
			      QueueReadMaxTest.this.notify();
			  }
		      }
		  } catch (Exception exc) {}
	      }
	  });
      consumer2.setMessageListener(new MessageListener() {
	      public void onMessage(Message message) {
		  try {
		      synchronized (QueueReadMaxTest.this) {
			  //System.out.println("msg2#" + counter);
			  counter++;
			  if (counter == LOOP_NB) {
			      QueueReadMaxTest.this.notify();
			  }
		      }
		  } catch (Exception exc) {}
	      }
	  });

     synchronized (this) {
        if (counter < LOOP_NB) {
          wait();
        }
      }
      
      connection.close();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
