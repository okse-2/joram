/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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

import fr.dyade.aaa.joram.admin.*;

/**
 * Administers an agent server for the deadMQueue samples.
 */
public class DMQAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("DMQ administration...");

    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    javax.jms.Queue queue = admin.createQueue(0);
    javax.jms.Topic topic = admin.createTopic(0);

    DeadMQueue userDmq = admin.createDeadMQueue(0);
    DeadMQueue destDmq = admin.createDeadMQueue(0);

    User ano = admin.createUser("anonymous", "anonymous", 0);
    User dmq = admin.createUser("dmq", "dmq", 0);

    javax.jms.ConnectionFactory cnxFact =
      admin.createConnectionFactory("localhost", 16010);

    admin.setUserDMQ(ano, userDmq);
    admin.setDestinationDMQ(queue, destDmq);
    admin.setDestinationDMQ(topic, destDmq);

    admin.setUserThreshold(ano, 2);
    admin.setQueueThreshold(queue, 2);

    admin.setFreeReading(queue);
    admin.setFreeWriting(queue);
    admin.setFreeReading(topic);
    admin.setFreeWriting(topic);
    admin.setReader(dmq, userDmq);
    admin.setWriter(dmq, userDmq);
    admin.setReader(dmq, destDmq);
    admin.setWriter(dmq, destDmq);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("userDmq", userDmq);
    jndiCtx.bind("destDmq", destDmq);
    jndiCtx.bind("cnxFact", cnxFact);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admin closed.");
  }
}
