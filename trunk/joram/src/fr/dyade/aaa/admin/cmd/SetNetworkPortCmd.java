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

public class SetNetworkPortCmd extends NetworkCmd {
  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: SetNetworkPortCmd.java,v 1.1 2003-09-11 09:51:41 fmaistre Exp $"; 

 /**
  * @param serverName  server name
  * @param domain      domain name
  * @param port        port
  */
  public SetNetworkPortCmd(String serverName, 
                           String domain, 
                           int port) {
    super(serverName, domain, port);
  }

  public String toString() {
    return "SetNetworkPortCmd" + super.toString();
  }
}
