/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package monitoring;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class MonitoringAdmin {
  
  public static void main(String[] args) throws Exception {
    
    System.out.println("Monitoring administration...");

    AdminModule.connect("root", "root", 60);
    
    Queue queue = Queue.create("MonitoredQueue");
    queue.setFreeReading();
    queue.setFreeWriting();
    
    Properties topicProps = new Properties();
    topicProps.put("period", "2000");
    topicProps.put("MBeanMonitoring:Joram#0:type=Destination,name=MonitoredQueue",
        "NbMsgsDeliverSinceCreation, NbMsgsReceiveSinceCreation, PendingMessageCount");
    
    Topic topic = Topic.create(0, "MonitoringTopic", "org.objectweb.joram.mom.dest.MonitoringTopic",
        topicProps);
    topic.setFreeReading();
    topic.setFreeWriting();
    
    User.create("anonymous", "anonymous");

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("MonitoringTopic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
