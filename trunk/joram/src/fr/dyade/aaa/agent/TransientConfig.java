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
 * Notification which is sent by a transient agent server to its
 * <code>TransientManager</code> agent when it starts.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		TransientManager
 */
public class TransientConfig extends Notification {

public static final String RCS_VERSION="@(#)$Id: TransientConfig.java,v 1.2 2000-08-01 09:13:31 tachkeni Exp $"; 


  /** id of transient agent server */
  short serverId;

  /**
   * Constructor.
   *
   * @param serverId	id of transient agent server
   */
  public TransientConfig(short serverId) {
    this.serverId = serverId;
  }

  /**
   * Provides a string image for this object.
   *
   * @return	printable image of this object
   */
  public String toString() {
    return "(" + super.toString() +
      ",serverId=" + serverId + ")";
  }
}
