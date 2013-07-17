/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
package joram.client;

import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
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
 * Test: Async send JMS API 2.0
 * - create Queue
 * - create User anonymous with a slow interceptor (on each msg sleep 100ms).
 * - send 10 messages
 * - verify that the send is asynchronous.
 * - count the onCompletion: expected 10
 * - verify the messages order on onCompletion
 * - receive the 10 messages
 * - send 10 messages
 * - the interceptor throw a RuntimeException
 * - count the onException expected 10
 */
public class ClientTestAsyncSend extends TestCase {
  public static void main(String[] args) {
    new ClientTestAsyncSend().run();
  }

  Object lock = new Object();
  
  public void run() {
    try {
      System.out.println("servers start");
      startAgentServer((short)0, new String[]{"-DTransaction.UseLockFile=false"});
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous", 0);
      user.addInterceptorsIN("joram.client.InterceptorSlow");
      
      Queue queue = Queue.create("queue");
      queue.setFreeReading();
      queue.setFreeWriting();

      AdminModule.disconnect();
      // Admin done
      
      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageProducer prod = session.createProducer(queue);
      MessageConsumer consumer = session.createConsumer(queue);
      cnx.start();
      TextMessage msg = session.createTextMessage();

      //Test onCompletion...
      long start = System.currentTimeMillis();
      for (int i = 0; i < 10; i++){
        msg.setText("TEST completion listener " + i);
        msg.setIntProperty("fragment", i+1);
        msg.setBooleanProperty("throwException", false);
        prod.send(msg, new MyCompletionListener());
      }
      long end = System.currentTimeMillis();
      System.out.println("async send in (ms) : " + (end - start));
      assertTrue("Bad async send, send keep many time (ms) : " + (end-start), (end-start) < 50);
      
      synchronized (lock) {
        lock.wait(6000);
      }
      assertEquals("Nb onCompletion", 10, count);
      
      // Receive the messages
      for (int i = 0; i < 10; i++) {
        Message m = consumer.receiveNoWait();
        assertEquals("TEST completion listener " + i, ((TextMessage) m).getText());
      }
      
      // Test onException...
      count = 0;
      for (int i = 0; i < 10; i++){
        msg.setText("TEST Exception listener " + i);
        msg.setIntProperty("fragment", i+1);
        msg.setBooleanProperty("throwException", true);
        prod.send(msg, new MyCompletionListener());
      }
      
      synchronized (lock) {
        lock.wait(6000);
      }
      assertEquals("Nb onException", 10, count);
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest();
    }
  }
 
  int count = 0;
  
  class MyCompletionListener implements CompletionListener {
    @Override
    public void onCompletion(Message msg) {
      count++;
      System.out.println(count + " : onCompletion msg = " + msg);
      try {
        assertEquals("onCompletion fragment", count, msg.getIntProperty("fragment"));
      } catch (JMSException e) { }
      if (count == 10) {
        synchronized (lock) {
          lock.notify(); 
        }
      }
    }

    @Override
    public void onException(Message msg, Exception exc) {
      count++;
      System.out.println(count + " : onException msg = " + msg + ", exc = " + exc);
      if (count == 10) {
        synchronized (lock) {
          lock.notify(); 
        }
      }
    }
  }
}
