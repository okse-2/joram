/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies 
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
package fr.dyade.aaa.admin.cmd;

import java.io.*;
import java.util.*;

public class NewNetworkCmd extends NetworkCmd implements Serializable {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public int port;

  /**
   * create new network
   *
   * @param serverName  server name
   * @param domain      domain name
   * @param port        port
   */
  public NewNetworkCmd(String serverName, String domain, int port) {
    super(serverName, domain);
    this.port = port;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",port=" + port + ')';
  }
}
