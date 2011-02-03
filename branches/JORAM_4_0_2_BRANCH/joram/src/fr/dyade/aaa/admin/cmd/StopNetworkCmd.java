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

public class StopNetworkCmd implements StopAdminCmd, Serializable {
  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: StopNetworkCmd.java,v 1.5 2004-03-16 10:03:45 fmaistre Exp $"; 

  public short sid;

  public String domainName;

  /**
   * Constructs a new stop network command.
   *
   * @param sid         server id
   * @param domainName  domain name
   */
  public StopNetworkCmd(short sid,
                        String domainName) {
    this.sid = sid;
    this.domainName = domainName;
  }

  public String toString() {
    return '(' + super.toString() + 
      ",sid=" + sid + 
      ",domainName=" + domainName + ')';
  }
}