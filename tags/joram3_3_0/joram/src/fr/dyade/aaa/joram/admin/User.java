/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram.admin;

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

  
  /** Returns a string view of this <code>User</code> instance. */
  public String toString()
  {
    return "User[" + name + "]:" + proxyId;
  }


  /** Sets the naming reference of this user. */
  public Reference getReference() throws NamingException
  {
    Reference ref = super.getReference();
    ref.add(new StringRefAddr("user.name", name));
    ref.add(new StringRefAddr("user.id", proxyId));
    return ref;
  }

  /** Returns the identifier of the user's proxy. */
  public String getProxyId()
  {
    return proxyId;
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
}
