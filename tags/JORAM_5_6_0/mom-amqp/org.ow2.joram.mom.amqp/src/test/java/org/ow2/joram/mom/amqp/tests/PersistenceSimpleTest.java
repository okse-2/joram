/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package org.ow2.joram.mom.amqp.tests;

import org.junit.Assert;
import org.junit.Test;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class PersistenceSimpleTest extends BaseTst {

  @Test
  public void recover1() throws Exception {
    ConnectionFactory cnxFactory = new ConnectionFactory();
    Connection connection = cnxFactory.newConnection();

    Channel channel = connection.createChannel();
    DeclareOk declareOk = channel.queueDeclare("testqueue", true, false, false, null);

    channel.txSelect();
    for (int i = 0; i < 5; i++) {
      channel.basicPublish("", declareOk.getQueue(), MessageProperties.PERSISTENT_BASIC,
          "this is a test message !!!".getBytes());
    }
    channel.txCommit();

    admin.killAgentServer((short) 0);

    admin.startAgentServer((short) 0);

    connection = cnxFactory.newConnection();
    channel = connection.createChannel();
    declareOk = channel.queueDeclarePassive("testqueue");

    for (int i = 0; i < 5; i++) {
      GetResponse msg = channel.basicGet("testqueue", true);
      Assert.assertNotNull(msg);
      Assert.assertEquals("this is a test message !!!", new String(msg.getBody()));
    }

    GetResponse msg = channel.basicGet("testqueue", true);
    Assert.assertNull(msg);

    channel.queueDelete(declareOk.getQueue());
  }

  @Test
  public void recover2() throws Exception {
    ConnectionFactory cnxFactory = new ConnectionFactory();
    Connection connection = cnxFactory.newConnection();

    Channel channel = connection.createChannel();
    DeclareOk declareOk = channel.queueDeclare("testqueue", true, false, false, null);

    channel.txSelect();
    for (int i = 0; i < 5; i++) {
      channel.basicPublish("", declareOk.getQueue(), MessageProperties.PERSISTENT_BASIC,
          "this is a test message !!!".getBytes());
    }
    channel.txCommit();

    admin.killAgentServer((short) 0);

    admin.startAgentServer((short) 0);
    Thread.sleep(500);

    connection = cnxFactory.newConnection();
    channel = connection.createChannel();
    declareOk = channel.queueDeclarePassive("testqueue");

    QueueingConsumer consumer = new QueueingConsumer(channel);
    channel.basicConsume(declareOk.getQueue(), true, consumer);

    for (int i = 0; i < 5; i++) {
      Delivery msg = consumer.nextDelivery(1000);
      Assert.assertNotNull(msg);
      Assert.assertEquals("this is a test message !!!", new String(msg.getBody()));
    }

    Delivery msg = consumer.nextDelivery(1000);
    Assert.assertNull(msg);

    channel.queueDelete(declareOk.getQueue());

  }

}
