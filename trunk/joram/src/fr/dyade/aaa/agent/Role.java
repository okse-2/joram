/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies
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

/**
 * A role wraps an AgentId. It is identified by a name.
 */
public class Role implements Serializable {
    /**
   * 
   */
  private static final long serialVersionUID = 1L;

    /**
     * The wrapped <code>AgentId</code>.
     */
    private AgentId listener;

    /**
     * The role name.
     */
    private String name;

    /**
     * Creates a new role with the specified name.
     * @param name the role name.
     */
    public Role(String name) {
	this.name= name;
    }

    /**
     * Creates a new role with the specified name and AgentId.
     * @param name the role name.
     * @param listener the wrapped <code>AgentId</code>.
     */
    public Role(String name, AgentId listener) {
	this(name);
	this.listener = listener;
    }

    /**
     * Sets the wrapped <code>AgentId</code>.
     * @param listener the wrapped <code>AgentId</code>.
     */
    public void setListener(AgentId listener) {
	this.listener = listener;
    }

    /**
     * Returns the wrapped <code>AgentId</code>.
     */
    public AgentId getListener() {
	return listener;
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

    public String toString() {
      StringBuffer output = new StringBuffer();
      output.append("(");
      output.append(super.toString());
      output.append(",name=" + name);
      output.append(",listener=" + listener);
      output.append(")");
      return output.toString();
    }
}
