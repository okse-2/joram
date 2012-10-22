/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package httpnet;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;


/**
 * Administer an Joram configuration with 2 servers connected through HTTP:
 *  - Creates a User with corresponding ConnectionFactory on local server.
 *  - Creates queue and topic on remote server.
 * Use 'classic' sample producer and consumer.
 */
public class HttpAdmin {

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Classic administration...");

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

    AdminModule.connect(cf, "root", "root");

    Queue queue = Queue.create(1, "queue");
    Topic topic = Topic.create(1, "topic");
    
    User.create("anonymous", "anonymous", 0);

    queue.setFreeReading();
    queue.setFreeWriting();
    topic.setFreeReading();
    topic.setFreeWriting();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
