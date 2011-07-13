/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2011 ScalAgent Distributed Technologies
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

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

public class MailAdmin {
  public static void main(String[] args) throws Exception {
    System.out.println("mail administration...");

    AdminModule.connect("root", "root", 60);

    // Create a topic forwarding its messages to the configured email address.
    Properties prop = new Properties();
    prop.load(new FileInputStream("smtp.properties"));
    prop.put("distribution.className", "com.scalagent.joram.mom.dest.mail.MailDistribution");
    Topic topic = Topic.create(0, null, Destination.DISTRIBUTION_TOPIC, prop);

    // Create a queue getting its messages from the configured email address.
    prop = new Properties();
    prop.load(new FileInputStream("pop.properties"));
    prop.put("acquisition.className", "com.scalagent.joram.mom.dest.mail.MailAcquisition");
    Queue queue = Queue.create(0, null, Destination.ACQUISITION_QUEUE, prop);

    javax.jms.ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

    User.create("anonymous", "anonymous", 0);

    topic.setFreeWriting();

    queue.setFreeReading();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf", cf);
    jndiCtx.bind("receiveMailQueue", queue);
    jndiCtx.bind("sendMailTopic", topic);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed, ready to send/recv mail.");
  }
}
