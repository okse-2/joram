/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2007 ScalAgent Distributed Technologies
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

import java.util.Vector;
import java.util.Hashtable;
import java.util.List;
import java.net.ConnectException;

import javax.naming.*;

import javax.jms.JMSException;

import fr.dyade.aaa.util.management.MXWrapper;
import org.objectweb.joram.client.jms.Message;
import org.objectweb.joram.shared.admin.*;

import org.objectweb.joram.shared.JoramTracing;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * The <code>User</code> class is a utility class needed for administering
 * JORAM users.
 */
public class User extends AdministeredObject implements UserMBean {
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
    AdminReply reply = AdminModule.doRequest(
      new CreateUserRequest(name, password, serverId));
    User user = new User(name, ((CreateUserReply) reply).getProxId());
    try {
      MXWrapper.registerMBean(user,
                              "joramClient",
                              "type=User,name="+name+
                              "["+user.getProxyId()+"]");
    } catch (Exception e) {
      if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
        JoramTracing.dbgClient.log(BasicLevel.DEBUG,
                                   "registerMBean",e);
    }
    return user;
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
    return create(name, password, AdminModule.getLocalServerId());
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
    AdminModule.doRequest(new UpdateUser(name, proxyId, newName, newPassword));
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
    AdminModule.doRequest(new SetUserDMQ(proxyId, dmq.getName()));
  }

  /**
   * Admin method setting a given value as the threshold for this user.
   * <p>
   * The request fails if the user is deleted server side.
   *
   * @param threshold  The threshold value to be set.
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
    Subscription sub = getSubscription(subName);
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
    Subscription sub = getSubscription(subName);
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
  public Subscription getSubscription(String subName) 
    throws AdminException, ConnectException {
    GetSubscriptionRep reply = 
      (GetSubscriptionRep)AdminModule.doRequest(
        new GetSubscription(proxyId, subName));
    return new Subscription(
      subName,
      reply.getTopicId(),
      reply.getMessageCount(),
      reply.getDurable());
  }

  public String getSubscriptionString(String subName) 
    throws AdminException, ConnectException {
    Subscription sub = getSubscription(subName);
    return sub.toString();
  }

  public String[] getMessageIds(String subName) 
    throws AdminException, ConnectException {
    GetSubscriptionMessageIdsRep reply = 
      (GetSubscriptionMessageIdsRep)AdminModule.doRequest(
        new GetSubscriptionMessageIds(proxyId, subName));
    return reply.getMessageIds();
  }

  public Message readMessage(String subName,
                             String msgId) throws AdminException, ConnectException, JMSException {
    GetSubscriptionMessageRep reply = 
      (GetSubscriptionMessageRep) AdminModule.doRequest(
        new GetSubscriptionMessage(proxyId,
                                   subName,
                                   msgId));
    return Message.wrapMomMessage(null, reply.getMessage());
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
    h.put("user.name",name);
    h.put("user.id",proxyId);
    return h;
  }

  /**
   * Decodes an <code>User</code> which travelled through the SOAP protocol.
   */
  public void decode(Hashtable h) {
    name = (String) h.get("user.name");
    proxyId = (String) h.get("user.id");
  }
}
