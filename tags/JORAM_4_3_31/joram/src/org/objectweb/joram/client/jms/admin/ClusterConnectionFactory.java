/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2007 ScalAgent Distributed Technologies
 * Copyright (C) 2007 France Telecom
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

import javax.naming.*;

import javax.jms.JMSException;
import javax.jms.Connection;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Map;
import java.util.Random;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.admin.AdministeredObject;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.joram.shared.JoramTracing;

/**
 * A base class for clustered connection factories.
 */
public class ClusterConnectionFactory extends org.objectweb.joram.client.jms.admin.AdministeredObject implements javax.jms.ConnectionFactory {

  protected Hashtable cluster = null;

  /** 
   * Constructs an empty clustered connection factory.
   */
  public ClusterConnectionFactory() {}

  /** 
   * Adds a connection factory to the cluster.
   * The object will be added with a key equals to the location property. 
   * By default, the location value is set to the hostname of corresponding
   * server.
   * Be careful, the object should be rebind after modification.
   *
   * @param cf the ConnectionFactory
   */
  public void addConnectionFactory(ConnectionFactory cf) {
    String location = System.getProperty("location");
    addConnectionFactory(location, cf);
  }

  /** 
   * Adds a connection factory to the cluster with the specified
   * <code>location</code> key. 
   * Be careful, the object should be rebind after modification.
   *
   * @param location	the location key
   * @param cf 		the ConnectionFactory
   */
  public void addConnectionFactory(String location, ConnectionFactory cf) {
    if (cluster == null) cluster = new Hashtable();

    if (location == null)
      location = cf.getParameters().getHost();
    cluster.put(location, cf);
  }

  /**
   * Chooses a connection factory from the cluster definition.
   */
  protected ConnectionFactory getConnectionFactory() throws JMSException {
    if ((cluster != null) && ! cluster.isEmpty()) {
      ConnectionFactory cf = null;
      String location = System.getProperty("location");
      if (location == null || location.equals("")) {
        int idx = new Random().nextInt(cluster.size());

        Object key[] = cluster.keySet().toArray();
        location = (String) key[idx];
        System.setProperty("location", location);
      }

      cf = (ConnectionFactory) cluster.get(location);
      if (cf == null) {
        Enumeration e = cluster.elements();
        cf = (ConnectionFactory) e.nextElement();
      }
      return cf;
    }

    return null;
  }

  /**
   * Creates a connection with the default user identity.
   * It chooses a <code>ConnectionFactory</code> depending of the
   * location property, then creates the related <code>Connection</code>. 
   *
   * API method, see javax.jms.ConnectionFactory.
   *
   * @exception JMSSecurityException  If the default identification is
   *              incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public final Connection createConnection() throws JMSException {
    ConnectionFactory cf = getConnectionFactory();
    return cf.createConnection();
  }

  /**
   * Creates a connection with the specified user identity.
   * It chooses a <code>ConnectionFactory</code> depending of the
   * location property, then creates the related <code>Connection</code>. 
   *
   * API method, see javax.jms.ConnectionFactory.
   *
   * @param name	the caller's user name
   * @param password	the caller's password
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public final Connection createConnection(String name,
                                           String password) throws JMSException {
    ConnectionFactory cf = getConnectionFactory();
    return cf.createConnection(name, password);
  }

  /** Returns a String image of the object. */
  public String toString() {
    return "ClusterConnectionFactory:" + cluster;
  }

  /** Sets the naming reference of an administered object. */
  public void toReference(Reference ref) throws NamingException {
    Map.Entry entries[] = new Map.Entry [cluster.size()];
    cluster.entrySet().toArray(entries);

    StringBuffer strbuf = new StringBuffer(15);
    for (int i=0; i<entries.length; i++) {
      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".key");
      ref.add(new StringRefAddr(strbuf.toString(),
                                (String) entries[i].getKey()));

      ConnectionFactory cf = (ConnectionFactory) entries[i].getValue();

      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".class");
      ref.add(new StringRefAddr(strbuf.toString(), cf.getClass().getName()));

      strbuf.setLength(0);
      strbuf.append("CF#").append(i);
      cf.toReference(ref, strbuf.toString());
    }
  }

  /** Restores the administered object from a naming reference. */
  public void fromReference(Reference ref) throws NamingException {
    if (cluster == null) cluster = new Hashtable();

    int i = 0;
    StringBuffer strbuf = new StringBuffer(15);

    while (true) {
      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".key");
      RefAddr refAddr = ref.get(strbuf.toString());
      if (refAddr == null) break;
      String key = (String) refAddr.getContent();

      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".class");
      String classname = (String) ref.get(strbuf.toString()).getContent();
      try {
        Class clazz = Class.forName(classname);
        ConnectionFactory cf = (ConnectionFactory) clazz.newInstance();
        strbuf.setLength(0);
        strbuf.append("CF#").append(i);
        cf.fromReference(ref, strbuf.toString());

        cluster.put(key, cf);
      } catch (Exception exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
          JoramTracing.dbgClient.log(BasicLevel.ERROR, "", exc);
      }
      i++;
    }
  }

  // AF: The JNDI Soap access should translate the Reference object to
  // an hashtable.

  /**
   * Codes a <code>ConnectionFactory</code> as a Hashtable for travelling
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();

    Map.Entry entries[] = new Map.Entry [cluster.size()];
    cluster.entrySet().toArray(entries);

    StringBuffer strbuf = new StringBuffer(15);
    for (int i=0; i<entries.length; i++) {
      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".key");
      h.put(strbuf.toString(), (String) entries[i].getKey());

      ConnectionFactory cf = (ConnectionFactory) entries[i].getValue();

      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".class");
      h.put(strbuf.toString(), cf.getClass().getName());

      strbuf.setLength(0);
      strbuf.append("CF#").append(i);
      cf.code(h, strbuf.toString());
    }

    return h;
  }

  /**
   * Implements the <code>decode</code> abstract method defined in the
   * <code>fr.dyade.aaa.jndi2.soap.SoapObjectItf</code> interface.
   * <p>
   * Actual implementation of the method is located in the
   * tcp and soap sub classes.
   */
  public void decode(Hashtable h) {
    if (cluster == null) cluster = new Hashtable();

    int i = 0;
    StringBuffer strbuf = new StringBuffer(15);

    while (true) {
      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".key");
      String key = (String) h.get(strbuf.toString());
      if (key == null) break;

      strbuf.setLength(0);
      strbuf.append("CF#").append(i).append(".class");
      String classname = (String) h.get(strbuf.toString());
      try {
        Class clazz = Class.forName(classname);
        ConnectionFactory cf = (ConnectionFactory) clazz.newInstance();

        strbuf.setLength(0);
        strbuf.append("CF#").append(i);
        cf.decode(h, strbuf.toString());

        cluster.put(key, cf);
      } catch (Exception exc) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.ERROR))
          JoramTracing.dbgClient.log(BasicLevel.ERROR, "", exc);
      }
      i++;
    }
  }
}
