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

package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import fr.dyade.aaa.util.*;

/**
 * The <code>AgentObject</code> class represents ... 
 */
public abstract class AgentObject implements Serializable {


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
    return (AgentObject) AgentServer.transaction.load("AgentObject_" + name);
  }

  /**
   *  Saves the <code>AgentObject</code> object. Be careful, this method
   * should only be used in the <code>save</code> method of including Agent
   * in order to preserve the atomicity.
   *
   * @exception Exception	unspecialized exception
   */
  void save() throws IOException {
    AgentServer.transaction.save(this, "AgentObject_" + name);
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
