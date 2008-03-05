/*
 * Copyright (C) 2000 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util;

import java.util.*;

public class ServerAddress implements java.io.Serializable {

  public static ServerAddress valueOf(String s) throws Exception {
    StringTokenizer tokenizer = new StringTokenizer(s, "=:");
    String serverName = tokenizer.nextToken();
    String hostName = tokenizer.nextToken();
    String portS = tokenizer.nextToken();
    int port = Integer.valueOf(portS).intValue();
    return new ServerAddress(serverName, hostName, port);
  }

  private String serverName;

  private String hostName;

  private int port;

  public ServerAddress(String serverName,
                       String hostName,
                       int port) {
    this.serverName = serverName;
    this.hostName = hostName;
    this.port = port;
  }

  public final String getServerName() {
    return serverName;
  }

  public final String getHostName() {
    return hostName;
  }

  public final int getPort() {
    return port;
  }

  public String toString() {
    return serverName + '=' + hostName + ':' + port;
  }

  public boolean equals(Object obj) {
    if (obj instanceof ServerAddress) {
      ServerAddress sa = (ServerAddress)obj;
      if (! sa.serverName.equals(serverName)) return false;
      if (! sa.hostName.equals(hostName)) return false;
      return (sa.port == port);
    } else {
      return false;
    }
  }
}
