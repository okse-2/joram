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

public class SetServerNatCmd implements AdminCmd, Serializable {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: SetServerNatCmd.java,v 1.1 2003-06-23 13:36:06 fmaistre Exp $"; 

  public String serverName = null;
  public String translationServerName = null;
  public String translationHostName = null;
  public int translationHostPort = -1;

 /**
  * set server jvm args
  *
  * @param serverName               server name
  * @param translationServerName    nat server name
  * @param translationHostName      nat host name
  * @param translationHostPort      nat host port
  */
  public SetServerNatCmd(String serverName,
                         String translationServerName,
                         String translationHostName,
                         int translationHostPort) {
    this.serverName = serverName;
    this.translationServerName = translationServerName;
    this.translationHostName = translationHostName;
    this.translationHostPort = translationHostPort;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("SetServerNatCmd(");
    buf.append("serverName=");
    buf.append(serverName);
    buf.append(",translationServerName=");
    buf.append(translationServerName);
    buf.append(",translationHostName=");
    buf.append(translationHostName);
    buf.append(",translationHostPort=");
    buf.append(translationHostPort);
    buf.append(")");
    return buf.toString();
  }
}
