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
 * Notification requesting an agent to be registered for receiving
 * <code>Condition</code> notifications for a scheduled event.
 * An agent may only register itself.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 */
public class AddConditionListener extends Notification {

public static final String RCS_VERSION="@(#)$Id: AddConditionListener.java,v 1.4 2002-12-11 11:26:52 maistrfr Exp $"; 


  /** condition name */
  String name;

  /**
   * Creates an item.
   *
   * @param name	condition name
   */
  public AddConditionListener(String name) {
    this.name = name;
  }


  /**
   * Provides a string image for this object.
   *
   * @return	a string image for this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",name=" + name + ")";
  }
}
