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
package classic;

import fr.dyade.aaa.joram.admin.*;


/**
 * Administers an agent server for the classic samples.
 */
public class ClassicAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Classic administration...");

    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    javax.jms.Queue queue = admin.createQueue(0);
    javax.jms.Topic topic = admin.createTopic(0);

    javax.jms.ConnectionFactory cf =
      admin.createConnectionFactory("localhost", 16010);
    javax.jms.QueueConnectionFactory qcf =
      admin.createQueueConnectionFactory("localhost", 16010);
    javax.jms.TopicConnectionFactory tcf =
      admin.createTopicConnectionFactory("localhost", 16010);

    User user = admin.createUser("anonymous", "anonymous", 0);

    admin.setFreeReading(queue);
    admin.setFreeReading(topic);
    admin.setFreeWriting(queue);
    admin.setFreeWriting(topic);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("qcf", qcf);
    jndiCtx.bind("tcf", tcf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admin closed.");
  }
}
