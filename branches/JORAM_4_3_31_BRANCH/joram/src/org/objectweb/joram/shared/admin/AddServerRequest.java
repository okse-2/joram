/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.shared.admin;

public class AddServerRequest extends AdminRequest {

  private static final long serialVersionUID = -4803635763566324018L;

  private String serverName;
  private String hostName;
  private int serverId;
  private String domainName;
  private int port;
  private String[] serviceNames;
  private String[] serviceArgs;

  public AddServerRequest(int serverId,
                          String hostName,
                          String domainName,
                          int port,
                          String serverName,
                          String[] serviceNames,
                          String[] serviceArgs) {
    this.serverId = serverId;
    this.hostName = hostName;
    this.serverName = serverName;
    this.domainName = domainName;
    this.port = port;
    this.serviceNames = serviceNames;
    this.serviceArgs = serviceArgs;
  }

  public final String getServerName() {
    return serverName;
  }

  public final String getHostName() {
    return hostName;
  }

  public final int getServerId() {
    return serverId;
  }

  public final String getDomainName() {
    return domainName;
  }

  public final int getPort() {
    return port;
  }

  public final String[] getServiceNames() {
    return serviceNames;
  }

  public final String[] getServiceArgs() {
    return serviceArgs;
  }
}
