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

public class NewTransientCmd extends TransientCmd implements Serializable {
  /** RCS version number of this file: $Revision: 1.3 $ */
  public static final String RCS_VERSION="@(#)$Id: NewTransientCmd.java,v 1.3 2004-02-13 08:12:03 fmaistre Exp $"; 

 /**
  * create new transient server
  *
  * @param name      transient server name
  * @param hostname  host name
  * @param gateway   gateway name
  */
  public NewTransientCmd(String name, String hostname, String gateway) {
    super(name,hostname,gateway);
  }

 /**
  * create new transient server
  *
  * @param name      transient server name
  * @param hostname  host name
  * @param gateway   gateway name
  * @param sid       server id of transient server
  */
  public NewTransientCmd(String name, 
                         String hostname, 
                         String gateway, 
                         Short sid) {
    super(name,hostname,gateway,sid);
  }

  public String toString() {
    return "NewTransientCmd" + super.toString();
  }
}
