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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class AMQPSender implements Runnable {
  private int nbmsgs;
  private long delay;
  private String queue;
  private boolean persistent;
  private String SenderId;

  public AMQPSender(String queue, boolean persistent, int nbmsgs, long delay) {
    this.queue = queue;
    this.persistent = persistent;
    this.nbmsgs = nbmsgs;
    this.delay = delay;
    this.SenderId = "S" + System.currentTimeMillis() + '_';
  }

  public void run() {
    ConnectionFactory cnxFactory = null;
    Connection connection = null;
    Channel channel = null;
    try {
      cnxFactory = new ConnectionFactory();
      connection = cnxFactory.newConnection();
      channel = connection.createChannel();
//      channel.queueDeclare(queue, true, false, false, null);
    } catch (Exception exc) {
      exc.printStackTrace();
      return;
    }
    
    System.out.println(SenderId + " starts");

    // Convert message properties
    AMQP.BasicProperties props = new AMQP.BasicProperties();
    if (persistent) {
      props.setDeliveryMode(Integer.valueOf(org.objectweb.joram.shared.messages.Message.PERSISTENT));
    } else {
      props.setDeliveryMode(Integer.valueOf(org.objectweb.joram.shared.messages.Message.NON_PERSISTENT));
    }
    props.setCorrelationId(null);
    props.setPriority(4);
    props.setType(String.valueOf(org.objectweb.joram.shared.messages.Message.TEXT));
    props.setExpiration("0");

    try {
      for (int i=0; i<nbmsgs; i++) {
        props.setTimestamp(new Date());
        props.setMessageId(SenderId + i);
        channel.basicPublish("", queue, props, getBody("Message number " + i));
        if (delay > 0) Thread.sleep(delay);
      }
    } catch (Exception exc) {
      exc.printStackTrace();
    } finally {
      try {
        System.out.println(SenderId + " stops");
        connection.close();
      } catch (IOException exc) {
        exc.printStackTrace();
      }
    }
  }
  
  private byte[] getBody(Object body) throws IOException {
    if (body == null) return null;

    ByteArrayOutputStream baos = null;
    ObjectOutputStream oos = null;
    try {
      baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
      oos.writeObject(body);
      oos.flush();
      return baos.toByteArray();
    } finally {
      if (oos != null)
        oos.close();
      if (baos != null)
        baos.close();
    }
  }
}
