/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.bridgeamqp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;

import framework.TestCase;

public class AMQPReceiver extends DefaultConsumer {
  public int nbmsgs = 0;
  private static Connection connection = null;
  
  public static AMQPReceiver createAMQPConsumer(String queue) throws IOException {
    ConnectionFactory cnxFactory = new ConnectionFactory();
    connection = cnxFactory.newConnection();
    Channel channel = connection.createChannel();

    AMQPReceiver consumer = new AMQPReceiver(channel, queue);
    channel.basicConsume(queue, true, consumer);
    
    return consumer;
  }
  
  public AMQPReceiver(Channel channel, String queue) {
    super(channel);
  }

  public void handleDelivery(String consumerTag,
                             Envelope envelope,
                             BasicProperties properties,
                             byte[] body) throws IOException {
    byte type = Byte.parseByte(properties.getType());
    if (type == org.objectweb.joram.shared.messages.Message.TEXT) {
      String text = (String) getBody(body);
//      System.out.println("receive msg = " + text);
      TestCase.assertEquals("Message number " + nbmsgs, text);
    } else {
      TestCase.assertTrue("Message type should be Text", false);
    }
    nbmsgs += 1;
  }

  public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
    System.out.println("handleShutdownSignal");
  }
  
  public void handleCancelOk(java.lang.String consumerTag) {
    System.out.println("handleCancelOk");
  }
  
  public void handleConsumeOk(java.lang.String consumerTag)  {
    System.out.println("handleConsumeOk");
  }
  
  public void handleRecoverOk()  {
    System.out.println("handleRecoverOk");
  }
  
  public void close() throws IOException {
    connection.close();
  }
  
  private Object getBody(byte[] body) {
    if (body == null) return null;

    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;
    Object obj = null;
    try {
      try {
        bais = new ByteArrayInputStream(body);
        ois = new ObjectInputStream(bais);
        obj = ois.readObject();
      } catch (ClassNotFoundException cnfexc) {
        // Could not build serialized object: reason could be linked to 
        // class loaders hierarchy in an application server.
        class Specialized_OIS extends ObjectInputStream {
          Specialized_OIS(InputStream is) throws IOException {
            super(is);
          }

          protected Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
            String n = osc.getName();
            return Class.forName(n, false, Thread.currentThread().getContextClassLoader());
          }
        }

        bais = new ByteArrayInputStream(body);
        ois = new Specialized_OIS(bais);
        obj = ois.readObject(); 
      } finally {
        try {
          ois.close();
        } catch (Exception e) {}
        try {
          bais.close();
        } catch (Exception e) {}
      }
    } catch (Exception e) {
      e.printStackTrace();
      obj = null;
    }

    return (Serializable) obj;
  }
}
