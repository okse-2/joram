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

public class NewServiceCmd extends ServiceCmd implements Serializable {
  /** RCS version number of this file: $Revision: 1.5 $ */
  public static final String RCS_VERSION="@(#)$Id: NewServiceCmd.java,v 1.5 2004-03-16 10:03:45 fmaistre Exp $"; 

 /**
  * create new service
  *
  * @param serverName  server name
  * @param className   class name
  * @param args        service args
  */
  public NewServiceCmd(String serverName, String className, String args) {
    super(serverName,className,args);
  }

  public String toString() {
    return "NewServiceCmd" + super.toString();
  }
}
