/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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

import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;

/**
 * A base class for clustered destinations.
 */
public class ClusterDestination extends Destination {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  private static Logger logger = Debug.getLogger(ClusterDestination.class.getName());
  
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
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": cluster = " + cluster);
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
  
  /**
   * Returns the type of the destination: queue or topic, temporary or not.
   */
  public byte getType() {
    return getDestination().getType();
  }

  public void setReader(User user) throws ConnectException, AdminException {
    for (Enumeration dests = cluster.elements(); dests.hasMoreElements();) {
      Destination dest = (Destination) dests.nextElement(); 
      getWrapper().doRequest(new SetReader(user.getProxyId(), dest.getName()));
    }
  }

  public void setWriter(User user) throws ConnectException, AdminException {
    for (Enumeration dests = cluster.elements(); dests.hasMoreElements();) {
      Destination dest = (Destination) dests.nextElement(); 
      getWrapper().doRequest(new SetWriter(user.getProxyId(), dest.getName()));
    }
  }
  
  public void setFreeReading() throws ConnectException, AdminException {
    for (Enumeration dests = cluster.elements(); dests.hasMoreElements();) {
      Destination dest = (Destination) dests.nextElement(); 
      getWrapper().doRequest(new SetReader(null, dest.getName()));
    }
  }

  public void setFreeWriting() throws ConnectException, AdminException {
    for (Enumeration dests = cluster.elements(); dests.hasMoreElements();) {
      Destination dest = (Destination) dests.nextElement(); 
      getWrapper().doRequest(new SetWriter(null, dest.getName()));
    }
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
    StringBuffer strbuf = new StringBuffer(20);

    for (int i=0; i<entries.length; i++) {
      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".key");
      ref.add(new StringRefAddr(strbuf.toString(),
                                (String) entries[i].getKey()));
      Destination dest = (Destination) entries[i].getValue();

      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".destName");
      ref.add(new StringRefAddr(strbuf.toString(), dest.getName()));
    }
  }

  /** Restores the administered object from a naming reference. */
  public void fromReference(Reference ref) throws NamingException {
    cluster = new Hashtable();
    int i = 0;
    Destination dest = null;
    StringBuffer strbuf = new StringBuffer(20);

    while (true) {
      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".key");
      RefAddr refAddr = ref.get(strbuf.toString());
      if (refAddr == null) break;

      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".destName");
      if (isQueue()) {
        dest = new Queue((String) ref.get(strbuf.toString()).getContent());
      } else {
        dest = new Topic((String) ref.get(strbuf.toString()).getContent());
      }
      cluster.put((String) refAddr.getContent(), dest);
      i++;
    }
  }

  /**
   * Codes a <code>ClusterDestination</code> as a Hashtable for
   * travelling through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();

    Map.Entry entries[] = new Map.Entry [cluster.size()];
    cluster.entrySet().toArray(entries);
    StringBuffer strbuf = new StringBuffer(20);

    for (int i=0; i<entries.length; i++) {
      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".key");
      h.put(strbuf.toString(), (String) entries[i].getKey());

      Destination dest = (Destination) entries[i].getValue();

      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".destName");
      h.put(strbuf.toString(), dest.getName());
    }

    return h;
  }

  public void decode(Hashtable h) {
    cluster = new Hashtable();

    int i = 0;
    Destination dest = null;
    StringBuffer strbuf = new StringBuffer(20);

    while (true) {
      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".key");
      String key = (String) h.get(strbuf.toString());
      if (key == null) break;

      strbuf.setLength(0);
      strbuf.append("cluster#").append(i).append(".destName");
      if (isQueue()) {
        dest = new Queue((String) h.get(strbuf.toString()));
      } else {
        dest = new Topic((String) h.get(strbuf.toString()));
      }
      cluster.put(key, dest);
      i++;
    }

  }
}
