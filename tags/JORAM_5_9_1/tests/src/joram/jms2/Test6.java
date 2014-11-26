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
 * Initial developer(s): 
 */
package joram.jms2;

import javax.jms.CompletionListener;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test calling stop and close from CompletionListener (expect IllegalStateRuntimeException).
 */
public class Test6 extends TestCase implements CompletionListener{
  public static void main(String[] args) {
    new Test6().run();
  }

  JMSContext context = null;
  
  public void run()  {
    try {
      startAgentServer((short) 0);
      Thread.sleep(1000);

      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      AdminModule.connect(cf, "root", "root");   
      User.create("anonymous", "anonymous", 0);
      Queue queue = Queue.create("queue");
      queue.setFreeReading();
      queue.setFreeWriting();
      AdminModule.disconnect();

      context = cf.createContext();
      JMSProducer producer = context.createProducer();
      producer.setAsync(this);
      JMSConsumer consumer = context.createConsumer(queue);
      context.start();

      int i = 0;
      
      {
        // Test to close the context from CompletionListener (this will try to close
        // only the session as there is a second active context).
        TextMessage msg = context.createTextMessage("stop");
        producer.send(queue, msg);
        Thread.sleep(2000L);
        assertTrue("Should expect message completion", nbmsg == ++i);
        Message recv = consumer.receive(1000L);
        assertTrue("Should not receive message", (recv == null));
        context.start();
        recv = consumer.receive(1000L);
        assertTrue("Should receive message", (recv != null));        
      }
      
      JMSContext context2 = context.createContext(JMSContext.AUTO_ACKNOWLEDGE);

      {
        // Test to close the context from CompletionListener (this will try to close
        // only the session as there is a second active context).
        TextMessage msg = context.createTextMessage("close");
        producer.send(queue, msg);
        Thread.sleep(2000L);
        assertTrue("Should expect message completion", nbmsg == ++i);
      }
      
      context2.close();
      {
        // Test to close the context from CompletionListener (this will try to close
        // the connection as there is no other active context).
        TextMessage msg = context.createTextMessage("close");
        producer.send(queue, msg);
        Thread.sleep(2000L);
        assertTrue("Should expect message completion", nbmsg == ++i);
      }
      
      context.close();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();     
    }
  }
  
  volatile int nbmsg = 0;
  
  @Override
  public void onCompletion(Message message) {
    try {
      if ("stop".equals(((TextMessage) message).getText())) {
        try {
          context.stop();
          assertTrue("expect no exception.", true);
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), false);
        }
      } else if ("close".equals(((TextMessage) message).getText())) {
        try {
          context.close();
          assertTrue("expect an IllegalStateRuntimeException.", false);
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), e instanceof IllegalStateRuntimeException);
        }
      }
      nbmsg += 1;
    } catch (JMSException e) {
      exception(e);
    }
  }

  @Override
  public void onException(Message message, Exception exc) {
    exception(exc);
  }
}
