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

public class ServiceCmd implements AdminCmd, Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  public String serverName = null;
  public String className = null;
  public String args = null;

 /**
  * service
  *
  * @param serverName  server name
  * @param className   class name
  * @param args        service args
  * @see   NewServiceCmd
  * @see   RemoveServiceCmd
  */
  public ServiceCmd(String serverName, String className, String args) {
    this.serverName = serverName;
    this.className = className;
    this.args = args;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("(");
    buf.append("serverName=");
    buf.append(serverName);
    buf.append(",className=");
    buf.append(className);
    buf.append(",args=");
    buf.append(args);
    buf.append(")");
    return buf.toString();
  }
}
