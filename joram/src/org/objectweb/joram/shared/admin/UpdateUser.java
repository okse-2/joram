/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * An <code>UpdateUser</code> instance requests the modification of a
 * user identification
 */
public class UpdateUser extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Name of the user. */
  private String userName;
  /** Identifier of the user's proxy. */
  private String proxId;
  /** Identity contain Name of the new user and new password or new Subjet */
  private Identity newIdentity;

  /**
   * Constructs an <code>UpdateUser</code> instance.
   *
   * @param userName     The name of the user.
   * @param proxId       Identifier of the user's proxy.
   * @param newIdentity  The new identity of the user.
   */
  public UpdateUser(String userName, 
                    String proxId,
                    Identity newIdentity) {
    this.userName = userName;
    this.proxId = proxId;
    this.newIdentity = newIdentity;
  }

  public UpdateUser() { }
  
  /** Returns the name of the user to update. */
  public String getUserName() {
    return userName;
  }

  /** Returns the user's proxy identifier. */
  public String getProxId() {
    return proxId;
  }

  /** Returns the new identity of the user. */
  public Identity getNewIdentity() {
    return newIdentity;
  }
  
  protected int getClassId() {
    return UPDATE_USER;
  }
  
  public void readFrom(InputStream is) throws IOException {
    userName = StreamUtil.readStringFrom(is);
    proxId  = StreamUtil.readStringFrom(is);
    try {
      newIdentity = Identity.read(is);
    } catch (Exception e) {
      throw new IOException(e.getClass() + ":: " + e.getMessage());
    }
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(userName, os);
    StreamUtil.writeTo(proxId, os);
    Identity.write(newIdentity, os);
  }
}
