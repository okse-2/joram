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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.joram.mom.dest.amqp.LiveServerConnection;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;

public class PersistenceKillTest extends BaseTst {

  private LiveServerConnection senderConnection;

  private LiveServerConnection receiverConnection;

  private Object lock = new Object();

  long nbRounds = 200;
  long nbMsgRound = 100;

  @Test
  public void killingTest() throws Exception {

    senderConnection = new LiveServerConnection("sender", "localhost", 5672, null, null);
    receiverConnection = new LiveServerConnection("receiver", "localhost", 5672, null, null);

    senderConnection.startLiveConnection();
    receiverConnection.startLiveConnection();

    Channel senderChannel = senderConnection.getConnection().createChannel();
    senderChannel.txSelect();

    DeclareOk declareOk = senderChannel.queueDeclare("testQueue", true, false, false, null);

    new Thread(new Runnable() {

      int received;
      long totalReceived;

      Channel consumerChannel;
      QueueingConsumer consumer;

      // Consumer thread
      public void run() {

        try {
          consumerChannel = receiverConnection.getConnection().createChannel();
          consumerChannel.txSelect();
          consumer = new QueueingConsumer(consumerChannel);
          consumerChannel.basicConsume("testQueue", false, consumer);
        } catch (Exception exc) {
          exc.printStackTrace();
        }

        while (true) {
          QueueingConsumer.Delivery delivery;
          try {
            delivery = consumer.nextDelivery();
            long receivedNb = Long.parseLong(new String(delivery.getBody()));
            consumer.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            try {
              Thread.sleep(1);
            } catch (InterruptedException exc1) {
            }

            if (receivedNb < totalReceived) {
              System.out.println("Duplicate received: " + receivedNb);
              continue;
            }

            // We can receive duplicates but can't miss one message
            // One duplicate if the channel is transacted, multiple if it is not
            Assert.assertEquals(totalReceived, receivedNb);

            totalReceived++;
            received++;

            consumerChannel.txCommit();

            if (received == nbMsgRound) {
              received = 0;
              synchronized (lock) {
                lock.notify();
              }
            }

            if (totalReceived == nbMsgRound * nbRounds) {
              consumer.getChannel().close();
              return;
            }

          } catch (Exception ie) {
            if (totalReceived == nbRounds * nbMsgRound) {
              return;
            }
            System.out.println("Consumer connection broken. Reconnect.");
            while (!receiverConnection.isConnectionOpen()) {
              try {
                Thread.sleep(100);
              } catch (InterruptedException exc) {
                exc.printStackTrace();
              }
            }
            try {
              consumerChannel = receiverConnection.getConnection().createChannel();
              consumerChannel.txSelect();
              consumer = new QueueingConsumer(consumerChannel);
              consumerChannel.basicConsume("testQueue", false, consumer);
            } catch (IOException exc) {
              exc.printStackTrace();
            }
            System.out.println("Consumer Reconnected --- totalReceived = " + totalReceived);
            continue;
          }
        }
      }
    }).start();


    long start = System.nanoTime();

    // Killer thread
    new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(5000);
          System.out.println("Kill server");
          admin.killAgentServer((short) 0);

          admin.startAgentServer((short) 0);
        } catch (Exception exc) {
          exc.printStackTrace();
        }
      }
    }).start();

    // Sender
    for (int i = 0; i < nbRounds; i++) {
      if (i % 20 == 0) {
        long delta = System.nanoTime() - start;
        System.out.println("Round " + i + " " + ((i * nbMsgRound * 1000000000L) / delta) + " msg/s");
      }
      try {
        for (int j = 0; j < nbMsgRound; j++) {
          senderChannel.basicPublish("", declareOk.getQueue(), MessageProperties.PERSISTENT_BASIC, new Long(i
              * nbMsgRound + j).toString().getBytes());
        }

        synchronized (lock) {
          senderChannel.txCommit();
          lock.wait();
        }
      } catch (Exception exc) {
        i--;
        System.out.println("Sender connection broken. Reconnect.");
        while (!senderConnection.isConnectionOpen()) {
          Thread.sleep(100);
        }
        senderChannel = senderConnection.getConnection().createChannel();
        senderChannel.txSelect();
        System.out.println("Sender Reconnected");
        Thread.sleep(1000);
        System.out.println("Restart Sender");
      }
    }

    long delta = System.nanoTime() - start;
    System.out.println(delta / 1000000L + " ms");
    System.out.println(((nbRounds * nbMsgRound * 1000000000L) / delta) + " msg/s");

    senderChannel.queueDelete(declareOk.getQueue());

    senderChannel.close();

    senderConnection.stopLiveConnection();
    receiverConnection.stopLiveConnection();

  }

}
