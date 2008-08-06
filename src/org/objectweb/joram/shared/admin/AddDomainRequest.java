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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package org.objectweb.joram.shared.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;

public class AddDomainRequest extends AdminRequest {
  private static final long serialVersionUID = 1L;
  private String domainName;
  private String network;
  private int serverId;
  private int port;

  public AddDomainRequest(String domainName,
                          String network,
                          int serverId,
                          int port) {
    this.domainName = domainName;
    this.network = network;
    this.serverId = serverId;
    this.port = port;
  }

  public AddDomainRequest(String domainName,
                          int serverId,
                          int port) {
    this.domainName = domainName;
    this.network = null;
    this.serverId = serverId;
    this.port = port;
  }

  public AddDomainRequest() { }
  
  public final String getDomainName() {
    return domainName;
  }
  
  public final String getNetwork() {
    if (network == null)
      return "fr.dyade.aaa.agent.SimpleNetwork";
    return network;
  }

  public final int getServerId() {
    return serverId;
  }

  public final int getPort() {
    return port;
  }

  protected int getClassId() {
    return ADD_DOMAIN_REQUEST;
  }

  public void readFrom(InputStream is) throws IOException {
    domainName = StreamUtil.readStringFrom(is);
    network = StreamUtil.readStringFrom(is);
    serverId = StreamUtil.readIntFrom(is);
    port = StreamUtil.readIntFrom(is);
  }

  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(domainName, os);
    StreamUtil.writeTo(network, os);
    StreamUtil.writeTo(serverId, os);
    StreamUtil.writeTo(port, os);
  }
}
