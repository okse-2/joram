/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Vector;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.AdministeredObject;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.admin.XmlSerializer;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.admin.CreateDestinationReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.DeleteDestination;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettings;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettingsRep;
import org.objectweb.joram.shared.admin.Monitor_GetFreeAccess;
import org.objectweb.joram.shared.admin.Monitor_GetFreeAccessRep;
import org.objectweb.joram.shared.admin.Monitor_GetReaders;
import org.objectweb.joram.shared.admin.Monitor_GetStat;
import org.objectweb.joram.shared.admin.Monitor_GetStatRep;
import org.objectweb.joram.shared.admin.Monitor_GetUsersRep;
import org.objectweb.joram.shared.admin.Monitor_GetWriters;
import org.objectweb.joram.shared.admin.SetDestinationDMQ;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.admin.UnsetDestinationDMQ;
import org.objectweb.joram.shared.admin.UnsetReader;
import org.objectweb.joram.shared.admin.UnsetWriter;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Implements the <code>javax.jms.Destination</code> interface and provides
 * JORAM specific administration and monitoring methods.
 */
public abstract class Destination extends AdministeredObject implements javax.jms.Destination, DestinationMBean {
  /** Identifier of the agent destination. */
  protected String agentId;

  /** Name given by the administrator. */
  protected String adminName;

  private String type;

  // Used by jndi2 SoapObjectHelper
  public Destination() {}

  public Destination(String type) {
    this.type = type;
  }

  protected Destination(String name, String type) {
    agentId = name;
    this.type = type;
  }

  /** Returns the name of the destination. */
  public String getName() {
    return agentId;
  }

  /** Returns the administration name of the destination. */
  public final String getAdminName() {
    return adminName;
  }

  public final String getType() {
    return type;
  }

  /**
   * Returns <code>true</code> if the parameter object is a Joram destination
   * wrapping the same agent identifier.
   */
  public boolean equals(Object obj) {
    if (! (obj instanceof Destination))
      return false;

    return (getName().equals(((Destination) obj).getName()));
  }

  /**
   * Returns a String image of the queue.
   *
   * @return A provider-specific identity values for this queue.
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(type).append(agentId);
    if (adminName != null)
      strbuf.append('(').append(adminName).append(')');
    return strbuf.toString();
  }

  /**
   * Format the destination properties in a XML format
   * @param indent use this indent for prexifing XML representation.
   * @param serverId server id hosting the destination object
   * @return returns a XML view of the queue (administration format)
   * @throws ConnectException if the server is unreachable
   * @throws AdminException if an error occurs
   */
  public String toXml(int indent, int serverId)  throws ConnectException, AdminException {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(XmlSerializer.indent(indent));

    if (getType().equals("queue")) {
      strbuf.append("<Queue ");
    } else if (getType().equals("topic")) {
      strbuf.append("<Topic ");
    } else {
      return "";
    }
    strbuf.append(XmlSerializer.xmlAttribute(getAdminName(), "name"));
    strbuf.append(XmlSerializer.xmlAttribute(String.valueOf(serverId), "serverId"));
    Queue dmq = getDMQ();
    if (dmq != null) {
      strbuf.append(XmlSerializer.xmlAttribute(dmq.getAdminName(), "dmq"));
      strbuf.append(XmlSerializer.xmlAttribute(String.valueOf(dmq.getThreshold()), "threshold"));
    }

    strbuf.append(">\n");

    indent+=2;

    if (isFreelyReadable()) {
      strbuf.append(XmlSerializer.indent(indent));
      strbuf.append("<freeReader/>\n");
    }

    if (isFreelyWriteable()) {
      strbuf.append(XmlSerializer.indent(indent));
      strbuf.append("<freeWriter/>\n");
    }

    List readers = getReaders();
    for (ListIterator iterator = readers.listIterator(); iterator.hasNext(); ) {
        User user = (User) (iterator.next());
        strbuf.append(XmlSerializer.indent(indent));
        strbuf.append("<reader ");
        strbuf.append(XmlSerializer.xmlAttribute(user.getName(), "user"));
        strbuf.append("/>\n");
    }

    List writers = getWriters();
    for (ListIterator iterator = writers.listIterator(); iterator.hasNext(); ) {
        User user = (User) (iterator.next());
        strbuf.append(XmlSerializer.indent(indent));
        strbuf.append("<writer ");
        strbuf.append(XmlSerializer.xmlAttribute(user.getName(), "user"));
        strbuf.append("/>\n");
    }


    strbuf.append(XmlSerializer.indent(indent));
    strbuf.append("<jndi ");
    strbuf.append(XmlSerializer.xmlAttribute(getAdminName(), "name"));
    strbuf.append("/>\n");

    indent-=2;

    strbuf.append(XmlSerializer.indent(indent));

    if (getType().equals("queue")) {
      strbuf.append("</Queue>\n");
    } else if (getType().equals("topic")) {
      strbuf.append("</Topic>\n");
    }

    return strbuf.toString();
  }

  /**
   * Returns <code>true</code> if the destination is a queue.
   */
  public boolean isQueue() {
    return (this instanceof javax.jms.Queue);
  }

  public static final String QUEUE =
      "org.objectweb.joram.mom.dest.Queue";
  public static final String TOPIC =
      "org.objectweb.joram.mom.dest.Topic";
  public static final String DEAD_MQUEUE =
      "org.objectweb.joram.mom.dest.DeadMQueue";
  public static final String CLUSTER_QUEUE =
      "org.objectweb.joram.mom.dest.ClusterQueue";
  public static final String BRIDGE_QUEUE =
      "org.objectweb.joram.mom.dest.BridgeQueue";
  public static final String BRIDGE_TOPIC =
      "org.objectweb.joram.mom.dest.BridgeTopic";
  public static final String MAIL_QUEUE =
      "com.scalagent.joram.mom.dest.mail.JavaMailQueue";
  public static final String MAIL_TOPIC =
      "com.scalagent.joram.mom.dest.mail.JavaMailTopic";
  public static final String SCHEDULER_QUEUE =
      "com.scalagent.joram.mom.dest.scheduler.SchedulerQueue";

  /**
   * Administration method creating or retrieving a destination with a given name on a
   * given server, and returning its identifier.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployement fails server side.
   *
   * @param serverId  The identifier of the server where deploying the
   *                  destination.
   * @param name      The destination name.
   * @param className Name of the MOM destination class.
   * @param prop      Properties.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  protected static void doCreate(
    int serverId,
    String name,
    String className,
    Properties props,
    Destination dest,
    String expectedType)
    throws ConnectException, AdminException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(
        BasicLevel.DEBUG,
        "Destination.doCreate(" +
        serverId + ',' + name + ',' +
        className + ',' + props + ',' +
        dest + ',' + expectedType + ')');

    CreateDestinationRequest cdr =
      new CreateDestinationRequest(serverId,
                                   name,
                                   className,
                                   props,
                                   expectedType);
    CreateDestinationReply reply =
      (CreateDestinationReply) AdminModule.doRequest(cdr);
    dest.agentId = reply.getId();
    dest.adminName = name;
    dest.type = reply.getType();
  }

  /**
   * Administration method removing this destination from the platform.
   *
   * @exception AdminException    Never thrown.
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception JMSException      Never thrown.
   */
  public void delete() throws ConnectException, AdminException, javax.jms.JMSException {
    AdminModule.doRequest(new DeleteDestination(getName()));
    if (MXWrapper.mxserver != null) {
      StringBuffer buff = new StringBuffer();
      buff.append("type=");
      buff.append(getType());
      buff.append(",name=");
      buff.append(getAdminName());
      try {
        MXWrapper.unregisterMBean("joramClient",buff.toString());
      } catch (Exception e) {
        if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
          JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                     "unregisterMBean",e);
      }
    }
  }

  /**
   * Administration method setting free reading access to this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setFreeReading() throws ConnectException, AdminException {
    AdminModule.doRequest(new SetReader(null, getName()));
  }

  /**
   * Administration method setting free writing access to this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setFreeWriting() throws ConnectException, AdminException {
    AdminModule.doRequest(new SetWriter(null, getName()));
  }

  /**
   * Administration method unsetting free reading access to this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void unsetFreeReading() throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetReader(null, getName()));
  }

  /**
   * Administration method unsetting free writing access to this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void unsetFreeWriting() throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetWriter(null, getName()));
  }

  /**
   * Administration method setting a given user as a reader on this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @param user  User to be set as a reader.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setReader(User user) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetReader(user.getProxyId(), getName()));
  }

  /**
   * Administration method setting a given user as a reader on this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   *
   * @see org.objectweb.joram.client.jms.DestinationMBean#addReader(java.lang.String)
   */
  public void addReader(String proxyId) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetReader(proxyId, getName()));
  }

  /**
   * Administration method setting a given user as a writer on this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @param user  User to be set as a writer.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setWriter(User user) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetWriter(user.getProxyId(), getName()));
  }

  /**
   * Administration method setting a given user as a writer on this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   *
   * @see org.objectweb.joram.client.jms.DestinationMBean#addWriter(java.lang.String)
   */
  public void addWriter(String proxyId) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetWriter(proxyId, getName()));
  }

  /**
   * Administration method unsetting a given user as a reader on this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @param user  Reader to be unset.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void unsetReader(User user) throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetReader(user.getProxyId(), getName()));
  }

  /**
   * Administration method unsetting a given user as a reader on this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   *
   * @see org.objectweb.joram.client.jms.DestinationMBean#removeReader(java.lang.String)
   */
  public void removeReader(String proxyId) throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetReader(proxyId, getName()));
  }

  /**
   * Administration method unsetting a given user as a writer on this destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @param user  Writer to be unset.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void unsetWriter(User user) throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetWriter(user.getProxyId(), getName()));
  }

  /**
   * Administration method unsetting a given user as a writer on this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   * 
   * @param proxyId The unique identification of the user's proxy.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   *
   * @see org.objectweb.joram.client.jms.DestinationMBean#removeWriter(java.lang.String)
   */
  public void removeWriter(String proxyId) throws ConnectException, AdminException {
    AdminModule.doRequest(new UnsetWriter(proxyId, getName()));
  }

  /**
   * Administration method setting or unsetting a dead message queue for this
   * destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @param dmq  The dead message queue to be set (<code>null</code> for
   *             unsetting current DMQ).
   *
   * @exception IllegalArgumentException  If the DMQ is not a valid
   *              JORAM destination.
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setDMQ(DeadMQueue dmq) throws ConnectException, AdminException {
    if (dmq != null)
      setDMQId(dmq.getName());
    else
      setDMQId(null);
  }
  
  /**
   * Administration method setting or unsetting a dead message queue for this
   * destination.
   * <p>
   * The request fails if this destination is deleted server side.
   *
   * @param dmqId  The dead message queue Id to be set (<code>null</code> for
   *               unsetting current DMQ).
   *
   * @exception IllegalArgumentException  If the DMQ is not a valid
   *              JORAM destination.
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setDMQId(String dmqId) throws ConnectException, AdminException {
    if (dmqId == null)
      AdminModule.doRequest(new UnsetDestinationDMQ(getName()));
    else
      AdminModule.doRequest(new SetDestinationDMQ(getName(), dmqId));
  }

  /**
   * Monitoring method returning the list of all users that have a reading
   * permission on this destination, or an empty list if no specific readers
   * are set.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public List getReaders() throws ConnectException, AdminException {
    Monitor_GetReaders request = new Monitor_GetReaders(getName());
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) AdminModule.doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getUsers();
    String name;
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      list.add(new User(name, (String) users.get(name)));
    }
    return list;
  }

  /**
   * Monitoring method returning the list of all users that have a reading
   * permission on this destination, or an empty list if no specific readers
   * are set.
   * <p>
   * This method should be only used by the JMX MBean.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#getReaderList()
   */
  public List getReaderList() throws ConnectException, AdminException {
    Monitor_GetReaders request = new Monitor_GetReaders(getName());
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) AdminModule.doRequest(request);
    
    Vector list = new Vector();
    Hashtable users = reply.getUsers();
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      list.add((String) names.nextElement());
    }
    return list;
  }

  /**
   * Monitoring method returning the list of all users that have a writing
   * permission on this destination, or an empty list if no specific writers
   * are set.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public List getWriters() throws ConnectException, AdminException {
    Monitor_GetWriters request = new Monitor_GetWriters(getName());
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) AdminModule.doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getUsers();
    String name;
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      name = (String) names.nextElement();
      list.add(new User(name, (String) users.get(name)));
    }
    return list;
  }

  /**
   * Monitoring method returning the list of all users that have a writing
   * permission on this destination, or an empty list if no specific writers
   * are set.
   * <p>
   * This method should be only used by the JMX MBean.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#getWriterList()
   */
  public List getWriterList() throws ConnectException, AdminException {
    Monitor_GetWriters request = new Monitor_GetWriters(getName());
    Monitor_GetUsersRep reply = (Monitor_GetUsersRep) AdminModule.doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getUsers();
    for (Enumeration names = users.keys(); names.hasMoreElements();) {
      list.add((String) names.nextElement());
    }
    return list;
  }

  /**
   * Monitoring method returning <code>true</code> if this destination
   * provides free READ access.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public boolean isFreelyReadable() throws ConnectException, AdminException {
    Monitor_GetFreeAccess request = new Monitor_GetFreeAccess(getName());
    Monitor_GetFreeAccessRep reply;
    reply = (Monitor_GetFreeAccessRep) AdminModule.doRequest(request);

    return reply.getFreeReading();
  }

  /**
   * Administration method (un)setting free reading access to this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   *
   * @param b if true set the free reading access else disable.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#setFreelyReadable(boolean)
   */
  public void setFreelyReadable(boolean b) throws ConnectException, AdminException {
    if (b)
      setFreeReading();
    else
      unsetFreeReading();
  }

  /**
   * Monitoring method returning <code>true</code> if this destination
   * provides free WRITE access.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public boolean isFreelyWriteable() throws ConnectException, AdminException {
    Monitor_GetFreeAccess request = new Monitor_GetFreeAccess(getName());
    Monitor_GetFreeAccessRep reply;
    reply = (Monitor_GetFreeAccessRep) AdminModule.doRequest(request);

    return reply.getFreeWriting();
  }

  /**
   * Administration method (un)setting free writing access to this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   *
   * @param b if true set the free writing access else disable.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#setFreelyWriteable(boolean)
   */
  public void setFreelyWriteable(boolean b) throws ConnectException, AdminException {
    if (b)
      setFreeWriting();
    else
      unsetFreeWriting();
  }

  /**
   * Monitoring method returning the dead message queue of this destination,
   * null if not set.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDMQ() throws ConnectException, AdminException {
    Monitor_GetDMQSettings request = new Monitor_GetDMQSettings(getName());
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) AdminModule.doRequest(request);

    if (reply.getDMQName() == null) {
      return null;
    } else {
      return new DeadMQueue(reply.getDMQName());
    }
  }

  /**
   * Monitoring method returning the dead message queue id of this destination,
   * null if not set.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public String getDMQId() throws ConnectException, AdminException {
    DeadMQueue dmq = getDMQ();
    if (dmq != null)
      return dmq.getName();
    else
      return null;
  }

  public static Destination newInstance(
    String id,
    String name,
    String type) throws AdminException {
    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                 "Destination.newInstance(" +
                                 id + ',' + name + ',' + type + ')');
    Destination dest;
    if (Queue.isQueue(type)) {
      if (TemporaryQueue.isTemporaryQueue(type)) {
        dest = new TemporaryQueue(id, null);
      } else if (DeadMQueue.isDeadMQueue(type)) {
        dest = new DeadMQueue(id);
      } else {
        dest = new Queue(id);
      }
    } else if (Topic.isTopic(type)) {
      if (TemporaryTopic.isTemporaryTopic(type)) {
        dest = new TemporaryTopic(id, null);
      } else {
        dest = new Topic(id);
      }
    } else throw new AdminException("Unknown destination type");
    dest.adminName = name;
    return dest;
  }

  public static boolean isAssignableTo(String realType,
                                       String resultingType) {
    return realType.startsWith(resultingType);
  }

  /**
   * Return a set of statistic values from the destination.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#getStatistic()
   * @deprecated
   */
  public Hashtable getStatistic() throws ConnectException, AdminException {
    return getStatistics();
  }

  /**
   * Return a set of statistic values from the destination.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#getStatistics()
   */
  public Hashtable getStatistics() throws ConnectException, AdminException {
    Monitor_GetStat request = new Monitor_GetStat(agentId);
    Monitor_GetStatRep reply = (Monitor_GetStatRep) AdminModule.doRequest(request);
    return  reply.getStats();
  }

  /** Sets the naming reference of a connection factory. */
  public void toReference(Reference ref) throws NamingException {
    ref.add(new StringRefAddr("dest.agentId", agentId));
    ref.add(new StringRefAddr("dest.adminName", adminName));
  }

  /** Restores the administered object from a naming reference. */
  public void fromReference(Reference ref) throws NamingException {
    agentId = (String) ref.get("dest.agentId").getContent();
    adminName = (String) ref.get("dest.adminName").getContent();
  }

  /**
   * Codes a <code>Destination</code> as a Hashtable for travelling through the
   * SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();
    h.put("agentId", getName());
    h.put("type", type);
    return h;
  }

  public void decode(Hashtable h) {
    agentId = (String) h.get("agentId");
    type = (String) h.get("type");
  }
}
