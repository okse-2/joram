/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package fr.dyade.aaa.mom.dest;

import fr.dyade.aaa.agent.management.MXWrapper;


/**
 * An <code>AdminTopic</code> agent is a MOM administration service, which
 * behaviour is provided by an <code>AdminTopicImpl</code> instance.
 *
 * @see AdminTopicImpl
 */
public class AdminTopic extends Topic
{
  /**
   * Constructs an <code>AdminTopic</code> agent. 
   */ 
  public AdminTopic()
  {
    super(true);
    topicImpl = new AdminTopicImpl(getId());
  }


  /**
   * Initializes the <code>AdminTopic</code> service.
   *
   * @exception java.io.IOException  If the deployment of the topic fails.
   */
  public static void init(String args, boolean firstTime)
                     throws java.io.IOException
  {
    if (! firstTime)
      return;

    // First initialization: deploying the topic, initializing it.
    AdminTopic adminTopic = new AdminTopic();
    adminTopic.deploy();
    AdminTopicImpl.initService(adminTopic.getId());
  }

  /**
   * Stops the <code>AdminTopic</code> service.
   */ 
  public static void stopService()
  {}

  /** (Re)initializes the agent when (re)loading. */
  public void initialize(boolean firstTime) throws Exception
  {
    super.initialize(firstTime);
    MXWrapper.unregisterMBean("JORAM destinations",
                              getId().toString(),
                              "Topic",
                              null);
    MXWrapper.registerMBean(topicImpl,
                            "JORAM server",
                            getId().toString(),
                            "AdminTopic",
                            null);
  }

  /** Finalizes the agent before it is garbaged. */
  public void agentFinalize()
  {
    try {
      MXWrapper.unregisterMBean("JORAM server",
                                getId().toString(),
                                "AdminTopic",
                                null);
    }
    catch (Exception exc) {}
  }
}
