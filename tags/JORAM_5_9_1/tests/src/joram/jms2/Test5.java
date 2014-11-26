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
 * Test the completion order when sending in asynchronous mode.
 */
public class Test5 extends TestCase implements MessageListener, CompletionListener {
  public static void main(String[] args) {
    new Test5().run();
  }
  
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

      JMSContext context = cf.createContext();
      JMSProducer producer = context.createProducer();
      producer.setAsync(this);
      context.start();

      TextMessage msg = null;
      for (int i=0; i < 10; i++) {
        msg = context.createTextMessage("msg#" + i);
        msg.setIntProperty("order", i);
        producer.send(queue, msg);
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
  public void onMessage(Message message) {
    nbmsg += 1;
  }

  volatile int sent = 0;
  
  @Override
  public void onCompletion(Message message) {
    int n = -1;
    try {
      n = message.getIntProperty("order");
    } catch (JMSException e) {
      assertTrue("Unexpected exception: " + e.getMessage(), false);
    }
    assertTrue("Received " + n + " should be #"  + sent, (n == sent));
    sent += 1;
  }

  @Override
  public void onException(Message message, Exception exc) {
    exc.printStackTrace();
  }
}
