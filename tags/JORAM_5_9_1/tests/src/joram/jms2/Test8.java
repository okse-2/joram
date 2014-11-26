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

import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test calling stop from MessageListener (expect IllegalStateRuntimeException)..
 */
public class Test8 extends TestCase implements MessageListener {
  public static void main(String[] args) {
    new Test8().run();
  }
  
  JMSContext context = null;
  JMSContext context2 = null;
  
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
      context2 = context.createContext(JMSContext.AUTO_ACKNOWLEDGE);
      JMSConsumer consumer = context2.createConsumer(queue);
      consumer.setMessageListener(this);
      context.start();
      context2.start();

      TextMessage msg = context.createTextMessage("stop");
      producer.send(queue, msg);
      Thread.sleep(2000L);
      assertTrue("Should complete recv", (nbmsg == 1));

      msg = context.createTextMessage("close");
      producer.send(queue, msg);
      Thread.sleep(2000L);
      assertTrue("Should complete recv", (nbmsg == 2));
      
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
  public void onMessage(Message message) {
    try {
      if ("stop".equals(((TextMessage) message).getText())) {
        try {
          context2.stop();
          assertTrue("should caught IllegalStateRuntimeException", true);
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), (e instanceof IllegalStateRuntimeException));
        }
        nbmsg += 1;
      } else if ("close".equals(((TextMessage) message).getText())) {
        try {
          context2.close();
          assertTrue("should caught IllegalStateRuntimeException", true);
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), (e instanceof IllegalStateRuntimeException));
        }
        nbmsg += 1;
      }
    } catch (JMSException e) {
      exception(e);
    }
  }
}

