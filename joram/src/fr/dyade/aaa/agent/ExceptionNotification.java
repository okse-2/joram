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
 * Notify by the engine to the sender of a notification when the corresponding
 * reaction throws an exception.
 *
 * @see Engine
 */
public class ExceptionNotification extends Notification {
public static final String RCS_VERSION="@(#)$Id: ExceptionNotification.java,v 1.5 2001-05-14 16:26:39 tachkeni Exp $"; 

  /** The target agent id. */
  public AgentId agent;
  /** The failing notification. */
  public Notification not;
  /** The exception thrown. */
  public Exception exc;

  /**
   * Allocates a new <code>ExceptionNotification</code> notification.
   *
   * @param agent	The target agent id.
   * @param not		The failing notification.
   * @param exc		The exception thrown.
   */
  public ExceptionNotification(AgentId agent,
			       Notification not,
			       Exception exc) {
    this.agent = agent;
    this.not = not;
    this.exc = exc;
  }

  /**
   * Returns a string representation of this notification.
   *
   * @return	A string representation of this notification. 
   */
  public String toString() {
    return "(" + super.toString() +
      ",agent=" + agent +
      ",not=" + not +
      ",exc=" + exc + ")";
  }
}
