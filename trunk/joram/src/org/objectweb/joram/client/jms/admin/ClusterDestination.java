/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package org.objectweb.joram.client.jms.admin;

import org.objectweb.joram.client.jms.Destination;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.*;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.client.jms.JoramTracing;

/**
 * 
 */
public abstract class ClusterDestination extends Destination {

  protected Hashtable cluster;

  /**
   * Constructs a cluster destination.
   *
   * @param cluster  Hashtable of the cluster agent destination.
   */ 
  public ClusterDestination(Hashtable cluster) {
    init(cluster);
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, 
                                 this + ": cluster = " + cluster);
  }

  /**
   * Constructs an empty cluster destination.
   */ 
  public ClusterDestination() {}

  public void init(Hashtable cluster) {
    this.cluster = cluster;
    if (cluster != null) {
      id = this.getClass().getName() + ":" + cluster.toString();
      instancesTable.put(id,this);
    }
  }

  /** return the appropriate destination of cluster */
  public abstract Destination getDestination(); 

  /** Returns the name of the destination. */
  public String getName() {
    return getDestination().getName();
  }

  public Hashtable getCluster() {
    return cluster;
  }

  /** Sets the naming reference of a destination. */
  public Reference getReference() throws NamingException {
    Reference ref = 
      new Reference(this.getClass().getName(),
                    "org.objectweb.joram.client.jms.admin.ObjectFactory",
                    null);
    ref.add(new StringRefAddr("adminObj.id", id));
    int i = 0;
    for (Enumeration e = cluster.keys(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      ref.add(new StringRefAddr("cluster.key"+i, key));
      ref.add(new StringRefAddr("cluster.destName"+i, 
                                ((Destination) cluster.get(key)).getName()));
      i++;
    }
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, 
                                 "ClusterDestination : ref = " +ref);
    return ref;
  }

  /**
   * Returns <code>true</code> if the parameter object is a Joram
   * cluster destination wrapping the same agent identifier.
   */
  public boolean equals(Object obj)
  {
    if (! (obj instanceof org.objectweb.joram.client.jms.admin.ClusterDestination))
      return false;

    return (getName().equals(
      ((org.objectweb.joram.client.jms.admin.ClusterDestination) obj).getName()));
  }


  /**
   * Codes a <code>ClusterDestination</code> as a Hashtable for
   * travelling through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = super.code();
    h.put("cluster",cluster);
    return h;
  }
}
