/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 2004 Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms.admin;

import java.net.ConnectException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.joram.client.jms.Message;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.shared.admin.AdminCommandConstant;
import org.objectweb.joram.shared.admin.AdminCommandReply;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.AdminRequest;
import org.objectweb.joram.shared.admin.ClearSubscription;
import org.objectweb.joram.shared.admin.CreateUserReply;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.DeleteSubscriptionMessage;
import org.objectweb.joram.shared.admin.DeleteUser;
import org.objectweb.joram.shared.admin.GetDMQSettingsReply;
import org.objectweb.joram.shared.admin.GetDMQSettingsRequest;
import org.objectweb.joram.shared.admin.GetNbMaxMsgRequest;
import org.objectweb.joram.shared.admin.GetNumberReply;
import org.objectweb.joram.shared.admin.GetSubscription;
import org.objectweb.joram.shared.admin.GetSubscriptionMessage;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIds;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIdsRep;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageRep;
import org.objectweb.joram.shared.admin.GetSubscriptionRep;
import org.objectweb.joram.shared.admin.GetSubscriptions;
import org.objectweb.joram.shared.admin.GetSubscriptionsRep;
import org.objectweb.joram.shared.admin.SetDMQRequest;
import org.objectweb.joram.shared.admin.SetNbMaxMsgRequest;
import org.objectweb.joram.shared.admin.SetThresholdRequest;
import org.objectweb.joram.shared.admin.UpdateUser;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.security.SimpleIdentity;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>User</code> class is a utility class needed for administering Joram users.
 * <p>
 * The User class is a factory for Joram's users through the create static methods, the
 * User object provides Joram specific administration and monitoring methods.
 * <p>
 * The User object provides methods to add and remove Interceptors, such an interceptor
 * can handle each incoming and outgoing message. Interceptors can read and also modify
 * the messages. This enables filtering, transformation or content enrichment, for example
 * adding a property into the message. Also Interceptors can stop the Interceptor chain by
 * simply returning false to their intercept method invocation, in this case the transmission
 * of the message is stopped.
 * <p>
 * There is two distinct chains of interceptors:<ul>
 * <li>The first one “interceptors_in” handles each message that’s entering the server (result
 * of a send method on a connection from the selected user).</li>
 * <li>The second one “interceptors_out” handles each message that’s exiting the server (result
 * of a receive method on a connection from the selected user).</li>
 * <ul>
 * These two interceptor chains are configurable for each user.
 */
public class User extends AdministeredObject implements UserMBean {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  public static Logger logger = Debug.getLogger(User.class.getName());

  /** The name of the user. */
  String name;
  /** Identifier of the user's proxy agent. */
  String proxyId;

  // Used by jndi2 SoapObjectHelper
  public User() {}

  /**
   * Constructs an <code>User</code> instance.
   *
   * @param name  The name of the user.
   */
  public User(String name) {
    this.name = name;
  }

  /**
   * Constructs an <code>User</code> instance.
   *
   * @param name  The name of the user.
   * @param proxyId  Identifier of the user's proxy agent.
   */
  public User(String name, String proxyId) {
    this.name = name;
    this.proxyId = proxyId;
  }


  /** Returns a string view of this <code>User</code> instance. */
  public String toString() {
    return "User[" + name + "]:" + proxyId;
  }


  /** Returns the user name. */
  public String getName() {
    return name;
  }

  /** 
   * Returns <code>true</code> if the parameter object is a Joram user wrapping
   * the same Joram's User.
   */
  public boolean equals(Object o) {
    if (! (o instanceof User))
      return false;

    User other = (User) o;

    return other.proxyId.equals(proxyId);
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
  AdminWrapper wrapper = null;

  /**
   * Returns the administration wrapper to use.
   * 
   * @return The wrapper to use.
   * @throws ConnectException if no wrapper is defined.
   */
  protected final AdminWrapper getWrapper() throws ConnectException {
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
  public void setWrapper(AdminWrapper wrapper) {
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
  private final AdminReply doRequest(AdminRequest request) throws AdminException, ConnectException {
    return getWrapper().doRequest(request);
  }

  /**
   * Admin method creating a user for a given server and instantiating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password,
                            int serverId) throws ConnectException, AdminException {
    return create(name, password, serverId, SimpleIdentity.class.getName());
  }

  /**
   * Admin method creating a user on the local server and instantiating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. It fails if a
   * proxy could not be deployed server side for a new user. 
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password) throws ConnectException, AdminException {
    return create(name, password, AdminModule.getLocalServerId(), SimpleIdentity.class.getName());
  }

  /**
   * Admin method creating a user for a given server and instantiating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   * @param identityClassName user/password or JAAS... (default SimpleIdentity).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password,
                            int serverId,
                            String identityClassName) throws ConnectException, AdminException {
  	return create(name, password, serverId, identityClassName, null);
  }
  
  /**
   * Admin method creating a user for a given server and instantiating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   * <p>
   * Be careful this method use the static AdminModule connection.
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   * @param identityClassName user/password or JAAS... (default SimpleIdentity).
   * @param prop properties
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password,
                            int serverId,
                            String identityClassName,
                            Properties prop) throws ConnectException, AdminException {
    Identity identity = createIdentity(name, password, identityClassName);

    User user = new User(name);
    AdminReply reply = user.getWrapper().doRequest(new CreateUserRequest(identity, serverId, prop));
    user.proxyId = ((CreateUserReply) reply).getProxId();
    
    // Be careful, MBean registration is now done explicitly

    return user;
  }

  // Object name of the MBean if it is registered.
  transient protected String JMXBeanName = null;
  
  public static String getJMXBeanName(String base, User user) {
    int sid = Integer.parseInt(user.proxyId.substring(user.proxyId.indexOf('.') +1, user.proxyId.lastIndexOf('.')));
    StringBuffer buf = new StringBuffer();
    buf.append(base);
    buf.append(":type=User,location=server#").append(sid).append(",name=").append(user.getName()).append('[').append(user.getProxyId()).append(']');
    return buf.toString();
  }

  public String registerMBean(String base) {
    JMXBeanName = getJMXBeanName(base, this);
    
    try {
      MXWrapper.registerMBean(this, JMXBeanName);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "User.registerMBean: " + JMXBeanName, e);
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
        logger.log(BasicLevel.DEBUG, "User.unregisterMBean: " + JMXBeanName, e);
    }
  }

  /**
   * Create a user Identity.
   * 
   * @param user              Name of the user.
   * @param passwd            Password of the user.
   * @param identityClassName identity class name (simple, jaas).
   * @return identity user Identity.
   * @throws AdminException
   */
  private static Identity createIdentity(String user, String passwd,
                                         String identityClassName) throws AdminException {
    Identity identity = null;
    try {
      Class clazz = Class.forName(identityClassName);
      identity = (Identity) clazz.newInstance();
      if (passwd != null)
        identity.setIdentity(user, passwd);
      else
        identity.setUserName(user);
    } catch (Exception e) {
      throw new AdminException(e.getMessage());
    }
    return identity;
  }

  /**
   * Admin method updating this user identification.
   * <p>
   * The request fails if the user does not exist server side, or if the new
   * identification is already taken by a user on the same server.
   *
   * @param newName  The new name of the user.
   * @param newPassword  The new password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void update(String newName, String newPassword) throws ConnectException, AdminException {
    update(newName, newPassword, SimpleIdentity.class.getName());
  }

  /**
   * Admin method updating this user identification.
   * <p>
   * The request fails if the user does not exist server side, or if the new
   * identification is already taken by a user on the same server.
   *
   * @param newName  The new name of the user.
   * @param newPassword  The new password of the user.
   * @param identityClassName user/password or JAAS... (default SimpleIdentity).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void update(String newName, String newPassword,
                     String identityClassName) throws ConnectException, AdminException {
    Identity newIdentity = createIdentity(newName, newPassword, identityClassName);
    doRequest(new UpdateUser(name, proxyId, newIdentity));
    name = newName;
  }

  /**
   * Removes this user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void delete() throws ConnectException, AdminException {
    doRequest(new DeleteUser(name, proxyId));
    unregisterMBean();
  } 

  /**
   * Admin method setting a given dead message queue for this user.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @param dmq  The dead message queue to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDMQ(Queue dmq) throws ConnectException, AdminException {
    setDMQId(dmq.getName());
  }

  /**
   * Admin method setting a given dead message queue for this user.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @param dmqId  The dead message queue Id to be set.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setDMQId(String dmqId) throws ConnectException, AdminException {
    doRequest(new SetDMQRequest(proxyId, dmqId));
  }
  
  /**
   * Monitoring method returning the dead message queue of this user,
   * null if not set.
   * <p>
   * The request fails if the user is deleted server side.
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
   * Monitoring method returning the dead message queue id of this user,
   * null if not set.
   * <p>
   * The request fails if the destination is deleted server side.
   *
   * @exception ConnectException  If the administration connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public String getDMQId() throws ConnectException, AdminException {
    GetDMQSettingsRequest request = new GetDMQSettingsRequest(proxyId);
    GetDMQSettingsReply reply = (GetDMQSettingsReply) doRequest(request);

    return reply.getDMQName();
  }

  /**
   * Admin method setting a given value as the threshold for this user.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @param threshold  The threshold value to be set (-1 for unsetting
   *                   previous value).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setThreshold(int threshold) throws ConnectException, AdminException {
    doRequest(new SetThresholdRequest(proxyId, threshold));
  }
  
  /**
   * Admin method setting a given value as the threshold for a particular
   * subscription of this user.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @param subname    The subscription name.
   * @param threshold  The threshold value to be set (-1 for unsetting
   *                   previous value).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void setThreshold(String subname, int threshold) throws ConnectException, AdminException {
    doRequest(new SetThresholdRequest(proxyId, subname, threshold));
  }

  /** 
   * Returns the threshold for this user, -1 if not set.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold() throws ConnectException, AdminException {
    GetDMQSettingsRequest request = new GetDMQSettingsRequest(proxyId);
    GetDMQSettingsReply reply = (GetDMQSettingsReply) doRequest(request);

    return reply.getThreshold();
  }

  /** 
   * Returns the threshold for a particular subscription of this user,
   * -1 if not set.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @param subname The subscription name.
   * 
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public int getThreshold(String subname) throws ConnectException, AdminException {
    GetDMQSettingsRequest request = new GetDMQSettingsRequest(proxyId, subname);
    GetDMQSettingsReply reply = (GetDMQSettingsReply) doRequest(request);

    return reply.getThreshold();
  }

  /**
   * Admin method setting nbMaxMsg for this subscription.
   * <p>
   * The request fails if the sub is deleted server side.
   *
   * @param subName the name of the subscription.
   * @param nbMaxMsg  nb Max of Message (-1 no limit).
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public void setNbMaxMsg(String subName, int nbMaxMsg) throws ConnectException, AdminException {
    //  TODO: Subscription sub = getSubscription(subName);
    doRequest(new SetNbMaxMsgRequest(proxyId, nbMaxMsg, subName));
  } 

  /** 
   * Monitoring method returning the nbMaxMsg of this subscription, -1 if no limit.
   * <p>
   * The request fails if the sub is deleted server side.
   *
   * @param subName the name of the subscription.
   *
   * @exception ConnectException  If the admin connection is closed or broken.
   * @exception AdminException  If the request fails.
   */
  public int getNbMaxMsg(String subName) throws ConnectException, AdminException {
    //  TODO: Subscription sub = getSubscription(subName);
    GetNbMaxMsgRequest request = new GetNbMaxMsgRequest(proxyId, subName);
    GetNumberReply reply = (GetNumberReply) doRequest(request);
    return reply.getNumber();
  }

  /**
   * Returns the subscriptions owned by a user.
   *
   * @return The subscriptions owned by the user.
   *
   * @exception AdminException If an error is raised by the administration operation.
   * @exception ConnectException  If the admin connection is not established.
   */
  public Subscription[] getSubscriptions() throws AdminException, ConnectException {
    GetSubscriptionsRep reply = (GetSubscriptionsRep) doRequest(new GetSubscriptions(proxyId));

    String[] subNames = reply.getSubNames();
    String[] topicIds = reply.getTopicIds();
    int[] messageCounts = reply.getMessageCounts();
    int[] ackCounts = reply.getDeliveredMessageCount();
    boolean[] durable = reply.getDurable();

    Subscription[] res = new Subscription[subNames.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = new Subscription(subNames[i], topicIds[i], messageCounts[i], ackCounts[i], durable[i]);
    }
    return res;
  }

  /** used by MBean jmx */
  public List getSubscriptionList() throws AdminException, ConnectException {
    Vector list = new Vector();
    Subscription[] sub = getSubscriptions();
    for (int i = 0; i < sub.length; i++) {
      list.add(sub[i].toString());
    }
    return list;
  }

  /**
   * Returns a subscription.
   *
   * @param subName the name of the subscription.
   * @return The subscription.
   *
   * @exception AdminException If an error is raised by the administration operation.
   * @exception ConnectException  If the admin connection is not established.
   */
  public Subscription getSubscription(String subName) throws AdminException, ConnectException {
    GetSubscriptionRep reply = (GetSubscriptionRep) doRequest(new GetSubscription(proxyId, subName));
    Subscription sub = new Subscription(subName, reply.getTopicId(), reply.getMessageCount(), reply.getDeliveredMessageCount(), reply.getDurable());

    return sub;
  }

  /** used by MBean jmx */
  public String getSubscriptionString(String subName) throws AdminException, ConnectException {
    return getSubscription(subName).toString();
  }

  public String[] getMessageIds(String subName) throws AdminException, ConnectException {
    GetSubscriptionMessageIdsRep reply =
      (GetSubscriptionMessageIdsRep) doRequest(new GetSubscriptionMessageIds(proxyId, subName));
    return reply.getMessageIds();
  }

  /**
   * Returns a copy of a message of the subscription.
   * 
   * @param subName       The name of the related subscription.
   * @param msgId         The identifier of the message.
   * @return The message
   * 
   * @throws AdminException
   * @throws ConnectException
   * @throws JMSException
   */
  public Message getMessage(String subName,
                            String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, true));
    return Message.wrapMomMessage(null, reply.getMessage());
  }

  public String getMessageDigest(String subName,
                                 String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, true));
    Message msg =  Message.wrapMomMessage(null, reply.getMessage());

    StringBuffer strbuf = new StringBuffer();
    strbuf.append("Message: ").append(msg.getJMSMessageID());
    strbuf.append("\n\tTo: ").append(msg.getJMSDestination());
    strbuf.append("\n\tCorrelationId: ").append(msg.getJMSCorrelationID());
    strbuf.append("\n\tDeliveryMode: ").append(msg.getJMSDeliveryMode());
    strbuf.append("\n\tExpiration: ").append(msg.getJMSExpiration());
    strbuf.append("\n\tPriority: ").append(msg.getJMSPriority());
    strbuf.append("\n\tRedelivered: ").append(msg.getJMSRedelivered());
    strbuf.append("\n\tReplyTo: ").append(msg.getJMSReplyTo());
    strbuf.append("\n\tTimestamp: ").append(msg.getJMSTimestamp());
    strbuf.append("\n\tType: ").append(msg.getJMSType());
    return strbuf.toString();
  }

  public Properties getMessageHeader(String subName,
                                     String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, false));
    Message msg =  Message.wrapMomMessage(null, reply.getMessage());

    Properties prop = new Properties();
    prop.setProperty("JMSMessageID", msg.getJMSMessageID());
    prop.setProperty("JMSDestination", msg.getJMSDestination().toString());
    if (msg.getJMSCorrelationID() != null)
      prop.setProperty("JMSCorrelationID", msg.getJMSCorrelationID());
    prop.setProperty("JMSDeliveryMode",
                     new Integer(msg.getJMSDeliveryMode()).toString());
    prop.setProperty("JMSExpiration",
                     new Long(msg.getJMSExpiration()).toString());
    prop.setProperty("JMSPriority",
                     new Integer(msg.getJMSPriority()).toString());
    prop.setProperty("JMSRedelivered",
                     new Boolean(msg.getJMSRedelivered()).toString());
    if (msg.getJMSReplyTo() != null)
      prop.setProperty("JMSReplyTo", msg.getJMSReplyTo().toString());
    prop.setProperty("JMSTimestamp",
                     new Long(msg.getJMSTimestamp()).toString());
    if (msg.getJMSType() != null)
      prop.setProperty("JMSType", msg.getJMSType());

    return prop;
  }

  public Properties getMessageProperties(String subName,
                                         String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, false));
    Message msg =  Message.wrapMomMessage(null, reply.getMessage());

    Properties prop = new Properties();
    msg.getProperties(prop);

    return prop;
  }

  /**
   * Add interceptors
   * 
   * @param interceptors list of string className interceptor (separate with ",")
   * @throws ConnectException
   * @throws AdminException
   */
  public void addInterceptorsIN(String interceptors) throws ConnectException, AdminException {
  	Properties prop = new Properties();
    prop.put(AdminCommandConstant.INTERCEPTORS_IN, interceptors);
    getWrapper().processAdmin(getProxyId(), AdminCommandConstant.CMD_ADD_INTERCEPTORS, prop);
  }
  
  /**
   * Get interceptors.
   * 
   * @return list of string className interceptor (separate with ",")
   * @throws ConnectException
   * @throws AdminException
   */
  public String getInterceptorsIN() throws ConnectException, AdminException {
    AdminCommandReply reply = (AdminCommandReply) getWrapper().processAdmin(getProxyId(),
        AdminCommandConstant.CMD_GET_INTERCEPTORS, null);
    return (String) reply.getProp().get(AdminCommandConstant.INTERCEPTORS_IN);
  }
  
  /**
   * Remove interceptors 
   * 
   * @param interceptors list of string className interceptor (separate with ",")
   * @throws ConnectException
   * @throws AdminException
   */
  public void removeInterceptorsIN(String interceptors) throws ConnectException, AdminException {
  	Properties prop = new Properties();
  	prop.put(AdminCommandConstant.INTERCEPTORS_IN, interceptors);
    getWrapper().processAdmin(getProxyId(), AdminCommandConstant.CMD_REMOVE_INTERCEPTORS, prop);
  }
  
  /**
   * Add interceptors
   * 
   * @param interceptors list of string className interceptor (separate with ",")
   * @throws ConnectException
   * @throws AdminException
   */
  public void addInterceptorsOUT(String interceptors) throws ConnectException, AdminException {
  	Properties prop = new Properties();
    prop.put(AdminCommandConstant.INTERCEPTORS_OUT, interceptors);
    getWrapper().processAdmin(getProxyId(), AdminCommandConstant.CMD_ADD_INTERCEPTORS, prop);
  }
  
  /**
   * Get interceptors.
   * 
   * @return list of string className interceptor (separate with ",")
   * @throws ConnectException
   * @throws AdminException
   */
  public String getInterceptorsOUT() throws ConnectException, AdminException {
    AdminCommandReply reply = (AdminCommandReply) getWrapper().processAdmin(getProxyId(),
        AdminCommandConstant.CMD_GET_INTERCEPTORS, null);
    return (String) reply.getProp().get(AdminCommandConstant.INTERCEPTORS_OUT);
  }
  
  /**
   * Remove interceptors 
   * 
   * @param interceptors list of string className interceptor (separate with ",")
   * @throws ConnectException
   * @throws AdminException
   */
  public void removeInterceptorsOUT(String interceptors) throws ConnectException, AdminException {
  	Properties prop = new Properties();
  	prop.put(AdminCommandConstant.INTERCEPTORS_OUT, interceptors);
    getWrapper().processAdmin(getProxyId(), AdminCommandConstant.CMD_REMOVE_INTERCEPTORS, prop);
  }
  
  /**
   * Replace interceptor IN
   * 
   * @param newInterceptor the new className interceptor.
   * @param oldInterceptor the old className interceptor.
   * @throws ConnectException
   * @throws AdminException
   */
  public void replaceInterceptorIN(String newInterceptor, String oldInterceptor) throws ConnectException, AdminException {
  	Properties prop = new Properties();
    prop.put(AdminCommandConstant.INTERCEPTORS_IN_NEW, newInterceptor);
    prop.put(AdminCommandConstant.INTERCEPTORS_IN_OLD, oldInterceptor);
    getWrapper().processAdmin(getProxyId(), AdminCommandConstant.CMD_REPLACE_INTERCEPTORS, prop);
  }
  
  /**
   * Replace interceptor OUT
   * 
   * @param newInterceptor the new className interceptor.
   * @param oldInterceptor the old className interceptor.
   * @throws ConnectException
   * @throws AdminException
   */
  public void replaceInterceptorOUT(String newInterceptor, String oldInterceptor) throws ConnectException, AdminException {
  	Properties prop = new Properties();
    prop.put(AdminCommandConstant.INTERCEPTORS_OUT_NEW, newInterceptor);
    prop.put(AdminCommandConstant.INTERCEPTORS_OUT_OLD, oldInterceptor);
    getWrapper().processAdmin(getProxyId(), AdminCommandConstant.CMD_REPLACE_INTERCEPTORS, prop);
  }
  
  /**
   * @deprecated
   */
  public Message readMessage(String subName,
                             String msgId) throws AdminException, ConnectException, JMSException {
    return getMessage(subName, msgId);
  }

  public void deleteMessage(String subName, 
                            String msgId)  throws AdminException, ConnectException {
    doRequest(new DeleteSubscriptionMessage(proxyId, subName, msgId));
  }

  public void clearSubscription(String subName) throws AdminException, ConnectException {
    doRequest(new ClearSubscription(proxyId, subName));
  }


  /** Returns the identifier of the user's proxy. */
  public String getProxyId() {
    return proxyId;
  }

  /** Sets the naming reference of a connection factory. */
  public void toReference(Reference ref) throws NamingException {
    ref.add(new StringRefAddr("user.name", name));
    ref.add(new StringRefAddr("user.id", proxyId));
  }

  /** Restores the administered object from a naming reference. */
  public void fromReference(Reference ref) throws NamingException {
    name = (String) ref.get("user.name").getContent();
    proxyId = (String) ref.get("user.id").getContent();
  }
}
