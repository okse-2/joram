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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */
package fr.dyade.aaa.task;

import fr.dyade.aaa.agent.*;
import java.io.*;


/**
 * Structure to keep registered listeners for a scheduler condition.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see	Scheduler
 */
public class ConditionItem implements Serializable {

public static final String RCS_VERSION="@(#)$Id: ConditionItem.java,v 1.3 2002-10-21 08:41:14 maistrfr Exp $"; 


  /** condition name */
  String name;
  /** list of registered agents for this event */
  RoleMultiple listeners;
  /** next item, <code>null</code> terminated, in lexicographic order of name */
  ConditionItem next;

  /**
   * Initializes object to be decoded.
   */
  public ConditionItem() {
    this(null);
  }

  /**
   * Constructor.
   *
   * @param name	condition name
   * @param listeners	list of registered agents for this event
   */
  public ConditionItem(String name, RoleMultiple listeners) {
    this.name = name;
    if (listeners != null)
      this.listeners = listeners;
    else
      this.listeners = new RoleMultiple();
    next = null;
  }

  /**
   * Constructor with default <code>null</code> value for
   * <code>listeners<code>.
   *
   * @param name	condition name
   */
  public ConditionItem(String name) {
    this(name, null);
  }

  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    StringBuffer output = new StringBuffer();
    output.append("(");
    for (ConditionItem item = this; item != null; item = item.next) {
      output.append("(name=");
      output.append(item.name);
      output.append(",listeners=");
      output.append(item.listeners);
      output.append("),");
    }
    output.append("null)");
    return output.toString();
  }
}
