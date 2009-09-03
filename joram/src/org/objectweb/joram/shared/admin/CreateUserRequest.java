/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2003 Dyade
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
package org.objectweb.joram.shared.admin;

/**
 * A <code>CreateUserRequest</code> instance requests the creation of a JMS
 * user proxy.
 */
public class CreateUserRequest extends AdminRequest {
  private static final long serialVersionUID = 5772076673534562231L;

  /** Name of the user. */
  private String userName;
  /** Password of the user. */
  private String userPass;
  /** Id of the server where deploying the proxy. */
  private int serverId;

  /**
   * Constructs a <code>CreateUserRequest</code> instance.
   *
   * @param userName  The name of the user.
   * @param userPass  The password of the user.
   * @param serverId  The id of the server where deploying its proxy.
   */
  public CreateUserRequest(String userName, String userPass, int serverId) {
    this.userName = userName;
    this.userPass = userPass;
    this.serverId = serverId;
  }

  /** Returns the name of the user to create. */
  public String getUserName() {
    return userName;
  }
  
  /** Returns the password of the user. */
  public String getUserPass() {
    return userPass;
  }

  /** Returns the id of the server where deploying its proxy. */
  public int getServerId() {
    return serverId;
  }
}
