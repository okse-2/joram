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

import fr.dyade.aaa.joram.*;
import fr.dyade.aaa.mom.jms.*;

import java.net.ConnectException;

/**
 * The <code>User</code> class is used by administrators for managing MOM
 * users.
 */
public class User
{
  /** The <code>Admin</code> instance the user belongs to. */
  private Admin admin;
  /** Name of the user. */
  private String userName;
  /** Name of the user's proxy. */
  private String proxyName;

  /** <code>true</code> if the user has been removed. */
  boolean removed;


  /**
   * Constructs a <code>User</code> instance.
   *
   * @param admin  The <code>Admin</code> instance the user belongs to.
   * @param userName  Name of the user.
   * @param proxyName  Name of the user's proxy.
   */
  User(Admin admin, String userName, String proxyName)
  {
    this.admin = admin;
    this.userName = userName;
    this.proxyName = proxyName;
    removed = false;
  }

  /**
   * Updates the user identification.
   *
   * @param newName  User's new name.
   * @param newPass  User's new password.
   * @exception AdminException  If the admin session has been closed, or if
   *              the user has been deleted, or if its new name is already
   *              taken.
   * @exception ConnectException  If the request/reply exchange fails.
   */
  public void modifyId(String newName, String newPass) throws Exception
  {
    if (removed)
      throw new AdminException("Forbidden method call as user "
                               + userName + " does not exist anymore.");

    ModifyUserId modU = new ModifyUserId(userName, newName, newPass);
    admin.sendRequest(modU);
    admin.getReply();
    userName = newName;
  }

  /**
   * Sets a <code>DeadMQueue</code> instance as the DMQ of this user.
   *
   * @param dmq  The DeadMQueue instance.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void setDMQ(DeadMQueue dmq) throws Exception
  {
    SetDeadMQueue setDMQ = new SetDeadMQueue(proxyName, dmq.getQueueName(),
                                             true);
    admin.sendRequest(setDMQ);
    admin.getReply();
  }

  /**
   * Sets a threshold value of maximum authorized delivery attempts before
   * logging a message to the DMQ, for this user.
   *
   * @param threshold  The number of authorized delivery attempts.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void setThreshold(int threshold) throws Exception
  {
    SetThreshold setT = new SetThreshold(proxyName, new Integer(threshold),
                                         true);
    admin.sendRequest(setT);
    admin.getReply();
  }

  /**
   * Sets a <code>DeadMQueue</code> instance as the DMQ of this user.
   *
   * @param dmq  The DeadMQueue instance.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void unsetDMQ() throws Exception
  {
    SetDeadMQueue setDMQ = new SetDeadMQueue(proxyName, null, true);
    admin.sendRequest(setDMQ);
    admin.getReply();
  }

  /**
   * Sets a threshold value of maximum authorized delivery attempts before
   * logging a message to the DMQ, for this user.
   *
   * @param threshold  The number of authorized delivery attempts.
   *
   * @exception ConnectException  If the connection with the server is lost.
   * @exception AdminException  If the admin session has been closed.
   */
  public void unsetThreshold() throws Exception
  {
    SetThreshold setT = new SetThreshold(proxyName, null, true);
    admin.sendRequest(setT);
    admin.getReply();
  }

  /** 
   * Deletes this user's proxy agent and removes its identification from
   * the admin proxy table.
   *
   * @exception AdminException  If the admin session has been closed, or if
   *              the user has already been deleted.
   * @exception ConnectException  If the request/reply exchange fails.
   */
  public void remove() throws Exception
  {
    if (removed)
      throw new AdminException("Forbidden method call as user "
                               + userName + " does not exist anymore.");

    DeleteUser delU = new DeleteUser(userName);
    admin.sendRequest(delU);
    admin.getReply();
    removed = true; 
  }

  /** Returns the name of this user's proxy agent. */
  String getProxyName()
  {
    return proxyName;
  }
}
