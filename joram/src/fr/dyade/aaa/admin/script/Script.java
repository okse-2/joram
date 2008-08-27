/*
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.admin.script;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;
import fr.dyade.aaa.admin.cmd.*;
import fr.dyade.aaa.agent.conf.*;

/**
 *  Script contains an AdminCmd vector. Each commands permits to create/remove
 * a domain, a network, or a server and set/unset property or a JVM argument in
 * A3CMLConfig (configuration).
 *
 * @see fr.dyade.aaa.agent.AgentAdmin
 * @see AdminCmd
 * @see A3CMLConfig
 */
public class Script implements Serializable, Cloneable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;

  /** AdminCmd Vector */
  private Vector commands = null;
  /** create new configuration */
  public boolean newConfig = false;

  /**
   * Script
   *
   * @see AgentAdmin
   */
  public Script() {
    commands = new Vector();
  }

  public void add(AdminCmd cmd) {
    commands.addElement(cmd);
  }

  public boolean remove(AdminCmd cmd) {
    return commands.removeElement(cmd);
  }

  public boolean contains(AdminCmd cmd) {
    return commands.contains(cmd);
  }

  public boolean isEmpty() {
    return commands.isEmpty();
  }

  public Enumeration elements() {
    return commands.elements();
  }

  public int size() {
    return commands.size();
  }

  public Object elementAt(int i) throws ArrayIndexOutOfBoundsException {
    return commands.elementAt(i);
  }

  public Object firstElement() throws NoSuchElementException {
    return commands.firstElement();
  }

  public String toString() {
    return commands.toString();
  }

  public Object clone() throws CloneNotSupportedException {
    Script clone = (Script)super.clone();
    clone.commands = (Vector)commands.clone();
    clone.newConfig = newConfig;
    return clone;
  }
}
