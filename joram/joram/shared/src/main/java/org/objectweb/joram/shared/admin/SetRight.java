/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2010 ScalAgent Distributed Technologies
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
 * A <code>SetRight</code> instance requests a given right to be granted to a
 * given user.
 */
public abstract class SetRight extends DestinationAdminRequest {

  /** Identifier of the user's proxy, <code>null</code> for all users. */
  private String userProxId;

  /**
   * Constructs a <code>SetRight</code> instance.
   *
   * @param userProxId  The identifier of the user's proxy, <code>null</code>
   *          for all users.
   * @param destId  The identifier of the destination.
   */
  public SetRight(String userProxId, String destId) {
    super(destId);
    this.userProxId = userProxId;
  }

  public SetRight() { }
  
  /** Returns the identifier of the future reader's proxy. */
  public String getUserProxId() {
    return userProxId;
  }
  
  protected int getClassId() {
    return SET_RIGHT;
  }
  
  public void readFrom(InputStream is) throws IOException {
    userProxId = StreamUtil.readStringFrom(is);
    super.readFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(userProxId, os);
    super.writeTo(os);
  }
}
