/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * A <code>Monitor_GetDestinationsRep</code> instance replies to a get
 * destinations monitoring request, and holds the destinations on a given
 * server.
 */
public class Monitor_GetDestinationsRep extends Monitor_Reply
{
  /** Queues identifiers. */
  private Vector queues;
  /** Dead message queues identifiers. */
  private Vector dmqs;
  /** Topics identifiers. */
  private Vector topics;

  /** Destinations' names, if any. */
  private Hashtable names;



  /** Adds a queue identifier to the reply. */
  public void addQueue(String id)
  {
    if (queues == null)
      queues = new Vector();
    queues.add(id);
  }

  /** Adds a dead message queue identifier to the reply. */
  public void addDeadMQueue(String id)
  {
    if (dmqs == null)
      dmqs = new Vector();
    dmqs.add(id);
  }

  /** Adds a topic identifier to the reply. */
  public void addTopic(String id)
  {
    if (topics == null)
      topics = new Vector();
    topics.add(id);
  }

  /** Sets destinations' names, if any. */
  public void setNames(Hashtable names)
  {
    if (names == null)
      return;

    this.names = new Hashtable();
    Enumeration keys = names.keys();
    Object key;
    Object value;
    while (keys.hasMoreElements()) {
      key = keys.nextElement();
      value = names.get(key);
      this.names.put(value.toString(), key);
    }
  }
  
  /** Returns the queues identifiers. */
  public Vector getQueues()
  {
    return queues;
  }

  /** Returns the dmqs identifiers. */
  public Vector getDeadMQueues()
  {
    return dmqs;
  } 

  /** Returns the topics identifiers. */
  public Vector getTopics()
  {
    return topics;
  }

  /** Returns a destination name. */
  public String getName(String id)
  {
    if (names == null)
      return null;
    return (String) names.get(id);
  }
}
