/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2010 ScalAgent Distributed Technologies
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
package org.ow2.joram.admin;

import org.objectweb.joram.mom.dest.QueueImplMBean;
import org.objectweb.joram.mom.dest.TopicImplMBean;
import org.objectweb.joram.mom.proxies.ClientSubscriptionMBean;
import org.objectweb.joram.mom.proxies.ProxyImplMBean;

public interface AdminListener {

  public void onQueueAdded(String queueName, QueueImplMBean queue);

  public void onQueueRemoved(String queueName, QueueImplMBean queue);

  public void onTopicAdded(String topicName, TopicImplMBean topic);

  public void onTopicRemoved(String topicName, TopicImplMBean topic);

  public void onSubscriptionAdded(String userName, ClientSubscriptionMBean subscription);

  public void onSubscriptionRemoved(String userName, ClientSubscriptionMBean subscription);

  public void onUserAdded(String userName, ProxyImplMBean user);

  public void onUserRemoved(String userName, ProxyImplMBean user);

}
