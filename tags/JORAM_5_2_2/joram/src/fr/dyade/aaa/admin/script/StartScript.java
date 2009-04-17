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
import java.util.*;
import fr.dyade.aaa.admin.cmd.*;
import fr.dyade.aaa.agent.conf.*;

/**
 * StartScript contain a StartAdminCmd vector and a ServerDesc hashtable.
 * It permits to start networks, servers and services.
 *
 * @see fr.dyade.aaa.agent.AgentAdmin
 * @see fr.dyade.aaa.agent.AgentServer
 * @see StartAdminCmd
 * @see A3CMLConfig
 * @see fr.dyade.aaa.agent.ServerDesc
 */
public class StartScript implements Serializable {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** vector of StartAdminCmd */
  private Vector commands = null;
  /** hashtable of server description */
  public Hashtable serverDesc = null;

 /**
  * StartScript
  *
  * @see AgentAdmin
  */
  public StartScript() {
    commands = new Vector();
    serverDesc = new Hashtable();
  }

  public void add(StartAdminCmd cmd) {
    commands.addElement(cmd);
  }

  public boolean remove(StartAdminCmd cmd) {
    return commands.removeElement(cmd);
  }

  public boolean contains(StartAdminCmd cmd) {
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
    StringBuffer strBuf = new StringBuffer();
    strBuf.append(getClass().getName());
    strBuf.append("@");
    strBuf.append(Integer.toHexString(hashCode()));
    strBuf.append("(commands=");
    strBuf.append(commands);
    strBuf.append(",serverDesc=");
    strBuf.append(serverDesc);
    strBuf.append(")");
    return strBuf.toString();
  }

}
