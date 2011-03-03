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

import fr.dyade.aaa.common.stream.StreamUtil;

/**
 * A <code>Monitor_GetServersIds</code> instance requests the list of
 * the platform's servers' identifiers.
 */
public class GetServersIdsRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;

  /** Identifier of the target server. */
  private int serverId;

  private String domainName;

  /**
   * Constructs a <code>Monitor_GetServersIds</code> instance.
   *
   * @param serverId  Identifier of the target server.
   */
  public GetServersIdsRequest(int serverId) {
    this(serverId, null);
  }

  public GetServersIdsRequest() { }
  
  /**
   * Constructs a <code>Monitor_GetServersIds</code> instance.
   *
   * @param serverId  Identifier of the target server.
   * @param domainName Name of the domain that contains
   *                   the servers.
   */
  public GetServersIdsRequest(int serverId,
                               String domainName) {
    this.serverId = serverId;
    this.domainName = domainName;
  }

  /** Returns the identifier of the target server. */
  public final int getServerId() {
    return serverId;
  }

  public final String getDomainName() {
    return domainName;
  }
  
  protected int getClassId() {
    return MONITOR_GET_SERVERS_IDS;
  }
  
  public void readFrom(InputStream is) throws IOException {
    serverId = StreamUtil.readIntFrom(is);
    domainName = StreamUtil.readStringFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(serverId, os);
    StreamUtil.writeTo(domainName, os);
  }
}
