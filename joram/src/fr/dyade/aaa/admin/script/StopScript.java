/*
 * Copyright (C) 2002 SCALAGENT 
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
 */

package fr.dyade.aaa.admin.script;

import java.lang.*;
import java.io.*;
import java.util.*;
import fr.dyade.aaa.admin.cmd.*;
import fr.dyade.aaa.agent.conf.*;

/**
 * StopScript contain a StopAdminCmd vector.
 * It use to stop networks, servers persistents,
 * servers transients and services.
 *
 * @see AgentAdmin
 * @see AgentServer
 * @see StopAdminCmd
 * @see A3CMLConfig
 */
public class StopScript implements Serializable {

  /** vector of StopAdminCmd */
  private Vector commands = null;

 /**
  * StopScript
  *
  * @see AgentAdmin
  */
  public StopScript() {
    commands = new Vector();
  }

  public void add(StopAdminCmd cmd) {
    commands.addElement(cmd);
  }

  public boolean remove(StopAdminCmd cmd) {
    return commands.removeElement(cmd);
  }

  public boolean contains(StopAdminCmd cmd) {
    return commands.contains(cmd);
  }

  public Enumeration elements() {
    return commands.elements();
  }

  public int size() {
    return commands.size();
  }

  public Object elementAt(int i) 
    throws ArrayIndexOutOfBoundsException {
    return commands.elementAt(i);
  }

  public String toString() {
    return commands.toString();
  }

}
