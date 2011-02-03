/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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

import java.io.IOException;


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
   * @exception IOException  If the topic deployment fails.
   */
  public static void init(String args, boolean firstTime) throws IOException
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
}