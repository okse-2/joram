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
package cluster.topic;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.tcp.*;

/**
 * Administers three agent servers for the cluster of topics sample.
 */
public class ClusterAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Cluster of topics administration...");

    AdminModule.connect("root", "root", 60);

    User user00 = User.create("publisher00", "publisher00", 0);
    User user10 = User.create("subscriber10", "subscriber10", 1);
    User user20 = User.create("subscriber20", "subscriber20", 2); 
    User user21 = User.create("subscriber21", "subscriber21", 2);

    javax.jms.ConnectionFactory cf0 =
      TcpConnectionFactory.create("localhost", 16010);
    javax.jms.ConnectionFactory cf1 =
      TcpConnectionFactory.create("localhost", 16011);
    javax.jms.ConnectionFactory cf2 =
      TcpConnectionFactory.create("localhost", 16012);

    Topic top0 = (Topic) Topic.create(0);
    Topic top1 = (Topic) Topic.create(1);
    Topic top2 = (Topic) Topic.create(2);

    top0.setFreeReading();
    top1.setFreeReading();
    top2.setFreeReading();
    top0.setFreeWriting();
    top1.setFreeWriting();
    top2.setFreeWriting();

    AdminHelper.setClusterLink(top0, top1);
    AdminHelper.setClusterLink(top0, top2);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.bind("top0", top0);
    jndiCtx.bind("top1", top1);
    jndiCtx.bind("top2", top2);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
