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


/**
 * This notification is sent from and to a <code>TransientManager</code> agent
 * to realize the communication between persistent and non persistent agents.
 * It encapsulates the actual notification and the actual source and target
 * agents.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 */

public class TransientMessage extends Notification {

public static final String RCS_VERSION="@(#)$Id: TransientMessage.java,v 1.1.1.1 2000-05-30 11:45:24 tachkeni Exp $"; 


  /** actual source agent */
  AgentId from;

  /** actual target agent */
  AgentId to;

  /** actual notification */
  Notification not;

  /**
   * Constructor.
   *
   * @param from	actual source agent
   * @param to		actual target agent
   * @param not		actual notification
   */
  public TransientMessage(AgentId from, AgentId to, Notification not) {
    this.from = from;
    this.to = to;
    this.not = not;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",from=" + from +
      ",to=" + to +
      ",not=" + not + ")";
  }
}
