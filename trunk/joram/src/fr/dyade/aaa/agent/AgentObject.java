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

package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import fr.dyade.aaa.util.*;

/**
 * The <code>AgentObject</code> class represents ... 
 */
public abstract class AgentObject implements Serializable {

public static final String RCS_VERSION="@(#)$Id: AgentObject.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $"; 

  transient String name;
  /**
   *  Loads the <code>AgentObject</code> object
   * internally designed by the <code>name</code> parameter. Be careful that
   * including agent should be intialized in engine.
   *
   * @param name	The object name.
   *
   * @exception IOException
   *	when accessing the stored image
   * @exception ClassNotFoundException
   *	if the stored image class may not be found
   */
  static AgentObject
  load(String name) throws IOException, ClassNotFoundException {
    return (AgentObject) Server.transaction.load("AgentObject_" + name);
  }

  /**
   *  Saves the <code>AgentObject</code> object. Be careful, this method
   * should only be used in the <code>save</code> method of including Agent
   * in order to preserve the atomicity.
   *
   * @exception Exception	unspecialized exception
   */
  void save() throws IOException {
    Server.transaction.save(this, "AgentObject_" + name);
  }

  /**
   * The <code>writeObject</code> method is responsible for writing the
   * state of the object for its particular class so that the corresponding
   * <code>readObject</code> method can restore it.
   *
   * @param out the underlying output stream.
   */
  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
    // May be we could not save the name field and get it from
    // the load method!
    out.writeObject(name);
  }

  /**
   * The <code>readObject</code> is responsible for reading from the stream
   * and restoring the classes fields.
   *
   * @param in	the underlying input stream.
   */
  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
      name = (String) in.readObject();
  }

  public AgentObject(String name) {
    this.name = name;
  }

  /**
   * Returns a string representation of this object.
   *
   * @return	A string representation of this object. 
   */
  public String toString() {
    return new String("AgentObject_" + name);
  }
}
