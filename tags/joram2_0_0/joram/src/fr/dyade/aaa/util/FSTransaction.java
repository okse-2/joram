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

public class FSTransaction implements Transaction {
  public static final String RCS_VERSION="@(#)$Id: FSTransaction.java,v 1.4 2001-05-04 14:55:04 tachkeni Exp $"; 

  private File dir = null;

  static private final String CURRENT = "current";
  static private final String PREVIOUS = "previous";
  static private final String CREATED = "created";
  static private final String PHASE = "phase";

  private File currentDir = null;
  private File previousDir = null;

  private File createdFile = null; 
  private FileOutputStream createdFos = null;
  private DataOutputStream createdDos = null;

  private RandomAccessFile phaseFile = null; 

  // State of the transaction monitor.
  private int phase;

  static private final int INIT = 0;		// Initialization state
  static private final int FREE = 1;		// No transaction 
  static private final int RUN = 2;		// A transaction is running
  static private final int COMMIT = 3;		// A transaction is commiting
  static private final int ROLLBACK = 4;	// A transaction is aborting

  private Vector created = null;
  private Vector used = null;

  public FSTransaction(String path) throws IOException {
    phase = INIT;

    dir = new File(path);
    if (!dir.exists()) dir.mkdir();
    if (!dir.isDirectory()) {
      // TODO: Error
    }
    currentDir = new File(dir, CURRENT);
    if (!currentDir.exists()) currentDir.mkdir();
    if (!currentDir.isDirectory()) {
      // TODO: Error
    }
    previousDir = new File(dir, PREVIOUS);
    if (!previousDir.exists()) previousDir.mkdir();
    if (!previousDir.isDirectory()) {
      // TODO: Error
    }
    createdFile = new File(dir, CREATED);

    created = new Vector();
    used = new Vector();

    // Test the stop status then complete commit or rollback if needed
    int oldPhase = FREE;
    phaseFile = new RandomAccessFile(new File(dir, PHASE), "rw");
    if (phaseFile.length() != 0) {
      phaseFile.seek(0L);
      oldPhase = phaseFile.readInt();
    }    
    if (oldPhase == COMMIT) {
      // Deletes all files in "previous" directory
      String list[] = previousDir.list();
      for (int i=0; i<list.length; i++) {
	File f = new File(previousDir, list[i]);
	f.delete();
      }
    } else if ((oldPhase == RUN) || (oldPhase == ROLLBACK)) {
      // Restores files from "previous" directory
      String list[] = previousDir.list();
      for (int i=0; i<list.length; i++) {
	File f = new File(previousDir, list[i]);
	f.renameTo(new File(currentDir, list[i]));
      }

      // then deletes new created files
      FileInputStream createdFis = null;
      DataInputStream createdDis = null;
      try {
	createdFis = new FileInputStream(createdFile);
	createdDis = new DataInputStream(createdFis);
	try {
	  while (true) {
	    File f = new File(currentDir, createdDis.readUTF());
	    f.delete();
	  }
	} catch (EOFException exc) {
	  // there is no other filename to read
	}	
      } catch (FileNotFoundException exc) {
	// There is no created file during last transaction.
      } finally {
	if (createdFis != null)
	  createdFis.close();
      }
    } else if (oldPhase == INIT) {
      // TODO: Error
    }    
    // then clean the "created" file
    createdFile.delete();
    
    setPhase(FREE);
  }

  public File getDir() {
    return currentDir;
  }

  private void setPhase(int newPhase) throws IOException {
    phaseFile.seek(0L);
    phaseFile.writeInt(newPhase);
    phaseFile.getFD().sync();
    phase = newPhase;
  }

  public synchronized void begin() throws IOException {
    while (phase != FREE) {
      try {
	wait();
      } catch (InterruptedException exc) {
      }
    }
    // In order to optimize (it takes 88% of Transaction.begin execution
    // time) we report the createdDos initialization in Transaction.save
    // when it is necessary.

    // Change the transaction state and save it.
    setPhase(RUN);
  }

  public String[] getList(String prefix) {
    return currentDir.list(new StartWithFilter(prefix));
  }

  public void save(Serializable obj, String name) throws IOException {
    File file = new File(currentDir, name);
    if ((! file.exists()) &&	// If the File don't exists...
	(phase == RUN)) {	// and we are during a transaction. 
      // Add its name in the created list
      created.addElement(name);

      if (createdDos == null) {
	// Initialize the "Created" file
	createdFos = new FileOutputStream(createdFile);
	createdDos = new DataOutputStream(createdFos);
      }
      createdDos.writeUTF(name);
      createdDos.flush();
      createdFos.getFD().sync();
    } else {
      if (!used.contains(name) && !created.contains(name) && (phase == RUN)) {
	//  It's the first used of the object in this transaction,
	// then make a copy of initial state
	file.renameTo(new File(previousDir, name));
	used.addElement(name);
      }
    }

    // Then save the current state of the object
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
    Object obj;

    File file = new File(currentDir, name);
    if (file.canRead()) {
      FileInputStream fis = new FileInputStream(file);
      ObjectInputStream ois = new ObjectInputStream(fis);
      
      obj = ois.readObject();
      
      fis.close();
      return obj;
    }
    return null;
  }

  public void delete(String name) {
    File file = new File(currentDir, name);
    if (! file.exists()) {
      // If the File don't exists it's an error
      // TODO: Error.
    } else {
      if (!used.contains(name) && !created.contains(name) && (phase == RUN)) {
	//  It's the first used of the object in this transaction,
	// then make a copy of initial state
	file.renameTo(new File(previousDir, name));
	used.addElement(name);
      } else {
	file.delete();
      }
    }
  }

  public synchronized void commit() throws IOException {
    if (phase != RUN) {
      // TODO: Error.
    }
    setPhase(COMMIT);

    // TODO: In order to avoid its expansive code, may be we could
    // use a "system("rm -rf previous/*")...

    // Deletes all files in "previous" directory.
    for (Enumeration e = used.elements() ; e.hasMoreElements() ;) {
      File file = new File(previousDir, (String) e.nextElement());
      file.delete();
    }
    used.removeAllElements();
    // Clean the "Created" file.
    created.removeAllElements();

    if (createdDos != null) {
      createdDos.flush();
      createdFos.close();
      createdDos = null;
      createdFile.delete();
    }
  }

  public synchronized void rollback() throws IOException {
    if (phase != RUN) {
      // TODO: Error.
    }
    setPhase(ROLLBACK);
    // Moves all files from "previous" directory in "current".
    for (Enumeration e = used.elements() ; e.hasMoreElements() ;) {
      String name = (String) e.nextElement();
      File file = new File(previousDir, name);
      // TODO: Verify if it's needed to delete destination file before
      file.renameTo(new File(currentDir, name));
    }
    used.removeAllElements();
    // Deletes all files referenced in "Created" file.
    for (Enumeration e = created.elements() ; e.hasMoreElements() ;) {
      File file = new File(currentDir, (String) e.nextElement());
      file.delete();
    }

    // Clean the "Created" file.
    created.removeAllElements();

    if (createdDos != null) {
      createdDos.flush();
      createdFos.close();
      createdDos = null;
      createdFile.delete();
    }
  }

  public synchronized void release() throws IOException {
    if ((phase != COMMIT) && (phase != ROLLBACK)) {
      // TODO: Error.
    }
    // Change the transaction state and save it.
    setPhase(FREE);
    notify();
  }

  public final void stop() {}
}
