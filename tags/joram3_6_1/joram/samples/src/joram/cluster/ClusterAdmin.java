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
package cluster;

import fr.dyade.aaa.joram.admin.*;

/**
 * Administers three agent servers for the cluster sample.
 */
public class ClusterAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Cluster administration...");

    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    User user00 = admin.createUser("publisher00", "publisher00", 0);
    User user10 = admin.createUser("subscriber10", "subscriber10", 1);
    User user20 = admin.createUser("subscriber20", "subscriber20", 2); 
    User user21 = admin.createUser("subscriber21", "subscriber21", 2);

    javax.jms.ConnectionFactory cf0 =
      admin.createConnectionFactory("localhost", 16010);
    javax.jms.ConnectionFactory cf1 =
      admin.createConnectionFactory("localhost", 16011);
    javax.jms.ConnectionFactory cf2 =
      admin.createConnectionFactory("localhost", 16012);

    javax.jms.Topic top0 = admin.createTopic(0);
    javax.jms.Topic top1 = admin.createTopic(1);
    javax.jms.Topic top2 = admin.createTopic(2);

    admin.setFreeReading(top0);
    admin.setFreeReading(top1);
    admin.setFreeReading(top2);
    admin.setFreeWriting(top0);
    admin.setFreeWriting(top1);
    admin.setFreeWriting(top2);

    admin.setCluster(top0, top1);
    admin.setCluster(top0, top2);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.bind("top0", top0);
    jndiCtx.bind("top1", top1);
    jndiCtx.bind("top2", top2);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admins closed.");
  }
}
