/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.util;

import java.io.*;
import java.util.*;

public class SimpleTransaction implements Transaction {
  public static final String RCS_VERSION="@(#)$Id: SimpleTransaction.java,v 1.11 2002-12-11 11:27:01 maistrfr Exp $"; 

  private File dir = null;

  public SimpleTransaction() {}

  public void init(String path) throws IOException {
    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory()) {
      // TODO: Error
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
    File file;
    if (dirName == null) {
      file = new File(dir, name);
    } else {
      File parentDir = new File(dir, dirName);
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      file = new File(parentDir, name);
    }

    // Save the current state of the object.
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(obj);
      oos.flush();
      fos.getFD().sync();
    } finally {
      if (fos != null)
	fos.close();
    }
  }

  public Object load(String name) throws IOException, ClassNotFoundException {
    return load(null, name);
  }

  public final Object load(String dirName, String name) throws IOException, ClassNotFoundException {
    Object obj;

    File file;
    if (dirName == null) {
      file = new File(dir, name);
    } else {
      File parentDir = new File(dir, dirName);
      file = new File(parentDir, name);
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
    File file;
    if (dirName == null) {
      file = new File(dir, name);
      if (! file.exists()) {
        // If the File don't exists it's an error
        // TODO: Error.
      } else {
        file.delete();
      }
    } else {
      File parentDir = new File(dir, dirName);
      file = new File(parentDir, name);
      file.delete();
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

  public final void stop() {}
}