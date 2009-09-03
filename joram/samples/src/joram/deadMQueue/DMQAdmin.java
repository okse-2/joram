/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package deadMQueue;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.tcp.*;

/**
 * Administers an agent server for the deadMQueue samples.
 */
public class DMQAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("DMQ administration...");

    AdminModule.connect("root", "root", 60);

    Queue queue = (Queue) Queue.create(0);
    Topic topic = (Topic) Topic.create(0);

    DeadMQueue userDmq = (DeadMQueue) DeadMQueue.create(0);
    DeadMQueue destDmq = (DeadMQueue) DeadMQueue.create(0);

    User ano = User.create("anonymous", "anonymous", 0);
    User dmq = User.create("dmq", "dmq", 0);

    javax.jms.ConnectionFactory cnxFact =
      TcpConnectionFactory.create("localhost", 16010);

    ano.setDMQ(userDmq);
    queue.setDMQ(destDmq);
    topic.setDMQ(destDmq);

    ano.setThreshold(2);
    queue.setThreshold(2);

    queue.setFreeReading();
    queue.setFreeWriting();
    topic.setFreeReading();
    topic.setFreeWriting();
    userDmq.setReader(dmq);
    userDmq.setWriter(dmq);
    destDmq.setReader(dmq);
    destDmq.setWriter(dmq);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("userDmq", userDmq);
    jndiCtx.bind("destDmq", destDmq);
    jndiCtx.bind("cnxFact", cnxFact);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
