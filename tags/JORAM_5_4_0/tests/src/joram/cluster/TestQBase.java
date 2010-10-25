/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2009 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.cluster;

import java.util.Properties;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.ClusterConnectionFactory;
import org.objectweb.joram.client.jms.admin.ClusterQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import framework.TestCase;

/**
 *   
 */
public class TestQBase extends TestCase {
  public void adminXML() throws Exception {
    AdminModule.executeXMLAdmin("joramAdmin.xml");
  }
  
  /**
   * Creates a clustered queue and an user on each server.
   */
  public void admin(Properties prop) throws Exception {
    AdminModule.connect("root", "root", 60);
    javax.naming.Context ictx = new javax.naming.InitialContext();

    // Creates an user on each server
    User.create("user", "pass", 0);
    User.create("user", "pass", 1);
    User.create("user", "pass", 2);

    ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 16010);
    ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 16011);
    ConnectionFactory cf2 = TcpConnectionFactory.create("localhost", 16012);

    ictx.bind("cf0", cf0);
    ictx.bind("cf1", cf1);
    ictx.bind("cf2", cf2);

    ClusterConnectionFactory clusterCF = new ClusterConnectionFactory();
    clusterCF.addConnectionFactory("server0", cf0);
    clusterCF.addConnectionFactory("server1", cf1);
    clusterCF.addConnectionFactory("server2", cf2);
    
    ictx.rebind("clusterCF", clusterCF);

    // Creates a queue on each server
    Queue queue0 = Queue.create(0, null, Queue.CLUSTER_QUEUE, prop);
    Queue queue1 = Queue.create(1, null, Queue.CLUSTER_QUEUE, prop);
    Queue queue2 = Queue.create(2, null, Queue.CLUSTER_QUEUE, prop);

    ictx.bind("queue0", queue0);
    ictx.bind("queue1", queue1);
    ictx.bind("queue2", queue2);

    // Builds the clustered queue
    queue0.addClusteredQueue(queue1);
    queue0.addClusteredQueue(queue2);

    ClusterQueue clusterQueue = new ClusterQueue();
    clusterQueue.addDestination("server0", queue0);
    clusterQueue.addDestination("server1", queue1);
    clusterQueue.addDestination("server2", queue2);
    clusterQueue.setFreeReading();
    clusterQueue.setFreeWriting();

    ictx.bind("clusterQueue", clusterQueue);
    
    ictx.close();

    AdminModule.disconnect();
  }
}
