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

public interface Transaction {
  public static final String RCS_VERSION="@(#)$Id: Transaction.java,v 1.13 2003-06-23 13:45:20 fmaistre Exp $"; 

  static final String separator = "_";

  void init(String path) throws IOException;

  void begin() throws IOException;

  File getDir();
  String[] getList(String prefix);

  void save(Serializable obj, String name) throws IOException;
  Object load(String name) throws IOException, ClassNotFoundException;
  void delete(String name);

  void save(Serializable obj, String dirName, String name) throws IOException;
  Object load(String dirName, String name) throws IOException, ClassNotFoundException;
  void delete(String dirName, String name);

  void commit() throws IOException;
  void rollback() throws IOException;

  void release() throws IOException;

  void stop();
}