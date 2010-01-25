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
package jaas;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;


/**
 * Administers an agent server for the jaas samples.
 */
public class ClassicAdmin {
  
  public static void main(String[] args) throws Exception {
    
    System.out.println();
    System.out.println("Classic administration...");

    //AdminModule.connect("org.objectweb.joram.shared.security.jaas.JonasIdentity:root", "root", 60);
    AdminModule.connect(
        "localhost", 16010, 
        "root", "root", 60, 
        "org.objectweb.joram.client.jms.tcp.ReliableTcpClient", 
        "org.objectweb.joram.shared.security.jaas.JonasIdentity");

    Queue queue = Queue.create("queue");
    Topic topic = Topic.create("topic");
    
    User user = User.create("anonymous", null, 0, "org.objectweb.joram.shared.security.jaas.JonasIdentity");

    queue.setReader(user);
    topic.setReader(user);
    queue.setWriter(user);
    topic.setWriter(user);

    javax.jms.ConnectionFactory cf =
      TcpConnectionFactory.create("localhost", 16010);
    ((org.objectweb.joram.client.jms.admin.AbstractConnectionFactory) cf).setIdentityClassName("org.objectweb.joram.shared.security.jaas.JonasIdentity");
    javax.jms.QueueConnectionFactory qcf =
      QueueTcpConnectionFactory.create("localhost", 16010);
    ((org.objectweb.joram.client.jms.admin.AbstractConnectionFactory) qcf).setIdentityClassName("org.objectweb.joram.shared.security.jaas.JonasIdentity");
    javax.jms.TopicConnectionFactory tcf =
      TopicTcpConnectionFactory.create("localhost", 16010);
    ((org.objectweb.joram.client.jms.admin.AbstractConnectionFactory) tcf).setIdentityClassName("org.objectweb.joram.shared.security.jaas.JonasIdentity");


    Properties env = new Properties();
    env.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
    env.put("java.naming.factory.host", "localhost");
    env.put("java.naming.factory.port", "16400");
    
    javax.naming.Context jndiCtx = new javax.naming.InitialContext(env);
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
