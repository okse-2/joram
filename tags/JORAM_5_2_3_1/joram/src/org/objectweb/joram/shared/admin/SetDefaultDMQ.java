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

import org.objectweb.joram.shared.stream.StreamUtil;

/**
 * A <code>SetDefaultDMQ</code> instance requests to set a given DMQ as the
 * default DMQ for a given server.
 */
public class SetDefaultDMQ extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of the server the DMQ is set for. */
  private int serverId;
  /** Identifier of the DMQ. */
  private String dmqId;

  /**
   * Constructs a <code>SetDefaultDMQ</code> instance.
   *
   * @param serverId  Identifier of the server the DMQ is set for.
   * @param dmqId  Identifier of the DMQ.
   */
  public SetDefaultDMQ(int serverId, String dmqId) {
    this.serverId = serverId;
    this.dmqId = dmqId;
  }

  public SetDefaultDMQ() { }
  
  /** Returns the identifier of the server the DMQ is set for. */
  public int getServerId() {
    return serverId;
  }

  /** Returns the identifier of the DMQ. */
  public String getDmqId() {
    return dmqId;
  }
  
  protected int getClassId() {
    return SET_DEFAULT_DMQ;
  }
  
  public void readFrom(InputStream is) throws IOException {
    serverId = StreamUtil.readIntFrom(is);
    dmqId = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(serverId, os);
    StreamUtil.writeTo(dmqId, os);
  }
}
