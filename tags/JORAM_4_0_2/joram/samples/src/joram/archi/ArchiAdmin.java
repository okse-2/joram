/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package archi;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.tcp.*;

/**
 * Administers two agent servers for the archi samples.
 */
public class ArchiAdmin
{
  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Archi administration...");

    // Connecting the administrator:
    AdminModule.connect("root", "root", 60);

    // Creating access for user anonymous on servers 0 and 2:
    User user0 = User.create("anonymous", "anonymous", 0);
    User user2 = User.create("anonymous", "anonymous", 2);

    // Creating the destinations on server 1:
    Queue queue = (Queue) Queue.create(1);
    Topic topic = (Topic) Topic.create(1);

    // Setting free access to the destinations:
    queue.setFreeReading();
    topic.setFreeReading();
    queue.setFreeWriting();
    topic.setFreeWriting();

    // Creating the connection factories for connecting to the servers 0 and 2:
    javax.jms.ConnectionFactory cf0 =
      TcpConnectionFactory.create("localhost", 16010);
    javax.jms.ConnectionFactory cf2 =
      TcpConnectionFactory.create("localhost", 16012);

    // Binding the objects in JNDI:
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  } 
}