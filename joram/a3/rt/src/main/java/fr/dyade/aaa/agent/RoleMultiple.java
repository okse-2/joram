/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;


/**
 * This structure provides code for managing target agents registering
 * in a role. A notification may be sent to a role using the <code>sendTo</code>
 * function of the sending agent.
 *
 * The class does not handle duplicates in the list.
 */
public class RoleMultiple implements Serializable {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;
  
  private String name;
  private Vector<AgentId> list = null;

  public RoleMultiple() {}

  /**
   * Creates a new RoleMultiple with a specified name.
   * @param name the role name.
   */
  public RoleMultiple(String name) {
      this.name = name;
  }

  /**
   * Adds an agent in the listeners list.
   */
  public void addListener(AgentId target) {
    if (list == null)
      list = new Vector<AgentId>();
    list.addElement(target);
  }

  /**
   * Removes an agent from the listeners list.
   */
  public void removeListener(AgentId target) {
    if (list == null)
      return;
    for (int i = list.size(); i-- > 0;) {
      AgentId id = (AgentId) list.elementAt(i);
      if (target.equals(id)) {
        list.removeElement(id);
        break;
      }
    }
  }

  /**
   * Gets the listeners list as an <code>Enumeration</code> of <code>AgentId</code> objects.
   *
   * There is no synchronization as we assume this object is manipulated
   * from the enclosing agent reaction.
   */
  public Enumeration<AgentId> getListeners() {
    if (list == null)
      return null;
    return list.elements();
  }

  /**
   * Returns the role name.
   */
  public String getName() {
    return name;
  }
    
  /**
   * Sets the role name.
   * @param name the role name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Tests if the specified agent id belongs to
   * role multiple. 
   * @param id the specified agent id.
   * @return true if the specified id belongs to the role;
   * false otherwise. 
   */
  public boolean contains(AgentId id) {
    if (list == null)
      return false;
    return list.contains(id);
  }
  
  /**
   * Provides a string image for this object.
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    output.append(super.toString());
    output.append(",name=" + name);
    output.append(",list=");
    if (list == null) {
      output.append("null");
    } else {
      output.append("(");
      output.append(list.size());
      for (int i = 0; i < list.size(); i ++) {
        output.append(",");
        output.append(list.elementAt(i));
      }
      output.append(")");
    }
    output.append(")");
    return output.toString();
  }
}
