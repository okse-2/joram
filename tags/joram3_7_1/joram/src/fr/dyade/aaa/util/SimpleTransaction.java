/*
 * Copyright (C) 2001 - 2003 ScalAgent Distributed Technologies
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

public final class SimpleTransaction implements Transaction {
  public static final String RCS_VERSION="@(#)$Id: SimpleTransaction.java,v 1.15 2003-09-12 08:05:50 afreyssin Exp $"; 

  private File dir = null;

  public SimpleTransaction() {}

  public void init(String path) throws IOException {
    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory())
      throw new FileNotFoundException(path + " is not a directory.");

    // Saves the transaction classname in order to prevent use of a
    // different one after restart (see AgentServer.init).
    DataOutputStream dos = null;
    try {
      File tfc = new File(dir, "TFC");
      if (! tfc.exists()) {
        dos = new DataOutputStream(new FileOutputStream(tfc));
        dos.writeUTF(getClass().getName());
        dos.flush();
      }
    } finally {
      if (dos != null) dos.close();
    }
  }

  public File getDir() {
    return dir;
  }

  public void begin() throws IOException {}

  public String[] getList(String prefix) {
    return dir.list(new StartWithFilter(prefix));
  }

  public void save(Serializable obj, String name) throws IOException {
    save(obj, null, name);
  }
    
  public void save(Serializable obj, String dirName, String name) throws IOException {
    File temp, file;
    if (dirName == null) {
      temp = new File(dir, "temp_" + name);
      file = new File(dir, name);
    } else {
      File parentDir = new File(dir, dirName);
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      temp = new File(parentDir, "temp_" + name);
      file = new File(parentDir, name);
    }

    if (temp.exists() && (! temp.delete()))
      throw new IOException("Can't delete log file: " + temp.getPath());

    if (file.exists())
      file.renameTo(temp);
	
    // Save the current state of the object.
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(obj);
      oos.flush();
      fos.getFD().sync();
      fos.close();
      fos = null;
      temp.delete();
      temp = null;
    } finally {
      if (fos != null) fos.close();
    }
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }

  public Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    Object obj;

    File temp, file;
    if (dirName == null) {
      temp = new File(dir, "temp_" + name);
      file = new File(dir, name);
    } else {
      File parentDir = new File(dir, dirName);
      temp = new File(parentDir, "temp_" + name);
      file = new File(parentDir, name);
    }
    if (temp.exists()) {
      if (! file.delete())
	throw new IOException("Can't delete corrupted file: " + file.getPath());
      if (! temp.renameTo(file))
	throw new IOException("Can't restore corrupted file: " + file.getPath());
    }
    if (file.canRead()) {
      FileInputStream fis = null;
      try {
	fis = new FileInputStream(file);
	ObjectInputStream ois = new ObjectInputStream(fis);
      
	obj = ois.readObject();
      } finally {
	if (fis != null)
	  fis.close();
      }
      return obj;
    }
    return null;
  }

  public void delete(String name) {
    delete(null, name);
  }

  public void delete(String dirName, String name) {
    File temp, file;
    if (dirName == null) {
      file = new File(dir, name);
      if (! file.exists()) {
        // If the File don't exists it's an error
        // TODO: Error.
      } else {
        file.delete();
      }
      temp = new File(dir, "temp_" + name);
      if (temp.exists()) temp.delete();
    } else {
      File parentDir = new File(dir, dirName);
      file = new File(parentDir, name);
      file.delete();
      temp = new File(parentDir, "temp_" + name);
      if (temp.exists()) temp.delete();
      deleteDir(parentDir);
    } 
  }  

  /**
   * Delete the specified directory if it is empty.
   * Also recursively delete the parent directories if
   * they are empty.
   */
  private void deleteDir(File dir) {
    if (dir.list().length == 0) {
      dir.delete();
      if (dir.getAbsolutePath().length() > 
          this.dir.getAbsolutePath().length()) {
        deleteDir(dir.getParentFile());
      }
    }
  }

  public void commit() throws IOException {}

  public void rollback() throws IOException {}

  public void release() throws IOException {}

  public void stop() {}
}
