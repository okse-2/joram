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


/**
 * This structure provides code for managing target agents registering
 * in a role. A notification may be sent to a role using the <code>sendTo</code>
 * function of the sending agent.
 *
 * The class does not handle duplicates in the list.
 */
public class RoleMultiple implements Serializable {

public static final String RCS_VERSION="@(#)$Id: RoleMultiple.java,v 1.4 2001-05-04 14:54:52 tachkeni Exp $"; 


  private String name;
  private Vector list = null;

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
      list = new Vector();
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
  public Enumeration getListeners() {
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
	output.append((AgentId) list.elementAt(i));
      }
      output.append(")");
    }
    output.append(")");
    return output.toString();
  }
}
