/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies 
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

import java.io.Serializable;

public class ServerCmd implements AdminCmd, Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public String name = null;
  public String hostname = null;
  public Short sid = null;

 /**
  * persistent server
  *
  * @param name      persistent server name
  * @param hostname  host name
  * @see   NewServerCmd
  * @see   RemoveServerCmd
  */
  public ServerCmd(String name, String hostname) {
    this(name,hostname,null);
  }

 /**
  * server
  *
  * @param name      persistent server name
  * @param hostname  host name
  * @param sid       persistent server id
  * @see   NewServerCmd
  * @see   RemoveServerCmd
  */
  public ServerCmd(String name, 
                   String hostname, 
                   Short sid) {
    this.name = name;
    this.hostname = hostname;
    this.sid = sid;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("(");
    buf.append("name=");
    buf.append(name);
    buf.append(",hostname=");
    buf.append(hostname);
    buf.append(",sid=");
    buf.append(sid);
    buf.append(")");
    return buf.toString();
  }
}
