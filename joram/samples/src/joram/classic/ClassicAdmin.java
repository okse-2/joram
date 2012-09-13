/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
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
package classic;

import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers an agent server for the classic samples.
 */
public class ClassicAdmin {

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Classic administration...");

    AdminModule.connect("root", "root", 60);

    Queue queue = Queue.create("queue");
    queue.setFreeReading();
    queue.setFreeWriting();
    Topic topic = Topic.create("topic");
    topic.setFreeReading();
    topic.setFreeWriting();
    
    User.create("anonymous", "anonymous");


    ConnectionFactory cf =
      TcpConnectionFactory.create("localhost", 16010);
    cf.getParameters().addOutInterceptor("classic.Interceptor");
    QueueConnectionFactory qcf =
      TcpConnectionFactory.create("localhost", 16010);
    TopicConnectionFactory tcf =
      TcpConnectionFactory.create("localhost", 16010);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("qcf", qcf);
    jndiCtx.bind("tcf", tcf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
