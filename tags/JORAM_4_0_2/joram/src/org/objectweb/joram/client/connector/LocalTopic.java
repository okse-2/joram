/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package org.objectweb.joram.client.connector;

import org.objectweb.joram.client.jms.Topic;


/**
 * The <code>LocalTopic</code> class allows to manage a topic hosted by the
 * adapter's underlying server.
 */
public class LocalTopic implements LocalTopicMBean
{
  /** Wrapped topic. */
  private Topic topic;


  /**
   * Constructs a <code>LocalTopic</code> instance.
   */
  public LocalTopic(Topic topic)
  {
    this.topic = topic;
  }


  public String getAgentId()
  {
    try {
      return topic.getTopicName();
    }
    catch (Exception exc) {
      return null;
    }
  }

  public String getJndiName()
  {
    return topic.getAdminName();
  }

  public int getNumberOfSubscribers()
  {
    try {
      return topic.getSubscriptions();
    }
    catch (Exception exc) {
      return -1;
    }
  }


  public void delete() throws Exception
  {
    topic.delete();
    JoramAdapter.unregister(this);
  }
}
