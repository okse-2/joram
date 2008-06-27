/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;

public class UserAdminRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  private String userId;

  public UserAdminRequest(String userId) {
    this.userId = userId;
  }

  public UserAdminRequest() { }
  
  public final String getUserId() {
    return userId;
  }

  public String toString() {
    return '(' + super.toString() + ",userId=" + userId + ')';
  }
  
  protected int getClassId() {
    return USER_ADMIN_REQUEST;
  }
  
  public void readFrom(InputStream is) throws IOException {
    userId = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(userId, os);
  }
}
