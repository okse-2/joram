/*
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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

public class NullTransaction implements Transaction {
  public NullTransaction() {}

  public void init(String path) throws IOException {
  }

  public File getDir() {
    return null;
  }

  public void begin() throws IOException {}

  public String[] getList(String prefix) {
    return new String[0];
  }

  public void save(Serializable obj, String name) throws IOException {}
  
  public void saveByteArray(byte[] buf, String name) throws IOException {}

  public Object load(String name) throws IOException, ClassNotFoundException {
    return null;
  }
  
  public byte[] loadByteArray(String name) throws IOException, ClassNotFoundException {
    return null;
  }

  public void delete(String name) {}

  public void save(Serializable obj, String dirName, String name) throws IOException {}
  
  public void saveByteArray(byte[] buf, String dirName, String name) throws IOException {}

  public Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    return null;
  }
  
  public byte[] loadByteArray(String dirName, String name) throws IOException {
    return null;
  }

  public void delete(String dirName, String name) {}

  public void commit() throws IOException {}

  public void rollback() throws IOException {}

  public void release() throws IOException {}

  public final void stop() {}
}
