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

public class UnsetPropertyCmd implements AdminCmd, Serializable {
  /** RCS version number of this file: $Revision: 1.2 $ */
  public static final String RCS_VERSION="@(#)$Id: UnsetPropertyCmd.java,v 1.2 2003-09-11 09:51:41 fmaistre Exp $"; 

  public String name = null;
  public String value = null;

 /**
  * unset global property
  *
  * @param name   property name
  * @param value  property value
  */
  public UnsetPropertyCmd(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("UnsetPropertyCmd(");
    buf.append("name=");
    buf.append(name);
    buf.append(",value=");
    buf.append(value);
    buf.append(")");
    return buf.toString();
  }
}
