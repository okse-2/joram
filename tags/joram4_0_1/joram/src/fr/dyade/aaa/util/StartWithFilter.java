/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */

package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

public class StartWithFilter implements FilenameFilter {

public static final String RCS_VERSION="@(#)$Id: StartWithFilter.java,v 1.16 2004-03-16 10:03:45 fmaistre Exp $"; 

  private String prefix;

  public StartWithFilter(String prefix) {
    this.prefix = prefix;
  }

  public boolean accept(File dir, String name) {
    return name.startsWith(prefix);
  }
}
