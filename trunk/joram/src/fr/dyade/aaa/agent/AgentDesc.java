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

public class AgentDesc implements Serializable {
public static final String RCS_VERSION="@(#)$Id: AgentDesc.java,v 1.7 2002-01-16 12:46:47 joram Exp $";

  /** 
   * The name of the Agent.
   */
  private String name;
  
  /**
   * The id of the Agent.
   */
  private AgentId id;
  
  /**
   * Some agents must be loaded at any time. If <code>true</code> agent
   * is pinned in memory.
   */ 
  private boolean fixed;
  
  /**
   * Allocates a new AgentDesc object.
   *
   * @param name  symbolic name of agent
   * @param id	  global id. of  agent 
   * @param fixed if <code>true</code> agent is pinned in memory
   */
  public AgentDesc(String name, AgentId id, boolean fixed) {
    this.name = name;
    this.id = id;
    this.fixed = fixed;
  }
  
  /**
   * Returns true if the described agent has been created in the server
   * specified by serverid.
   *
   * @param serverid	the id of the server
   */
  public final boolean createdIn(short serverid) {
    return (id.from == serverid);
  }
  
  /**
   * Returns true if the described agent has been deployed in the server
   * specified by serverid.
   *
   * @param serverid	the id of the server
   */
  public final boolean deployedIn(short serverid) {
    return (id.to == serverid);
  }
  
  /**
   * Returns true if the Agent is fixed in memory.
   */
  public final boolean isFixed() {
    return fixed;
  }

  /**
   * Returns a string representation of the object.
   */
  public String toString() {
    return "(AgentDesc, "+ name + ", " + id + ", "+ fixed + ")";
  }
    
  /**
   * Returns the AgentId of the AgentDesc
   */
  public final AgentId getId() {
    return id;
  }

  /**
   * Returns the Name of the Agent.
   *
   * @return the name of the Agent.
   */
  public final String getName() {
    return name;
  }

  /**
   * Indicates whether some other object is "equal to" this one. This method
   * returns code>true</code> if and only if obj is an <code>AgentDesc/code>
   * and refer to the same object (id and obj.id are equals).
   *
   * @param obj	 the reference object with which to compare.
   * @return	 <code>true</code> if this object is the same as the obj
   *		 argument; <code>false</code> otherwise.
   */
  public final boolean equals(Object obj) {
    try {
      return id.equals(((AgentDesc) obj).getId());
    } catch(ClassCastException exc) {
      return false;
    }
  }

}
