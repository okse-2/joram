/*
 * Copyright (C) 2004 ScalAgent Distributed Technologies
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

public interface AgentMBean {
  /**
   * Returns this <code>Agent</code>'s name.
   *
   * @return this <code>Agent</code>'s name.
   */
  public String getName();

  /**
   * Returns the global unique identifier of the agent. Each agent is
   * identified by a unique identifier allowing the agent to be found.
   * The identifiers format is detailed in <a href="AgentId.html">AgentId</a>
   * class.
   *
   * @return the global unique identifier of the agent.
   */
  public AgentId getId();

//   /**
//    * Returns log topic for the agent.
//    */
//   protected String getLogTopic();

  /** 
   * Tests if the agent is pinned in memory.
   *
   * @return true if this agent is a pinned in memory; false otherwise.
   * @see fixed
   */
  public boolean isFixed();

  /**
   * Permits this agent to destroy it.
   */
  public void delete();

  /**
   * Returns a string representation of this agent, including the agent's
   * class, name, global identication, and fixed property.
   *
   * @return	A string representation of this agent. 
   */
  public String toString();
}
