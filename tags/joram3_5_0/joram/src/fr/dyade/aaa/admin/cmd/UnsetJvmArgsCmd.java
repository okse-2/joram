/*
 * Copyright (C) 2000 SCALAGENT 
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

package fr.dyade.aaa.admin.cmd;

import java.io.*;
import java.util.*;

public class UnsetJvmArgsCmd implements AdminCmd, Serializable {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: UnsetJvmArgsCmd.java,v 1.1 2003-06-23 13:36:06 fmaistre Exp $"; 

  public String serverName = null;
  public String args = null;

 /**
  * unset server jvm args
  *
  * @param serverName  server name
  * @param args        jvm args
  */
  public UnsetJvmArgsCmd(String serverName, String args) {
    this.serverName = serverName;
    this.args = args;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("UnsetJvmArgsCmd(");
    buf.append("serverName=");
    buf.append(serverName);
    buf.append(",args=");
    buf.append(args);
    buf.append(")");
    return buf.toString();
  }
}
