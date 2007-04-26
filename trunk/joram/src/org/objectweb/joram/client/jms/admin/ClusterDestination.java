/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - 2007 France Telecom R&D
 * Copyright (C) 1996 - 2000 Dyade
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
import org.objectweb.joram.client.jms.Topic;

import javax.naming.*;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.shared.JoramTracing;

/**
 * A base class for clustered destinations.
 */
public class ClusterDestination extends Destination {

  protected Hashtable cluster = null;

  /**
   * Constructs an empty clustered destination.
   */ 
  public ClusterDestination() {}

  /**
   * Constructs a cluster destination.
   *
   * @param cluster  Hashtable of the cluster agent destination.
   */ 
  public ClusterDestination(Hashtable cluster) {
    this.cluster = cluster;
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, 
                                 this + ": cluster = " + cluster);
  }

  public void setCluster(Hashtable cluster) {
    this.cluster = cluster;
  }

  public Hashtable getCluster() {
    return cluster;
  }

  /** 
   * Adds a destination to the cluster.
   * The object will be added with a key equals to the location property. 
   * Be careful, the object should be rebind after modification.
   *
   * @param dest the Destination
   */
  public void addDestination(Destination dest) {
    String location = System.getProperty("location");
    addDestination(location, dest);
  }

  /** 
   * Adds a destination to the cluster with the specified
   * <code>location</code> key.
   * By default, the location value is set to the String server#i, where i is
   * the server id. of the destination. 
   * Be careful, the object should be rebind after modification.
   *
   * @param location	the location key
   * @param dest 	the Destination
   */
  public void addDestination(String location, Destination dest) {
    if (cluster == null) cluster = new Hashtable();

    if (location == null) {
      String destname = dest.getName();
      location = "server" + destname.substring(0, destname.indexOf('.'));
    }
    cluster.put(location, dest);
  }

  /** return the appropriate destination of cluster */
  protected Destination getDestination() {
    if ((cluster != null) && ! cluster.isEmpty()) {
      Destination dest = null;
      String location = System.getProperty("location");
      if (location == null || location.equals("")) {
        int idx = new Random().nextInt(cluster.size());

        Object key[] = cluster.keySet().toArray();
        location = (String) key[idx];
        System.setProperty("location", location);
      }

      dest = (Destination) cluster.get(location);
      if (dest == null) {
        Enumeration e = cluster.elements();
        dest = (Destination) e.nextElement();
      }
      return dest;
    }

    return null;
  }

  /** Returns the name of the destination. */
  public String getName() {
    return getDestination().getName();
  }

//   /**
//    * Returns <code>true</code> if the parameter object is a Joram
//    * cluster destination wrapping the same agent identifier.
//    */
//   public boolean equals(Object obj) {
//     if (! (obj instanceof org.objectweb.joram.client.jms.admin.ClusterDestination))
//       return false;

//     return (getName().equals(
//       ((org.objectweb.joram.client.jms.admin.ClusterDestination) obj).getName()));
//   }

  /** Sets the naming reference of an administered object. */
  public void toReference(Reference ref) throws NamingException {
    Map.Entry entries[] = new Map.Entry [cluster.size()];
    cluster.entrySet().toArray(entries);

    for (int i=0; i<entries.length; i++) {
      ref.add(new StringRefAddr("cluster#" + i + ".key",
                                (String) entries[i].getKey()));
      Destination dest = (Destination) entries[i].getValue();
      ref.add(new StringRefAddr("cluster#" + i + ".destName",
                                dest.getName()));
    }
  }

  /** Restores the administered object from a naming reference. */
  public void fromReference(Reference ref) throws NamingException {
    cluster = new Hashtable();
    int i = 0;
    if (isQueue()) {
      while (true) {
        RefAddr refAddr = ref.get("cluster#" + i + ".key");
        if (refAddr == null) break;
        cluster.put((String) refAddr.getContent(),
                    new Queue((String) ref.get("cluster#" + i + ".destName").getContent()));
        i++;
      }
    } else {
      while (true) {
        RefAddr refAddr = ref.get("cluster#" + i + ".key");
        if (refAddr == null) break;
        cluster.put((String) refAddr.getContent(),
                    new Topic((String) ref.get("cluster#" + i + ".destName").getContent()));
        i++;
      }
    }
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

  public void decode(Hashtable h) {
    cluster = (Hashtable) h.get("cluster");
  }
}
