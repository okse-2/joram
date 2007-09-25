/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.admin;

/**
 * An <code>UpdateUser</code> instance requests the modification of a
 * user identification
 */
public class UpdateUser extends AdminRequest {
  private static final long serialVersionUID = 5991002585349654595L;

  /** Name of the user. */
  private String userName;
  /** Identifier of the user's proxy. */
  private String proxId;
  /** New name of the user. */
  private String newName;
  /** New password of the user. */
  private String newPass;

  /**
   * Constructs an <code>UpdateUser</code> instance.
   *
   * @param userName  The name of the user.
   * @param proxId  Identifier of the user's proxy.
   * @param newName  The new name of the user.
   * @param newPass  The new password of the user.
   */
  public UpdateUser(String userName, 
                    String proxId,
                    String newName,
                    String newPass) {
    this.userName = userName;
    this.proxId = proxId;
    this.newName = newName;
    this.newPass = newPass;
  }

  /** Returns the name of the user to update. */
  public String getUserName() {
    return userName;
  }

  /** Returns the user's proxy identifier. */
  public String getProxId() {
    return proxId;
  }

  /** Returns the new name of the user. */
  public String getNewName() {
    return newName;
  }
  
  /** Returns the new password of the user. */
  public String getNewPass() {
    return newPass;
  }
}
