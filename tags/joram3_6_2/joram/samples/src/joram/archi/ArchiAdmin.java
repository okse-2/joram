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
package archi;

import fr.dyade.aaa.joram.admin.*;

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
    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    // Creating access for user anonymous on servers 0 and 2:
    User user0 = admin.createUser("anonymous", "anonymous", 0);
    User user2 = admin.createUser("anonymous", "anonymous", 2);

    // Creating the destinations on server 1:
    javax.jms.Queue queue = admin.createQueue(1);
    javax.jms.Topic topic = admin.createTopic(1);

    // Setting free access to the destinations:
    admin.setFreeReading(queue);
    admin.setFreeReading(topic);
    admin.setFreeWriting(queue);
    admin.setFreeWriting(topic);

    // Creating the connection factories for connecting to the servers 0 and 2:
    javax.jms.ConnectionFactory cf0 =
      admin.createConnectionFactory("localhost", 16010);
    javax.jms.ConnectionFactory cf2 =
      admin.createConnectionFactory("localhost", 16012);

    // Binding the objects in JNDI:
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admin closed.");
  } 
}
