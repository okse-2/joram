/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.joram.admin;

import java.util.Vector;
import java.util.Hashtable;

import javax.naming.*;


/**
 * The <code>User</code> class allows administrators to manipulate users.
 */
public class User extends AdministeredObject
{
  /** The name of the user. */
  String name;
  /** Identifier of the user's proxy agent. */
  String proxyId;

  /** Used by old admin class. */
  AdminImpl adminImpl = null;


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

  /**
   * Constructs an empty <code>User</code> instance.
   */ 
  public User()
  {}

  
  /** Returns a string view of this <code>User</code> instance. */
  public String toString()
  {
    return "User[" + name + "]:" + proxyId;
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
   * Old administration method.
   *
   * @deprecated  This method is temporary kept but the methods of the new
   *              <code>AdminItf</code> interface should be used instead.
   */
  public void setDMQ(DeadMQueue dmq)
              throws java.net.ConnectException, AdminException
  {
    adminImpl.setUserDMQ(this, dmq);
  }

  /** 
   * Old administration method.
   *
   * @deprecated  This method is temporary kept but the methods of the new
   *              <code>AdminItf</code> interface should be used instead.
   */
  public void unsetDMQ() throws java.net.ConnectException, AdminException
  {
    adminImpl.unsetUserDMQ(this);
  }

  /** 
   * Old administration method.
   *
   * @deprecated  This method is temporary kept but the methods of the new
   *              <code>AdminItf</code> interface should be used instead.
   */
  public void setThreshold(int thresh)
              throws java.net.ConnectException, AdminException
  {
    adminImpl.setUserThreshold(this, thresh);
  }

  /** 
   * Old administration method.
   *
   * @deprecated  This method is temporary kept but the methods of the new
   *              <code>AdminItf</code> interface should be used instead.
   */
  public void unsetThreshold()
              throws java.net.ConnectException, AdminException
  {
    adminImpl.unsetUserThreshold(this);
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
