/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package cluster.queue;

import java.util.Hashtable;
import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminHelper;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.ClusterQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;

/**
 * Administers three agent servers for the cluster sample.
 */
public class ClusterAdminAdd {

  public static void main(String[] args) throws Exception {
    
    System.out.println();
    System.out.println("Cluster administration Add ...");

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    
    Properties prop = new Properties();
    prop.setProperty("period",args[0]);
    prop.setProperty("producThreshold",args[1]);
    prop.setProperty("consumThreshold",args[2]);
    prop.setProperty("autoEvalThreshold",args[3]);
    prop.setProperty("waitAfterClusterReq",args[4]);
    System.out.println("prop = " + prop);

    String sid = args[5];
    int id = new Integer(sid).intValue();
    String host = args[6];
    int port = new Integer(args[7]).intValue();

    AdminModule.connect(host, port, "root", "root", 60);

    Queue queue = Queue.create(
      id,
      null,
      "org.objectweb.joram.mom.dest.ClusterQueue",
      prop);
    
    System.out.println("queue = " + queue);

    User user = User.create("user"+sid, "user"+sid, id);


    javax.jms.QueueConnectionFactory cf =
      QueueTcpConnectionFactory.create("localhost", 16010+id);

    ClusterQueue cluster = (ClusterQueue) jndiCtx.lookup("clusterQueue");

    AdminHelper.setQueueCluster(cluster,queue);

    Hashtable h = cluster.getCluster();
    h.put(sid,queue);

    ClusterQueue clusterQueue = new ClusterQueue(h);
    System.out.println("clusterQueue = " + clusterQueue);

    clusterQueue.setReader(user);
    clusterQueue.setWriter(user);


    jndiCtx.bind("qcf"+sid, cf);
    jndiCtx.rebind("clusterQueue",clusterQueue);
    jndiCtx.bind("queue"+sid, queue);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admins closed.");
  }
}
