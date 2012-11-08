/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
package mail;

import java.io.FileInputStream;
import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class MailAdmin {
  public static void main(String[] args) throws Exception {
    System.out.println("mail administration...");

    AdminModule.connect("root", "root", 60);
    Properties prop = new Properties();
    prop.load(new FileInputStream("pop.properties"));
 
    Queue queue = Queue.create(0, null,
                               "com.scalagent.joram.mom.dest.mail.JavaMailQueue",prop);

    prop = new Properties();
    prop.load(new FileInputStream("smtp.properties"));
    Topic topic = Topic.create(0,null,
                               "com.scalagent.joram.mom.dest.mail.JavaMailTopic",prop);

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

    User.create("anonymous", "anonymous", 0);

    queue.setFreeReading();
    queue.setFreeWriting();

    topic.setFreeReading();
    topic.setFreeWriting();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("mailQueue", queue);
    jndiCtx.bind("mailTopic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed, ready to send/recv mail.");
  }
}
