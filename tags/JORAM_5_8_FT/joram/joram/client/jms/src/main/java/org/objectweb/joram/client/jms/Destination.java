/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import javax.jms.InvalidDestinationException;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminItf;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.AdministeredObject;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.admin.XmlSerializer;
import org.objectweb.joram.shared.DestinationConstants;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.CreateDestinationReply;
import org.objectweb.joram.shared.admin.CreateDestinationRequest;
import org.objectweb.joram.shared.admin.DeleteDestination;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetRightsReply;
import org.objectweb.joram.shared.admin.GetRightsRequest;
import org.objectweb.joram.shared.admin.GetStatsReply;
import org.objectweb.joram.shared.admin.GetStatsRequest;
import org.objectweb.joram.shared.admin.SetDMQRequest;
import org.objectweb.joram.shared.admin.SetReader;
import org.objectweb.joram.shared.admin.SetWriter;
import org.objectweb.joram.shared.admin.UnsetReader;
import org.objectweb.joram.shared.admin.UnsetWriter;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * Implements the <code>javax.jms.Destination</code> interface and provides
 * JORAM specific administration and monitoring methods.
 * <p>
 * A Destination is a JMS administered object that encapsulates a Joram's specific address.
 * It is created by an administrator and later used by JMS clients. Normally the JMS clients
 * find administered objects by looking them up in a JNDI namespace.
 * <p>
 * <b>Joram MOM Model</b>
 * <p>
 * Server side, a destination is a component receiving messages from producers and answering to
 * consuming requests from consumers. A destination might either be a “queue” or a “topic”:<ul>
 * <li>Queue: each messages is read only by a single client.</li>
 * <li>Topic: All clients that have previously subscribed to this topic are notified of
 * the corresponding message.</li>
 * </ul>
 * A destination allows clients to perform operations according to their access rights. A client
 * set as a READER will be able to request messages from the destination (either as a subscriber
 * to a topic, or as a receiver or browser on a queue). A client set as a WRITER will be able to
 * send messages to the destination.
 * <p>
 * A destination provides methods to add and remove Interceptors, an interceptor is an object handling
 * each message sent to the destination. Interceptors can read and also modify the messages. This enables
 * filtering, transformation or content enrichment, for example adding a property into the message.
 * Also Interceptors can stop the Interceptor chain by simply returning false to their intercept method
 * invocation, in this case the transmission of the message is stopped.
 */
public abstract class Destination extends AdministeredObject implements javax.jms.Destination, DestinationMBean {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  public static Logger logger = Debug.getLogger(Destination.class.getName());

  /**
   * Identifier of the agent destination.
   * 
   * Be careful when using directly this attribute, it is null in clustered
   * destination even the getDestination method is called.
   */
  protected String agentId;
  /**
   * Returns the internal name of the destination.
   * This unique name is chosen internally by the MOM.
   * 
   * @return the internal name of the destination.
   */
  public String getName() {
    return agentId;
  }

  /**
   * Check the specified destination identifier.
   * 
   * @exception Exception if an invalid destination identifier is specified.
   */
  public static final void checkId(String id) throws InvalidDestinationException {
    try {
      DestinationConstants.checkId(id);
    } catch (Exception exc) {
      throw new InvalidDestinationException(exc.getMessage());
    }
  }
  
  /**
   * Check the destination identifier.
   * 
   * @exception InvalidDestinationException if the destination identifier is invalid.
   */
  public void check() throws InvalidDestinationException {
    checkId(getName());
  }

  /**
   * Constant defining the type of a topic destination.
   * @see #getType()
   * @see DestinationConstants#TOPIC_TYPE
   */
  public final static byte TOPIC_TYPE = DestinationConstants.TOPIC_TYPE;
  /**
   * Constant defining the type of a queue destination.
   * @see #getType()
   * @see DestinationConstants#QUEUE_TYPE
   */
  public final static byte QUEUE_TYPE = DestinationConstants.QUEUE_TYPE;
  
  /**
   * Constant defining the type of a temporary destination (OR'ed with queue or topic type
   * depending of the real type of the destination).
   * @see #getType()
   * @see DestinationConstants#TEMPORARY
   */
  public final static byte TEMPORARY = DestinationConstants.TEMPORARY;
  
  /**
   * Type of the destination: Queue or Topic, Temporary or not.
   * @see #getType()
   */
  private byte type;

  /**
   * Returns the type of the destination: queue or topic, temporary or not.
   */
  public byte getType() {
    return type;
  }

  /** Symbolic name given by the administrator. */
  protected String adminName;

  /**
   * Returns the symbolic administration name of the destination.
   * This symbolic name is given by the user at creation, if it is unknown the internal
   * name of this destination is returned.
   * 
   * @return the symbolic name of the destination if any.
   */
  public final String getAdminName() {
    if (adminName == null)
      return agentId;
    return adminName;
  }

  public Destination() {}

  protected Destination(byte type) {
    this.type = type;
  }

  protected Destination(String id, byte type) {
    agentId = id;
    this.type = type;
  }

  /**
   * Returns <code>true</code> if the parameter object is a Joram destination
   * wrapping the same Joram's Destination.
   */
  public boolean equals(Object obj) {
    if (! (obj instanceof Destination))
      return false;

    return (getName().equals(((Destination) obj).getName()));
  }

  public int hashCode() {
    return getName().hashCode();
  }

  /**
   * Administration wrapper used to perform administration stuff.
   * <p>
   * It is defined through AdminModule element, it is closed at the end of
   * the script. if it is not defined the wrapper set at creation is used, if
   * none the static AdminModule connection is used.
   */
  AdminItf wrapper = null;

  /**
   * Returns the administration wrapper to use.
   * 
   * @return The wrapper to use.
   * @throws ConnectException if no wrapper is defined.
   */
  protected final AdminItf getWrapper() throws ConnectException {
    if ((wrapper != null) && (! wrapper.isClosed()))
      return wrapper;
    return AdminModule.getWrapper();
  }

  /**
   * Sets the administration wrapper to use.
   * If not set the AdminModule static connection is used by default.
   * 
   * @param wrapper The wrapper to use or null to unset.
   */
  public void setWrapper(AdminItf wrapper) {
    this.wrapper = wrapper;
  }

  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   * 
   * @param request the administration request to send
   * @return  the reply message
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */
  final AdminReply doRequest(AdminRequest request) throws AdminException, ConnectException {
    return getWrapper().doRequest(request);
  }

  /**
   * Format the destination properties in a XML format, the result can be used
   * in an XML configuration script.
   * 
   * @param indent use this indent for prefixing XML representation.
   * @param serverId server id hosting the destination object
   * @return returns a XML view of the queue (XML configuration format)
   * 
   * @throws ConnectException if the server is unreachable
   * @throws AdminException if an error occurs
   */
  public String toXml(int indent, int serverId)  throws ConnectException, AdminException {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(XmlSerializer.indent(indent));

    if (getType() == QUEUE_TYPE) {
      strbuf.append("<Queue ");
    } else if (getType() == TOPIC_TYPE) {
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

    if (getType() == QUEUE_TYPE) {
      strbuf.append("</Queue>\n");
    } else if (getType() == TOPIC_TYPE) {
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

  /**
   * Returns <code>true</code> if the destination is a topic.
   */
  public boolean isTopic() {
    return (this instanceof javax.jms.Topic);
  }

  /**
   * Constant defining the implementation class for a classic Queue.
   */
  public static final String QUEUE = "org.objectweb.joram.mom.dest.Queue";
  /**
   * Constant defining the implementation class for a classic Topic.
   */ 
  public static final String TOPIC = "org.objectweb.joram.mom.dest.Topic";
  /**
   * Constant defining the implementation class for a Dead Message Queue. 
   * @deprecated Since Joram 5.2.2 the DeadMQueue is a simple Queue. 
   */
  public static final String DEAD_MQUEUE = "org.objectweb.joram.mom.dest.Queue";
  /**
   * Constant defining the implementation class for a clustered Queue. 
   */
  public static final String CLUSTER_QUEUE = "org.objectweb.joram.mom.dest.ClusterQueue";
  /**
   * Constant defining the implementation class for a scheduled Queue.
   */
  public static final String SCHEDULER_QUEUE = "com.scalagent.joram.mom.dest.scheduler.SchedulerQueue";
  /**
   * Constant defining the implementation class for a Queue allowing to collect data from
   * external sources. The nature of data collector is configurable through properties.
   */
  public static final String ACQUISITION_QUEUE = "org.objectweb.joram.mom.dest.AcquisitionQueue";
  /**
   * Constant defining the implementation class for a Queue allowing to forward data to
   * external targets. The nature of data forwarder is configurable through properties.
   */
  public static final String DISTRIBUTION_QUEUE = "org.objectweb.joram.mom.dest.DistributionQueue";
  /**
   * Constant defining the implementation class for a Topic allowing to collect data from
   * external sources. The nature of data collector is configurable through properties.
   */
  public static final String ACQUISITION_TOPIC = "org.objectweb.joram.mom.dest.AcquisitionTopic";
  /**
   * Constant defining the implementation class for a Queue allowing to forward data to
   * external targets. The nature of data forwarder is configurable through properties.
   */
  public static final String DISTRIBUTION_TOPIC = "org.objectweb.joram.mom.dest.DistributionTopic";
  public static final String ALIAS_QUEUE = "org.objectweb.joram.mom.dest.AliasQueue";
  /**
   * Constant defining the implementation class for a Queue allowing to forward data with
   * Ftp. The nature of data forwarder is configurable through properties.
   */
  public static final String FTP_QUEUE = "com.scalagent.joram.mom.dest.ftp.FtpQueue";
  
  /**
   * Administration method creating or retrieving a destination with a given name on a
   * given server, and returning its identifier.
   * <p>
   * The request fails if the target server does not belong to the platform,
   * or if the destination deployment fails server side.
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param serverId  The identifier of the server where deploying the
   *                  destination.
   * @param name      The destination name.
   * @param className Name of the MOM destination class.
   * @param props     The configuration properties of the destination.
   * @param dest      The proxy object of the destination.
   * @param type      The type of the destination: queue, topic, temporary or not.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  protected void doCreate(int serverId,
                                 String name,
                                 String className,
                                 Properties props,
                                 Destination dest,
                                 byte type) throws ConnectException, AdminException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Destination.doCreate(" + serverId + ',' + name + ',' + className + ',' + props + ',' + dest + ',' + type + ')');

    CreateDestinationRequest cdr = new CreateDestinationRequest(serverId, name, className, props, type);
    CreateDestinationReply reply = (CreateDestinationReply) getWrapper().doRequest(cdr);

    dest.agentId = reply.getId();
    dest.adminName = name;

    // Be careful, MBean registration is now done explicitly
  }

  /**
   * Administration method removing this destination from the platform.
   *
   * @exception AdminException    Never thrown.
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception JMSException      Never thrown.
   */
  public void delete() throws ConnectException, AdminException, javax.jms.JMSException {
    doRequest(new DeleteDestination(getName()));
    unregisterMBean();
  }

  // Object name of the MBean if it is registered.
  transient protected String JMXBeanName = null;

  public static String getJMXBeanName(String base, Destination dest) {
    String agentId = dest.agentId;
    int sid = Integer.parseInt(agentId.substring(agentId.indexOf('.') +1, agentId.lastIndexOf('.')));
    StringBuffer buf = new StringBuffer();
    buf.append(base);
    if (dest.isQueue())
      buf.append(":type=Queue,location=server#");
    else
      buf.append(":type=Topic,location=server#");
    buf.append(sid).append(",name=").append(dest.getAdminName()).append('[').append(dest.getName()).append(']');
    
    return buf.toString();
  }
  
  public String registerMBean(String base) {
    JMXBeanName = getJMXBeanName(base, this);
    try {
       MXWrapper.registerMBean(this, JMXBeanName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Destination.registerMBean: " + JMXBeanName, e);
    }
    
    return JMXBeanName;
  }

  public void unregisterMBean() {
    if (JMXBeanName == null)
      return;

    try {
      MXWrapper.unregisterMBean(JMXBeanName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "Destination.unregisterMBean: " + JMXBeanName, e);
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
    doRequest(new SetReader(null, getName()));
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
    doRequest(new SetWriter(null, getName()));
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
    doRequest(new UnsetReader(null, getName()));
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
    doRequest(new UnsetWriter(null, getName()));
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
    doRequest(new SetReader(user.getProxyId(), getName()));
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
    doRequest(new SetReader(proxyId, getName()));
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
    doRequest(new SetWriter(user.getProxyId(), getName()));
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
    doRequest(new SetWriter(proxyId, getName()));
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
    doRequest(new UnsetReader(user.getProxyId(), getName()));
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
    doRequest(new UnsetReader(proxyId, getName()));
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
    doRequest(new UnsetWriter(user.getProxyId(), getName()));
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
    doRequest(new UnsetWriter(proxyId, getName()));
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
    GetRightsRequest request = new GetRightsRequest(getName());
    GetRightsReply reply = (GetRightsReply) doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getReaders();
    if (users != null) {
      String name;
      for (Enumeration names = users.keys(); names.hasMoreElements();) {
        name = (String) names.nextElement();
        list.add(new User(name, (String) users.get(name)));
      }
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
    GetRightsRequest request = new GetRightsRequest(getName());
    GetRightsReply reply = (GetRightsReply) doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getReaders();
    if (users != null) {
      for (Enumeration names = users.keys(); names.hasMoreElements();) {
        list.add(names.nextElement());
      }
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
    GetRightsRequest request = new GetRightsRequest(getName());
    GetRightsReply reply = (GetRightsReply) doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getWriters();
    if (users != null) {
      String name;
      for (Enumeration names = users.keys(); names.hasMoreElements();) {
        name = (String) names.nextElement();
        list.add(new User(name, (String) users.get(name)));
      }
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
    GetRightsRequest request = new GetRightsRequest(getName());
    GetRightsReply reply = (GetRightsReply) doRequest(request);

    Vector list = new Vector();
    Hashtable users = reply.getWriters();
    if (users != null) {
      for (Enumeration names = users.keys(); names.hasMoreElements();) {
        list.add(names.nextElement());
      }
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
    GetRightsRequest request = new GetRightsRequest(getName());
    GetRightsReply reply = (GetRightsReply) doRequest(request);

    return reply.isFreeReading();
  }

  /**
   * Administration method (un)setting free reading access to this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   *
   * @param readable if true set the free reading access else disable.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#setFreelyReadable(boolean)
   */
  public void setFreelyReadable(boolean readable) throws ConnectException, AdminException {
    if (readable)
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
    GetRightsRequest request = new GetRightsRequest(getName());
    GetRightsReply reply = (GetRightsReply) doRequest(request);

    return reply.isFreeWriting();
  }

  /**
   * Administration method (un)setting free writing access to this destination.
   * <p>
   * This method should be only used by the JMX MBean.
   *
   * @param writeable if true set the free writing access else disable.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#setFreelyWriteable(boolean)
   */
  public void setFreelyWriteable(boolean writeable) throws ConnectException, AdminException {
    if (writeable)
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
  public Queue getDMQ() throws ConnectException, AdminException {
    String dmqId = getDMQId();
    Queue dmq = null;
    if (dmqId != null) {
      dmq = new Queue(dmqId);
      if (wrapper != null)
        dmq.setWrapper(wrapper);
    }
    return dmq;
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
    GetDMQSettingsRequest request = new GetDMQSettingsRequest(getName());
    GetDMQSettingsReply reply = (GetDMQSettingsReply) doRequest(request);

    return reply.getDMQName();
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
   * @throws InvalidDestinationException If the specified destination is invalid.
   */
  public void setDMQ(Queue dmq) throws ConnectException, AdminException, InvalidDestinationException {
    if (dmq != null) {
      setDMQId(dmq.getName());
    } else {
      setDMQId(null);
    }
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
   * @throws InvalidDestinationException If the specified destination is invalid.
   */
  public void setDMQId(String dmqId) throws ConnectException, AdminException, InvalidDestinationException {
    if (dmqId != null) checkId(dmqId);
    doRequest(new SetDMQRequest(getName(), dmqId));
  }

  public static Destination newInstance(String id,
                                        String name,
                                        byte type) throws AdminException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "Destination.newInstance(" + id + ',' + name + ',' + type + ')');
    
    Destination dest;
    if ((type & QUEUE_TYPE) != 0) {
      if ((type & TEMPORARY) != 0) {
        dest = new TemporaryQueue(id, null);
      } else {
        dest = new Queue(id);
      }
    } else if ((type & TOPIC_TYPE) != 0) {
      if ((type & TEMPORARY) != 0) {
        dest = new TemporaryTopic(id, null);
      } else {
        dest = new Topic(id);
      }
    } else {
      throw new AdminException("Unknown destination type (" + type + ')');
    }
    
    dest.adminName = name;
    return dest;
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
   * Returns values of all valid JMX attributes about the destination.
   * 
   * @return a Hashtable containing the values of all valid JMX attributes about the destination.
   *         The keys are the name of corresponding attributes.
   * 
   * @see org.objectweb.joram.client.jms.DestinationMBean#getStatistics()
   */
  public Hashtable getStatistics() throws ConnectException, AdminException {
    GetStatsRequest request = new GetStatsRequest(getName());
    GetStatsReply reply = (GetStatsReply) doRequest(request);
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
   * Administration method add interceptors.
   * 
   * @param interceptors list of string className interceptor (separate with ",")
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void addInterceptors(String interceptors) throws ConnectException, AdminException {
  	Properties prop = new Properties();
    prop.put(AdminCommandConstant.INTERCEPTORS, interceptors);
    getWrapper().processAdmin(getName(), AdminCommandConstant.CMD_ADD_INTERCEPTORS, prop);
  }
  
  /**
   * Administration method to get interceptors list.
   * 
   * @return list of string className interceptor (separate with ",")
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public String getInterceptors() throws ConnectException, AdminException {
    AdminCommandReply reply = (AdminCommandReply) getWrapper().processAdmin(getName(),
        AdminCommandConstant.CMD_GET_INTERCEPTORS, null);
    return (String) reply.getProp().get(AdminCommandConstant.INTERCEPTORS);
  }
  
  /**
   * Administration method to remove interceptors. 
   * 
   * @param interceptors list of string className interceptor (separate with ",")
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void removeInterceptors(String interceptors) throws ConnectException, AdminException {
  	Properties prop = new Properties();
  	prop.put(AdminCommandConstant.INTERCEPTORS, interceptors);
    getWrapper().processAdmin(getName(), AdminCommandConstant.CMD_REMOVE_INTERCEPTORS, prop);
  }
  
  /**
   * Administration method to replace interceptor.
   * 
   * @param newInterceptor the new className interceptor.
   * @param oldInterceptor the old className interceptor.
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void replaceInterceptor(String newInterceptor, String oldInterceptor) throws ConnectException, AdminException {
  	Properties prop = new Properties();
    prop.put(AdminCommandConstant.INTERCEPTORS_NEW, newInterceptor);
    prop.put(AdminCommandConstant.INTERCEPTORS_OLD, oldInterceptor);
    getWrapper().processAdmin(getName(), AdminCommandConstant.CMD_REPLACE_INTERCEPTORS, prop);
  }
  
  /**
   * Administration method to set properties.
   * 
   * @param prop the properties to update.
   * @return the admin reply
   * 
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public AdminReply setProperties(Properties prop) throws ConnectException, AdminException {
  	return getWrapper().processAdmin(getName(), AdminCommandConstant.CMD_SET_PROPERTIES, prop);
  }
}
