/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2010 ScalAgent Distributed Technologies
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
package bridge;

import java.util.Properties;

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;


/**
 * Administers an agent server for the bridge sample.
 */
public class BridgeAdmin {
  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Bridge administration...");

    ConnectionFactory bridgeCF = TcpConnectionFactory.create("localhost", 16011);

    AdminModule.connect(bridgeCF, "root", "root");
    
    User.create("anonymous", "anonymous");

    // Setting the bridge queue properties
    Properties prop = new Properties();
    prop.setProperty("jndiFactory", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    prop.setProperty("jndiUrl", "scn://localhost:16400");
    prop.setProperty("connectionFactoryName", "cf");  // Foreign QueueConnectionFactory JNDI name
    prop.setProperty("destinationName", "queue");     // Foreign Queue JNDI name: foreignDest
    prop.setProperty("distribution.className", "org.objectweb.joram.mom.dest.jms.JMSDistribution");

    // Creating a Queue bridge on server 0:
    Queue bridgeQueue = Queue.create(1, Queue.DISTRIBUTION_QUEUE, prop);
    bridgeQueue.setFreeReading();
    bridgeQueue.setFreeWriting();
    System.out.println("joram queue = " + bridgeQueue);
    
    // Setting the bridge topic properties
    prop = new Properties();
    prop.setProperty("jndiFactory", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    prop.setProperty("jndiUrl", "scn://localhost:16400");
    prop.setProperty("connectionFactoryName", "cf"); // Foreign QueueConnectionFactory JNDI name
    prop.setProperty("destinationName", "topic"); // Foreign Queue JNDI name: foreignDest
    prop.setProperty("acquisition.className", "org.objectweb.joram.mom.dest.jms.JMSAcquisition");

    // Creating a Topic bridge on server 0:
    Topic bridgeTopic = Topic.create(1, Topic.ACQUISITION_TOPIC, prop);
    bridgeTopic.setFreeReading();
    bridgeTopic.setFreeWriting();
    System.out.println("joram topic = " + bridgeTopic);
    
    // bind foreign destination and connectionFactory
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.rebind("bridgeQueue", bridgeQueue);
    jndiCtx.rebind("bridgeTopic", bridgeTopic);
    jndiCtx.rebind("bridgeCF", bridgeCF);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
