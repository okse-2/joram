/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2005 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - France Telecom R&D
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
package cluster.queue;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.tcp.*;
import org.objectweb.joram.shared.admin.*;
import org.objectweb.joram.client.jms.Queue;

import java.util.Properties;
import java.util.Hashtable;

/**
 * Administers three agent servers for the cluster sample.
 */
public class ClusterAdmin {
  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Cluster administration...");

    AdminModule admin = new AdminModule();
    admin.connect("root", "root", 60);

    Properties prop = new Properties();
    prop.setProperty("period","100");
    prop.setProperty("producThreshold","25");
    prop.setProperty("consumThreshold","2");
    prop.setProperty("autoEvalThreshold","true");
    prop.setProperty("waitAfterClusterReq","100");

//     prop.setProperty("period",args[0]);
//     prop.setProperty("producThreshold",args[1]);
//     prop.setProperty("consumThreshold",args[2]);
//     prop.setProperty("autoEvalThreshold",args[3]);
//     prop.setProperty("waitAfterClusterReq",args[4]);

    String ClusterQueueCN = "org.objectweb.joram.mom.dest.ClusterQueue";

    Queue queue0 = Queue.create(0, null, ClusterQueueCN, prop);
    Queue queue1 = Queue.create(1, null, ClusterQueueCN, prop);
    Queue queue2 = Queue.create(2, null, ClusterQueueCN, prop);
    
    System.out.println("queue0 = " + queue0);
    System.out.println("queue1 = " + queue1);
    System.out.println("queue2 = " + queue1);

    User user0 = User.create("user0", "user0", 0);
    User user1 = User.create("user1", "user1", 1);
    User user2 = User.create("user2", "user2", 2);

    javax.jms.QueueConnectionFactory cf0 =
      QueueTcpConnectionFactory.create("localhost", 16010);
    javax.jms.QueueConnectionFactory cf1 =
      QueueTcpConnectionFactory.create("localhost", 16011);
    javax.jms.QueueConnectionFactory cf2 =
      QueueTcpConnectionFactory.create("localhost", 16012);

    AdminHelper.setQueueCluster(queue0,queue1);
    AdminHelper.setQueueCluster(queue0,queue2);
    
    queue0.addClusteredQueue(queue1);
    queue0.addClusteredQueue(queue2);
    
    Hashtable h = new Hashtable();
    h.put("0",queue0);
    h.put("1",queue1);
    h.put("2",queue2);

    ClusterQueue clusterQueue = new ClusterQueue(h);
    System.out.println("clusterQueue = " + clusterQueue);

    clusterQueue.setReader(user0);
    clusterQueue.setWriter(user0);
    clusterQueue.setReader(user1);
    clusterQueue.setWriter(user1);
    clusterQueue.setReader(user2);
    clusterQueue.setWriter(user2);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("qcf0", cf0);
    jndiCtx.bind("qcf1", cf1);
    jndiCtx.bind("qcf2", cf2);
    jndiCtx.bind("clusterQueue", clusterQueue);
    jndiCtx.bind("queue0", queue0);
    jndiCtx.bind("queue1", queue1);
    jndiCtx.bind("queue2", queue2);
    jndiCtx.close();

    admin.disconnect();

    javax.naming.InitialContext jndiCtx2 = new javax.naming.InitialContext();
    clusterQueue = (ClusterQueue) jndiCtx2.lookup("clusterQueue");
    System.out.println("clusterQueue = " + clusterQueue);
    jndiCtx2.close();

    System.out.println("Admins closed.");
  }
}
