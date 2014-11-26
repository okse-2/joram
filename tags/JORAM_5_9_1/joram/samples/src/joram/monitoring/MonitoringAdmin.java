/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2010 ScalAgent Distributed Technologies
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

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.MonitoringQueue;
import org.objectweb.joram.client.jms.admin.MonitoringTopic;

/**
 * Creates 2 monitoring destinations: a queue and topic.
 * This code needs a previous administration phase creating at least
 * a connection factory.
 */
public class MonitoringAdmin {
  
  public static void main(String[] args) throws Exception {
    System.out.println("Monitoring administration...");

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    ConnectionFactory cf = (ConnectionFactory) jndiCtx.lookup("cf");
    
    AdminModule.connect(cf, "root", "root");
    
    Properties topicProps = new Properties();
    topicProps.put("acquisition.period", "5000");
    topicProps.put("Joram#0:type=Destination,name=queue",
                   "NbMsgsDeliverSinceCreation,NbMsgsReceiveSinceCreation,PendingMessageCount,NbMsgsSentToDMQSinceCreation");
    topicProps.put("Joram#0:type=Destination,name=topic",
                   "NbMsgsDeliverSinceCreation,NbMsgsReceiveSinceCreation,NbMsgsSentToDMQSinceCreation");
    
    Topic mTopic = MonitoringTopic.create(0, "MonitoringTopic", topicProps);
    mTopic.setFreeReading();
    mTopic.setFreeWriting();
    jndiCtx.bind("MonitoringTopic", mTopic);
    
    Queue mQueue = MonitoringQueue.create(0, "MonitoringQueue");
    mQueue.setFreeReading();
    mQueue.setFreeWriting();
    jndiCtx.bind("MonitoringQueue", mQueue);

    jndiCtx.close();
    AdminModule.disconnect();
    
    System.out.println("Admin closed.");
  }
}
