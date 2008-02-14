/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - 2007 France Telecom R&D
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
package org.objectweb.joram.client.jms.admin;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.shared.admin.*;

import java.net.ConnectException;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;

import javax.jms.JMSException;

/**
 * this is a sample.
 */
public class ClusterQueue extends ClusterDestination implements javax.jms.Queue {
  /** 
   * Constructs an empty queue.
   */
  public ClusterQueue() {}

  /** 
   * Constructs a cluster queue.
   *
   * @param cluster  Hashtable of the cluster agent destination.
   */
  public ClusterQueue(Hashtable cluster) {
    super(cluster);
  }

  /** Returns a String image of the cluster queue. */
  public String toString() {
    return "ClusterQueue:" + cluster;
  }

  /**
   * API method.
   *
   * @exception JMSException  Actually never thrown.
   */
  public String getQueueName() throws JMSException {
    return getName();
  }
}
