/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.agent;

import java.util.Enumeration;

import fr.dyade.aaa.common.monitoring.DumpAttributes;

public class SCServer implements SCServerMBean {
  public SCServer() {
  }

  public short getServerId() {
    return AgentServer.getServerId();
  }

  public String getName() {
    return AgentServer.getName();
  }

  public void start() {
    try {
      AgentServer.start();
    } catch (Throwable exc) {
    }
  }

  public void stop() {
    AgentServer.stop(false);
  }

  public int getStatus() {
    return AgentServer.getStatus();
  }

  public String getStatusInfo() {
    return AgentServer.getStatusInfo();
  }

  public String[] getServers() {
    Enumeration<ServerDesc> e = AgentServer.elementsServerDesc();
    String[] servers = new String[AgentServer.getServerNb()];
    StringBuffer strBuf = new StringBuffer();
    for (int i=0; e.hasMoreElements(); i++) {
      ServerDesc server = e.nextElement();
      strBuf.append("sid=").append(server.sid);
      strBuf.append(",name=").append(server.name);
      if (server.gateway == -1) {
        strBuf.append(",gateway=").append(server.gateway);
      } else {
        strBuf.append(",host=").append(server.getHostname())
          .append(':').append(server.getPort());
        strBuf.append(",active=").append(server.active);
        strBuf.append(",last=").append(server.last);
      }
      servers[i] = strBuf.toString();
      strBuf.setLength(0);
    }
    return servers;
  }
  
  public void dumpAttributes(String name, String path) {
    DumpAttributes.dumpAttributes(name, path);
  }
}
