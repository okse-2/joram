/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011-2012 ScalAgent Distributed Technologies
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
package joram.amqp;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import framework.TestCase;

public class QueueTest extends TestCase {
  
  public static void main(String[] args) {
    new QueueTest().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      queueDeclareName();
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("finaly kill servers.");
      killAgentServer((short)0);
      endTest(); 
    }
  }
  
  public void queueDeclareName() throws Exception {
    ConnectionFactory cnxFactory = new ConnectionFactory();
    Connection connection = cnxFactory.newConnection();

    Channel channel = connection.createChannel();
    DeclareOk declareOk = channel.queueDeclare("testqueue", true, false, false, null);

    assertEquals("testqueue", declareOk.getQueue());
  }

}
