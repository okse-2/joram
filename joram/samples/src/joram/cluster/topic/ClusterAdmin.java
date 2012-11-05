/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package cluster.topic;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.ClusterConnectionFactory;
import org.objectweb.joram.client.jms.admin.ClusterTopic;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers three agent servers for the cluster of topics sample.
 */
public class ClusterAdmin {
  
  public static void main(String[] args) throws Exception {
    
    System.out.println();
    System.out.println("Cluster of topics administration...");

    AdminModule.connect("root", "root", 60);
    javax.naming.Context ictx = new javax.naming.InitialContext();

    User.create("anonymous", "anonymous", 0);
    User.create("anonymous", "anonymous", 1);
    User.create("anonymous", "anonymous", 2); 

    ConnectionFactory cf0 = (ConnectionFactory) TcpConnectionFactory.create("localhost", 16010);
    ConnectionFactory cf1 = (ConnectionFactory) TcpConnectionFactory.create("localhost", 16011);
    ConnectionFactory cf2 = (ConnectionFactory) TcpConnectionFactory.create("localhost", 16012);

    ictx.bind("cf0", cf0);
    ictx.bind("cf1", cf1);
    ictx.bind("cf2", cf2);

    ClusterConnectionFactory clusterCF = new ClusterConnectionFactory();
    clusterCF.addConnectionFactory("server0", cf0);
    clusterCF.addConnectionFactory("server1", cf1);
    clusterCF.addConnectionFactory("server2", cf2);
    ictx.rebind("clusterCF", clusterCF);

    Topic topic0 = Topic.create(0);
    Topic topic1 = Topic.create(1);
    Topic topic2 = Topic.create(2);
    
    System.out.println("topic0 = " + topic0);
    System.out.println("topic1 = " + topic1);
    System.out.println("topic2 = " + topic2);

    topic0.addClusteredTopic(topic1);
    topic0.addClusteredTopic(topic2);
    
    ictx.bind("topic0", topic0);
    ictx.bind("topic1", topic1);
    ictx.bind("topic2", topic2);

    ClusterTopic clusterTopic = new ClusterTopic();
    clusterTopic.addDestination("server0", topic0);
    clusterTopic.addDestination("server1", topic1);
    clusterTopic.addDestination("server2", topic2);
    clusterTopic.setFreeReading();
    clusterTopic.setFreeWriting();
    ictx.rebind("clusterTopic", clusterTopic);

    System.out.println("clusterTopic = " + clusterTopic);

    ictx.close();
    AdminModule.disconnect();

    System.out.println("Admin closed.");
  }
}
