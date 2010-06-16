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
package collector;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.shared.messages.Message;

/**
 * Administers an agent server for the URL acquisition samples.
 */
public class CollectorAdmin {
  
  public static void main(String[] args) throws Exception {
    // The collected URL
    String url = "http://www.gnu.org/licenses/lgpl-2.1.txt";

    System.out.println();
    System.out.println("Collector administration...");

    AdminModule.connect("root", "root", 60);

    Properties prop = new Properties();
    prop.setProperty("expiration", "0");
    prop.setProperty("persistent", "true");
    prop.setProperty("acquisition.period", "10000");
    prop.setProperty("acquisition.className", "com.scalagent.joram.mom.dest.collector.URLAcquisition");
    prop.setProperty("collector.url", url);
    prop.setProperty("collector.type", "" + Message.BYTES);
    
    Queue queue = Queue.create(0, "queue", Queue.ACQUISITION_QUEUE, prop);
//    Topic topic = Topic.create(0, "topic", Topic.ACQUISITION_TOPIC, prop);
    
    User.create("anonymous", "anonymous");

    queue.setFreeReading();
    queue.setFreeWriting();
//    topic.setFreeReading();
//    topic.setFreeWriting();

    javax.jms.ConnectionFactory cf =
      TcpConnectionFactory.create("localhost", 16010);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
//    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
