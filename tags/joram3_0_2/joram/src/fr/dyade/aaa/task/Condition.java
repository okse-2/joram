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


/**
 * Notification changing the status of a starting condition of a
 * <code>Task</code> agent.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Task
 */
public class Condition extends Notification {

public static final String RCS_VERSION="@(#)$Id: Condition.java,v 1.2 2002-03-26 16:09:59 joram Exp $"; 

  /** condition name, may be null */
  public String name;
  /** condition status */
  public boolean status;

  /**
   * Constructor.
   *
   * @param name		condition name, may be null
   * @param status		condition status
   */
  public Condition(String name, boolean status) {
    this.name = name;
    this.status = status;
  }

  /**
   * Constructor with default true status.
   *
   * @param name		condition name, may be null
   */
  public Condition(String name) {
    this(name, true);
  }

  /**
   * Constructor with default null name.
   *
   * @param status		condition status
   */
  public Condition(boolean status) {
    this(null, status);
  }

  /**
   * Constructor with default null name and true status.
   */
  public Condition() {
    this(null, true);
  }

  
  /**
   * Provides a string image for this object.
   *
   * @result		string image for this object
   */
  public String toString() {
    return "(" +
      super.toString() +
      ",name=" + name + "," +
      "status=" + status + ")";
  }
}
