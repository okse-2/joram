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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * A <code>Monitor_GetDMQSettings</code> instance requests the DMQ settings
 * of a server, a user or a destination.
 */
public class Monitor_GetDMQSettings extends Monitor_Request {
  private static final long serialVersionUID = 1L;

  /** Identifier of the target server. */
  private int serverId = -1;
  /** Identifier of the target queue or user. */
  private String target = null;
  
  /**
   * Constructs a <code>Monitor_GetDMQSettings</code> instance.
   *
   * @param serverId  Identifier of the target server. 
   */
  public Monitor_GetDMQSettings(int serverId) {
    this.serverId = serverId;
  }

  /**
   * Constructs a <code>Monitor_GetDMQSettings</code> instance.
   *
   * @param target  Identifier of the target destination or user.
   */
  public Monitor_GetDMQSettings(String target) {
    this.target = target;
  }

  public Monitor_GetDMQSettings() { }
  
  /** Returns the identifier of the target server. */
  public int getServerId() {
    return serverId;
  }

  /** Returns the identifier of the target destination or user. */
  public String getTarget() {
    return target;
  }
  
  protected int getClassId() {
    return MONITOR_GET_DMQ_SETTINGS;
  }
  
  public void readFrom(InputStream is) throws IOException {
    super.readFrom(is);
    serverId = StreamUtil.readIntFrom(is);
    target = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    super.writeTo(os);
    StreamUtil.writeTo(serverId, os);
    StreamUtil.writeTo(target, os);
  }
}
