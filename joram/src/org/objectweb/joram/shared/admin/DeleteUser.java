/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>DeleteUser</code> instance requests the deletion of a user proxy
 * on a given server.
 */
public class DeleteUser extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Name of the user to delete. */
  private String userName;
  /** Identifier of the user's proxy. */
  private String proxId;

  /**
   * Constructs a <code>DeleteUser</code> instance.
   *
   * @param userName  The name of the user to delete.
   * @param proxId  The identifier of the user's proxy.
   */
  public DeleteUser(String userName, String proxId) {
    this.userName = userName;
    this.proxId = proxId;
  }

  public DeleteUser() { }
  
  /** Returns the name of the user to delete. */
  public String getUserName() {
    return userName;
  }

  /** Returns the identifier of the user's proxy. */
  public String getProxId() {
    return proxId;
  }
  
  protected int getClassId() {
    return DELETE_USER;
  }
  
  public void readFrom(InputStream is) throws IOException {
    userName = StreamUtil.readStringFrom(is);
    proxId = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(userName, os);
    StreamUtil.writeTo(proxId, os);
  }
}
