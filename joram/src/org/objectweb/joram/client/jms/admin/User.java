/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package org.objectweb.joram.client.jms.admin;

import java.util.Vector;
import java.util.Hashtable;
import java.net.ConnectException;

import javax.naming.*;

import org.objectweb.joram.shared.admin.*;

/**
 * The <code>User</code> class is a utility class needed for administering
 * JORAM users.
 */
public class User extends AdministeredObject
{
  /** The name of the user. */
  private String name;
  /** Identifier of the user's proxy agent. */
  private String proxyId;


  /**
   * Constructs an <code>User</code> instance.
   *
   * @param name  The name of the user.
   * @param proxyId  Identifier of the user's proxy agent.
   */
  public User(String name, String proxyId)
  {
    super(proxyId);
    this.name = name;
    this.proxyId = proxyId;
  }

  
  /** Returns a string view of this <code>User</code> instance. */
  public String toString()
  {
    return "User[" + name + "]:" + proxyId;
  }


  /** Returns the user name. */
  public String getName()
  {
    return name;
  }

  /** Provides a reliable way to compare <code>User</code> instances. */
  public boolean equals(Object o)
  {
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
         throws ConnectException, AdminException
  {
    AdminReply reply = AdminModule.doRequest(
      new CreateUserRequest(name, password, serverId));
    return new User(name, ((CreateUserReply) reply).getProxId());
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
         throws ConnectException, AdminException
  {
    return create(name, password, AdminModule.getLocalServer());
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
         throws ConnectException, AdminException
  {
    AdminModule.doRequest(new UpdateUser(name, proxyId, newName, newPassword));
    name = newName;
  }

  /**
   * Removes this user.
   *
   * @exception ConnectException  If the connection fails.
   * @exception AdminException  Never thrown.
   */
  public void delete() throws ConnectException, AdminException
  {
    AdminModule.doRequest(new DeleteUser(name, proxyId));
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
  public void setDMQ(DeadMQueue dmq) throws ConnectException, AdminException
  {
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
  public void setThreshold(int thresh) throws ConnectException, AdminException
  {
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
  public DeadMQueue getDMQ() throws ConnectException, AdminException
  {
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
  public int getThreshold() throws ConnectException, AdminException
  {
    Monitor_GetDMQSettings request;
    request = new Monitor_GetDMQSettings(proxyId);
    Monitor_GetDMQSettingsRep reply;
    reply = (Monitor_GetDMQSettingsRep) AdminModule.doRequest(request);
    
    if (reply.getThreshold() == null)
      return -1;
    else
      return reply.getThreshold().intValue();
  }

   
  /** Returns the identifier of the user's proxy. */
  public String getProxyId()
  {
    return proxyId;
  }

  /** Sets the naming reference of this user. */
  public Reference getReference() throws NamingException
  {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("user.name", name));
    ref.add(new StringRefAddr("user.id", proxyId));
    return ref;
  }

  /**
   * Codes an <code>User</code> instance as a Hashtable for travelling 
   * through the SOAP protocol.
   */
  public Hashtable code() {
    Hashtable h = super.code();
    h.put("name",name);
    h.put("proxyId",proxyId);
    return h;
  }

  /**
   * Decodes an <code>User</code> which travelled through the SOAP protocol.
   */
  public Object decode(Hashtable h) {
    return new User((String) h.get("name"), (String) h.get("proxyId"));
  }
}
