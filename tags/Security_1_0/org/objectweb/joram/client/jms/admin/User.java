/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
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
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.objectweb.joram.client.jms.Message;
import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.joram.shared.admin.AdminReply;
import org.objectweb.joram.shared.admin.ClearSubscription;
import org.objectweb.joram.shared.admin.CreateUserReply;
import org.objectweb.joram.shared.admin.CreateUserRequest;
import org.objectweb.joram.shared.admin.DeleteSubscriptionMessage;
import org.objectweb.joram.shared.admin.DeleteUser;
import org.objectweb.joram.shared.admin.GetQueueMessage;
import org.objectweb.joram.shared.admin.GetQueueMessageRep;
import org.objectweb.joram.shared.admin.GetSubscription;
import org.objectweb.joram.shared.admin.GetSubscriptionMessage;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIds;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageIdsRep;
import org.objectweb.joram.shared.admin.GetSubscriptionMessageRep;
import org.objectweb.joram.shared.admin.GetSubscriptionRep;
import org.objectweb.joram.shared.admin.GetSubscriptions;
import org.objectweb.joram.shared.admin.GetSubscriptionsRep;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettings;
import org.objectweb.joram.shared.admin.Monitor_GetDMQSettingsRep;
import org.objectweb.joram.shared.admin.Monitor_GetNbMaxMsg;
import org.objectweb.joram.shared.admin.Monitor_GetNbMaxMsgRep;
import org.objectweb.joram.shared.admin.SetNbMaxMsg;
import org.objectweb.joram.shared.admin.SetUserDMQ;
import org.objectweb.joram.shared.admin.SetUserThreshold;
import org.objectweb.joram.shared.admin.UpdateUser;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.util.management.MXWrapper;

/**
 * The <code>User</code> class is a utility class needed for administering
 * JORAM users.
 */
public class User extends AdministeredObject implements UserMBean {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
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

  /** Provides a reliable way to compare <code>User</code> instances. */
  public boolean equals(Object o) {
    if (! (o instanceof User))
      return false;

    User other = (User) o;

    return other.proxyId ==proxyId;
  }
  
  /**
   * Admin method creating a user for a given server and instanciating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password, int serverId)
    throws ConnectException, AdminException {
    return create(name, password, serverId, Identity.SIMPLE_IDENTITY_CLASS);
  }
  
  /**
   * Admin method creating a user on the local server and instanciating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. It fails if a
   * proxy could not be deployed server side for a new user. 
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password)
         throws ConnectException, AdminException {
    return create(name, password, AdminModule.getLocalServerId(), Identity.SIMPLE_IDENTITY_CLASS);
  }
  
  /**
   * Admin method creating a user for a given server and instanciating the
   * corresponding <code>User</code> object.
   * <p>
   * If the user has already been set on this server, the method simply
   * returns the corresponding <code>User</code> object. Its fails if the
   * target server does not belong to the platform, or if a proxy could not
   * be deployed server side for a new user. 
   *
   * @param name  Name of the user.
   * @param password  Password of the user.
   * @param serverId  The identifier of the user's server.
   * @param identityClassName user/password or JAAS... (delault SimpleIdentity).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */ 
  public static User create(String name, String password, int serverId, String identityClassName)
    throws ConnectException, AdminException {
    Identity identity = createIdentity(name, password, identityClassName);
    AdminReply reply = AdminModule.doRequest(
      new CreateUserRequest(identity, serverId));
    User user = new User(name, ((CreateUserReply) reply).getProxId());
    try {
      MXWrapper.registerMBean(user,
                              "joramClient",
                              "type=User,name="+ name + "[" + user.getProxyId() + "]");
    } catch (Exception e) {
      JoramTracing.dbgClient.log(BasicLevel.WARN, "registerMBean",e);
    }
    return user;
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
  private static Identity createIdentity(String user, String passwd, String identityClassName) throws AdminException {
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
  public void update(String newName, String newPassword)
    throws ConnectException, AdminException {
    update(newName, newPassword, Identity.SIMPLE_IDENTITY_CLASS);
  }
  
  /**
   * Admin method updating this user identification.
   * <p>
   * The request fails if the user does not exist server side, or if the new
   * identification is already taken by a user on the same server.
   *
   * @param newName  The new name of the user.
   * @param newPassword  The new password of the user.
   * @param identityClassName user/password or JAAS... (delault SimpleIdentity).
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public void update(String newName, String newPassword, String identityClassName)
    throws ConnectException, AdminException {
    Identity newIdentity = createIdentity(newName, newPassword, identityClassName);
    AdminModule.doRequest(new UpdateUser(name, proxyId, newIdentity));
    name = newName;
  }

  /**
   * Removes this user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void delete() throws ConnectException, AdminException {
    AdminModule.doRequest(new DeleteUser(name, proxyId));
    try {
      MXWrapper.unregisterMBean("joramClient",
                                "type=User,name="+name+
                                "["+proxyId+"]");
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   "unregisterMBean",e);
    }
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
  public void setDMQ(DeadMQueue dmq) throws ConnectException, AdminException {
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
    AdminModule.doRequest(new SetUserDMQ(proxyId, dmqId));
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
  public void setThreshold(int thresh) throws ConnectException, AdminException {
    AdminModule.doRequest(new SetUserThreshold(proxyId, thresh));
  }

  /** 
   * Returns the dead message queue for this user, null if not set.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public DeadMQueue getDMQ() throws ConnectException, AdminException {
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(proxyId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) AdminModule.doRequest(request);
    
    if (reply.getDMQName() == null)
      return null;
    else
      return new DeadMQueue(reply.getDMQName());
  }

  /** 
   * Returns the dead message queue Id for this user, null if not set.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  If the request fails.
   */
  public String getDMQId() throws ConnectException, AdminException {
    DeadMQueue dmq = getDMQ();  
    if (dmq == null)
      return null;
    else
      return dmq.getName();
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
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(proxyId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) AdminModule.doRequest(request);
    
    if (reply.getThreshold() == null)
      return -1;
    else
      return reply.getThreshold().intValue();
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
  public void setNbMaxMsg(String subName, int nbMaxMsg)
    throws ConnectException, AdminException {
//  TODO: Subscription sub = getSubscription(subName);
    AdminModule.doRequest(new SetNbMaxMsg(proxyId, nbMaxMsg, subName));
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
  public int getNbMaxMsg(String subName) 
    throws ConnectException, AdminException {
//  TODO: Subscription sub = getSubscription(subName);
    Monitor_GetNbMaxMsg request = new Monitor_GetNbMaxMsg(proxyId, subName);
    Monitor_GetNbMaxMsgRep reply;
    reply = (Monitor_GetNbMaxMsgRep) AdminModule.doRequest(request);
    return reply.getNbMaxMsg();
  }

  /**
   * Returns the subscriptions owned by a user.
   *
   * @param serverId the identifier of the server where the user has been
   *        created.
   *
   * @param userName name of the user.
   *
   * @exception AdminException If an error is raised by the 
   *                           administration operation.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public Subscription[] getSubscriptions() 
    throws AdminException, ConnectException {
    GetSubscriptionsRep reply = 
      (GetSubscriptionsRep)AdminModule.doRequest(
        new GetSubscriptions(proxyId));
    String[] subNames = reply.getSubNames();
    String[] topicIds = reply.getTopicIds();
    int[] messageCounts = reply.getMessageCounts();
    boolean[] durable = reply.getDurable();
    Subscription[] res = new Subscription[subNames.length];
    for (int i = 0; i < res.length; i++) {
      res[i] = new Subscription(subNames[i],
                                topicIds[i],
                                messageCounts[i],
                                durable[i]);
    }
    return res;
  }

  /** used by MBean jmx */
  public List getSubscriptionList() 
    throws AdminException, ConnectException {
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
   * @param serverId the identifier of the server where the user 
   * owner of the subscription has been created.
   *
   * @param userName name of the user that owns the subscription.
   *
   * @param subName the name of the subscription.
   *
   * @exception AdminException If an error is raised by the 
   *                           administration operation.
   *
   * @exception ConnectException  If the admin connection is not established.
   */
  public Subscription getSubscription(String subName) throws AdminException, ConnectException {
    GetSubscriptionRep reply = (GetSubscriptionRep) AdminModule.doRequest(new GetSubscription(proxyId, subName));
    return new Subscription(subName,
                            reply.getTopicId(),
                            reply.getMessageCount(),
                            reply.getDurable());
  }

  public String getSubscriptionString(String subName) throws AdminException, ConnectException {
    return getSubscription(subName).toString();
  }

  public String[] getMessageIds(String subName) throws AdminException, ConnectException {
    GetSubscriptionMessageIdsRep reply =
      (GetSubscriptionMessageIdsRep) AdminModule.doRequest(new GetSubscriptionMessageIds(proxyId, subName));
    return reply.getMessageIds();
  }

  public Message getMessage(String subName,
                            String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) AdminModule.doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, true));
    return Message.wrapMomMessage(null, reply.getMessage());
  }
  
  public String getMessageDigest(String subName,
                                 String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) AdminModule.doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, true));
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
      (GetSubscriptionMessageRep) AdminModule.doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, false));
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

    // Adds optional header properties
    msg.getOptionalHeader(prop);

    return prop;
}

public Properties getMessageProperties(String subName,
                                       String msgId)
  throws AdminException, ConnectException, JMSException {
  GetSubscriptionMessageRep reply = 
    (GetSubscriptionMessageRep) AdminModule.doRequest(new GetSubscriptionMessage(proxyId, subName, msgId, false));
  Message msg =  Message.wrapMomMessage(null, reply.getMessage());

  Properties prop = new Properties();
  msg.getProperties(prop);

  return prop;
}

  /**
   * @deprecated
   * @see org.objectweb.joram.client.jms.admin.UserMBean#readMessage(java.lang.String, java.lang.String)
   */
  public Message readMessage(String subName,
                             String msgId) throws AdminException, ConnectException, JMSException {
    return getMessage(subName, msgId);
  }

  public void deleteMessage(
    String subName, 
    String msgId) 
    throws AdminException, ConnectException {
    AdminModule.doRequest(
      new DeleteSubscriptionMessage(proxyId,
                                    subName,
                                    msgId));
  }

  public void clearSubscription(String subName)
    throws AdminException, ConnectException {
    AdminModule.doRequest(
      new ClearSubscription(proxyId,
                            subName));
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

  /**
   * Codes an <code>User</code> instance as a Hashtable for travelling 
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = new Hashtable();
    h.put("name",name);
    h.put("proxyId",proxyId);
    return h;
  }

  /**
   * Decodes an <code>User</code> which travelled through the SOAP protocol.
   */
  public void decode(Hashtable h) {
    name = (String) h.get("name");
    proxyId = (String) h.get("proxyId");
  }
}
