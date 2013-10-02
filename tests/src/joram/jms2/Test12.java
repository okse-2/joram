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

import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.IllegalStateRuntimeException;
import javax.jms.InvalidDestinationRuntimeException;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test start and stop of JMSContext.
 */
public class Test12 extends TestCase {
  public static void main(String[] args) {
    new Test12().run();
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

      JMSContext context = cf.createContext(JMSContext.CLIENT_ACKNOWLEDGE);
      JMSProducer producer = context.createProducer();
      JMSConsumer consumer = context.createConsumer(queue);
      context.stop();
      
      {
        TextMessage msg = context.createTextMessage("message");
        
        msg.setStringProperty("name", "first");
        producer.send(queue, msg);

        msg.setStringProperty("name", "second");
        producer.send(queue, msg);

        msg.setStringProperty("name", "last");
        producer.send(queue, msg);
        
        TextMessage recv = (TextMessage) consumer.receive(5000L);
        assertTrue("Received a message on a STOPPED connection", (recv == null));
        
        context.start();
        
        recv = (TextMessage) consumer.receive();
        String name = recv.getStringProperty("name");
        assertTrue("Received bad message from started connection: " + name, "first".equals(name));
        
        recv = (TextMessage) consumer.receive();
        name = recv.getStringProperty("name");
        assertTrue("Received bad message from started connection: " + name, "second".equals(name));
        
        recv = (TextMessage) consumer.receive();
        name = recv.getStringProperty("name");
        assertTrue("Received bad message from started connection: " + name, "last".equals(name));
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

}
