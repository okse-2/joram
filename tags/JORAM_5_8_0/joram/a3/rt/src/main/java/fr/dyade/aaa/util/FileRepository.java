/*
 * Copyright (C) 2006 - 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package fr.dyade.aaa.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *  This class allows to use a filesystem directory as repository with the
 * NTransaction module.
 *
 * @see NTransaction
 * @see Repository
 */
public final class FileRepository implements Repository {
  File dir = null;

  /** The number of save operation to repository. */
  private int nbsaved = 0;

  /**
   * Returns the number of save operation to repository.
   *
   * @return The number of save operation to repository.
   */
  public int getNbSavedObjects() {
    return nbsaved;
  }

  /** The number of delete operation on repository. */
  private int nbdeleted = 0;

  /**
   * Returns the number of delete operation on repository.
   *
   * @return The number of delete operation on repository.
   */
  public int getNbDeletedObjects() {
    return nbdeleted;
  }

  /** The number of useless delete operation on repository. */
  private int baddeleted = 0;

  /**
   * Returns the number of useless delete operation on repository.
   *
   * @return The number of useless delete operation on repository.
   */
  public int getNbBadDeletedObjects() {
    return baddeleted;
  }

  /** The number of load operation from repository. */
  private int nbloaded = 0;

  /**
   * Returns the number of load operation from repository.
   *
   * @return The number of load operation from repository.
   */
  public int getNbLoadedObjects() {
    return nbloaded;
  }

  /**
   *  Boolean value to force the use of FileOutputStream rather than
   * RandomAccessFile. By default this value is false, it can be set to 
   * true using the FileRepository.useRandomAccessFile java property.
   * <p>
   *  This property can be fixed only from <code>java</code> launching
   * command, or through System.property method.
   */
  private boolean useFileOutputStream;

  // Be careful the constructor must be public to allow newInstance from another package.
  public FileRepository() {}

  /**
   * Initializes the repository.
   * Nothing to do.
   */
  public void init(Transaction transaction, File dir)  throws IOException {
    this.dir = dir;
    useFileOutputStream = transaction.getBoolean("FileRepository.useRandomAccessFile");
  }

  /**
   * Gets a list of persistent objects that name corresponds to prefix.
   *
   * @return The list of corresponding names.
   */
  public String[] list(String prefix) throws IOException {
    return dir.list(new StartWithFilter(prefix));
  }

  /**
   * Save the corresponding bytes array.
   */
  public void save(String dirName, String name, byte[] content) throws IOException {
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

    if (useFileOutputStream ) {
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(content);
      fos.getFD().sync();
      fos.close();
    } else {
      RandomAccessFile raf = new RandomAccessFile(file, "rwd");
      raf.write(content);
      raf.close();                    
    }

    nbsaved += 1;
  }

//   /**
//    * Loads the object.
//    *
//    * @return The loaded object or null if it does not exist.
//    */
//   public Object loadobj(String dirName, String name) throws IOException, ClassNotFoundException {
//     File file;
//     Object obj;
//     if (dirName == null) {
//       file = new File(dir, name);
//     } else {
//       File parentDir = new File(dir, dirName);
//       file = new File(parentDir, name);
//     }

//     FileInputStream fis = new FileInputStream(file);
//     ObjectInputStream ois = new ObjectInputStream(fis);
//     try {
//       obj = ois.readObject();
//     } finally {
//       ois.close();
//       fis.close();
//     }

//     nbloaded += 1;
//     return obj;
//   }

  /**
   * Loads the byte array.
   *
   * @return The loaded bytes array.
   */
  public byte[] load(String dirName, String name) throws IOException {
      // Gets it from disk.      
      File file;
      if (dirName == null) {
        file = new File(dir, name);
      } else {
        File parentDir = new File(dir, dirName);
        file = new File(parentDir, name);
      }
      FileInputStream fis = new FileInputStream(file);
      byte[] buf = new byte[(int) file.length()];
      for (int nb=0; nb<buf.length; ) {
        int ret = fis.read(buf, nb, buf.length-nb);
        if (ret == -1) throw new EOFException();
        nb += ret;
      }
      fis.close();

      nbloaded += 1;    
      return buf;
  }

  /**
   * Deletes the corresponding objects in repository.
   */
  public void delete(String dirName, String name) throws IOException {
    if (dirName == null) {
      if (! new File(dir, name).delete()) baddeleted += 1;
    } else {
      File parentDir = new File(dir, dirName);
      if (! new File(parentDir, name).delete()) baddeleted += 1;
      deleteDir(parentDir);
    }
    nbdeleted += 1;
  }

  /**
   * Delete the specified directory if it is empty.
   * Also recursively delete the parent directories if they are empty.
   */
  private final void deleteDir(File dir) {
    // Check the disk state. It may be false according to the transaction
    // log but it doesn't matter because directories are lazily created.
    String[] children = dir.list();
    // children may be null if dir doesn't exist any more.
    if (children != null && children.length == 0) {
      dir.delete();
      if (dir.getAbsolutePath().length() > 
          this.dir.getAbsolutePath().length()) {
        deleteDir(dir.getParentFile());
      }
    }
  }

  /**
   * Commits all changes to the repository.
   */
  public void commit() throws IOException {}

  /**
   * Closes the repository.
   */
  public void close() throws IOException {}
}
