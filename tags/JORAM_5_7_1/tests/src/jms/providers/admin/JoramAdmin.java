/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2002 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2002 INRIA
 * Contact: joram-team@objectweb.org
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
 * Initial developer(s): Jeff Mesnil (Inria)
 * Contributor(s): Nicolas Tachker (ScalAgent D.T.)
 */

package jms.providers.admin;

import java.net.ConnectException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import jms.admin.Admin;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;

public class JoramAdmin implements Admin {
  private String name = "JORAM";

  public JoramAdmin() {
    try {
      AdminModule.connect("root", "root", 30);
      User.create("anonymous", "anonymous");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getName() {
    return name;
  }

  public ConnectionFactory createConnectionFactory(String name) throws ConnectException {
    return TcpConnectionFactory.create();
  }

  public QueueConnectionFactory createQueueConnectionFactory(String name) throws ConnectException {
    return QueueTcpConnectionFactory.create();
  }

  public TopicConnectionFactory createTopicConnectionFactory(String name) throws ConnectException {
    return TopicTcpConnectionFactory.create();
  }

  public Queue createQueue(String name) throws ConnectException, AdminException {
    Queue queue = null;
    queue = Queue.create(name);
    queue.setFreeWriting();
    queue.setFreeReading();
    return queue;
  }

  public Topic createTopic(String name) throws ConnectException, AdminException {
    Topic topic = Topic.create(name);
    topic.setFreeWriting();
    topic.setFreeReading();
    return topic;
  }

  public void deleteQueue(javax.jms.Destination queue) throws ConnectException, AdminException, JMSException {
    ((org.objectweb.joram.client.jms.Destination)queue).delete();
  }

  public void deleteTopic(javax.jms.Destination topic) throws ConnectException, AdminException, JMSException {
    ((org.objectweb.joram.client.jms.Destination)topic).delete();
  }

  public void deleteConnectionFactory(String name) {
  }

  public void deleteTopicConnectionFactory(String name) {
  }

  public void deleteQueueConnectionFactory(String name) {
  }

  public void disconnect() {
    AdminModule.disconnect();
  }
}
