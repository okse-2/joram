/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2010 ScalAgent Distributed Technologies
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
import java.util.Enumeration;
import java.util.Hashtable;

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>Monitor_GetUsersRep</code> instance replies to a get users,
 * readers or writers monitoring request.
 */
public class GetUsersReply extends AdminReply {
  private static final long serialVersionUID = 1L;

  /** Table holding the users identifications. */
  private Hashtable users;

  /**
   * Constructs a <code>Monitor_GetUsersRep</code> instance.
   */
  public GetUsersReply(Hashtable users) {
    super(true, null);
    this.users = users;
  }

  /** Returns the users table. */
  public Hashtable getUsers() {
    return users;
  }

  public GetUsersReply() {}

  protected int getClassId() {
    return GET_USERS_REPLY;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);   
    int size = StreamUtil.readIntFrom(is);
    if (size == -1) {
      users = null;
    } else {
      users = new Hashtable(size*4/3);
      for (int i=0; i< size; i++) {
        String key = StreamUtil.readStringFrom(is);
        String value = StreamUtil.readStringFrom(is);
        users.put(key, value);
      }
    }
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);   
    if (users == null) {
      StreamUtil.writeTo(-1, os);
    } else {
      int size = users.size();
      StreamUtil.writeTo(size, os);
      for (Enumeration keys = users.keys(); keys.hasMoreElements(); ) {
        String key = (String) keys.nextElement();
        StreamUtil.writeTo(key, os);
        String value = (String) users.get(key);
        StreamUtil.writeTo(value, os);
      }
    }
  }
}
