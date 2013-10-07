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
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MessageFormatException;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 * Test Message.getBody method exceptions.
 */
public class Test4 extends TestCase {
  public static void main(String[] args) {
    new Test4().run();
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
      JMSConsumer consumer = context.createConsumer(queue);
      context.start();

      {
        TextMessage msg = context.createTextMessage("message");
        producer.send(queue, msg);
        TextMessage recv = (TextMessage) consumer.receive();
        assertTrue("Message not received", (recv != null));
        // Call TextMessage.getBody(Boolean.class) to extract TextMessage as Boolean.
        // Expect MessageFormatException.
        try {
          recv.getBody(Boolean.class);
          assertTrue("Expected MessageFormatException to be thrown", false);
        } catch (MessageFormatException e) {
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), false);
        }
      }
      
      {
        ObjectMessage msg = context.createObjectMessage(new StringBuffer("message"));
        producer.send(queue, msg);
        ObjectMessage recv = (ObjectMessage) consumer.receive();
        assertTrue("Message not received", (recv != null));
        // Call ObjectMessage.getBody(HashMap.class) to extract ObjectMessage as HashMap.
        // Expect MessageFormatException.
        try {
          recv.getBody(HashMap.class);
          assertTrue("Expected MessageFormatException to be thrown", false);
        } catch (MessageFormatException e) {
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), false);
        }
      }
      
      {
        StreamMessage msg = context.createStreamMessage();
        msg.writeBoolean(true);
        msg.writeLong((long) 123456789);
        producer.send(queue, msg);
        StreamMessage recv = (StreamMessage) consumer.receive();
        assertTrue("Message not received", (recv != null));
        // Call StreamMessage.getBody(HashMap.class) to extract StreamMessage as HashMap.
        // Expect MessageFormatException.
        try {
          recv.getBody(HashMap.class);
          assertTrue("Expected MessageFormatException to be thrown", false);
        } catch (MessageFormatException e) {
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), false);
        }
      }

      {
        BytesMessage msg = context.createBytesMessage();
        msg.writeBoolean(true);
        msg.writeLong((long) 123456789);
        try {
          msg.getBody(StringBuffer.class);
          assertTrue("Expected MessageFormatException to be thrown", false);
        } catch (MessageFormatException e) {
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), false);
        }

        producer.send(queue, msg);
        BytesMessage recv = (BytesMessage) consumer.receive();
        assertTrue("Message not received", (recv != null));
        // Call BytesMessage.getBody(StringBuffer.class) to receive BytesMessage as StringBuffer
        // Expect MessageFormatException.
        try {
          recv.getBody(StringBuffer.class);
          assertTrue("Expected MessageFormatException to be thrown", false);
        } catch (MessageFormatException e) {
        } catch (Exception e) {
          assertTrue("Caught unexpected exception: " + e.getMessage(), false);
        }
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
