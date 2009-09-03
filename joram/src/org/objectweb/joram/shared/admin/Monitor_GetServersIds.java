/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
 * A <code>Monitor_GetServersIds</code> instance requests the list of
 * the platform's servers' identifiers.
 */
public class Monitor_GetServersIds extends Monitor_Request {
  private static final long serialVersionUID = -219223518933031700L;

  /** Identifier of the target server. */
  private int serverId;

  private String domainName;

  /**
   * Constructs a <code>Monitor_GetServersIds</code> instance.
   *
   * @param serverId  Identifier of the target server.
   */
  public Monitor_GetServersIds(int serverId) {
    this(serverId, null);
  }

  /**
   * Constructs a <code>Monitor_GetServersIds</code> instance.
   *
   * @param serverId  Identifier of the target server.
   * @param domainName Name of the domain that contains
   *                   the servers.
   */
  public Monitor_GetServersIds(int serverId,
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
}
