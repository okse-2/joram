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
 * Notify by agents when there is no reaction allowed. 
 *
 * @see Agent
 */
public class UnknownNotification extends Notification {
  public static final String RCS_VERSION="@(#)$Id: UnknownNotification.java,v 1.9 2002-03-26 16:08:39 joram Exp $";

  /** The target agent id. */
  public AgentId agent;
  /** The failing notification. */
  public Notification not;

  /**
   * Allocates a new <code>UnknownNotification</code> notification.
   *
   * @param agent	The target agent id.
   * @param not		The failing notification.
   */
  public UnknownNotification(AgentId agent, Notification not) {
    this.agent = agent;
    this.not = not;
  }

  /**
   * Returns a string representation of this notification.
   *
   * @return	A string representation of this notification. 
   */
  public String toString() {
    return "(" + super.toString() +
      ",agent=" + agent +
      ",not=" + not + ")";
  }
}
