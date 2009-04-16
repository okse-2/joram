/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * A <code>CreateUserRequest</code> instance requests the creation of a JMS
 * user proxy.
 */
public class CreateUserRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identity contain Name of the user and password or Subjet */
  private Identity identity;
  /** Id of the server where deploying the proxy. */
  private int serverId;

  /**
   * Constructs a <code>CreateUserRequest</code> instance.
   *
   * @param userName  The name of the user.
   * @param userPass  The password of the user.
   * @param serverId  The id of the server where deploying its proxy.
   */
  public CreateUserRequest(Identity identity, int serverId) {
    this.identity = identity;
    this.serverId = serverId;
  }

  public CreateUserRequest() { }
  
  /**
   * @return identity (contains the name and 
   * password or Subject of the user to create).
   */
  public Identity getIdentity() {
    return identity;
  }

  /** Returns the id of the server where deploying its proxy. */
  public int getServerId() {
    return serverId;
  }
  
  protected int getClassId() {
    return CREATE_USER_REQUEST;
  }
  
  public void readFrom(InputStream is) throws IOException {
    serverId = StreamUtil.readIntFrom(is);
    try {
      identity = Identity.read(is);
    } catch (Exception e) {
      throw new IOException(e.getClass() + ":: " + e.getMessage());
    }
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(serverId, os);
    Identity.write(identity, os);
  }
}
