/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.mom.admin;

/**
 * An <code>AdminReply</code> instance is used by a
 * <code>fr.dyade.aaa.mom.dest.AdminTopic</code> topic for sending data or
 * information to a client administrator.
 */
public class AdminReply implements java.io.Serializable
{
  /** <code>true</code> if this reply replies to a successful request. */
  private boolean success = false;
  /** Information. */
  private String info;

  /**
   * Constructs an <code>AdminReply</code> instance.
   *
   * @param success  <code>true</code> if this reply replies to a successful
   *          request.
   * @param info  Information to carry.
   */
  public AdminReply(boolean success, String info)
  {
    this.success = success;
    this.info = info;
  }

  /**
   * Returns <code>true</code> if this reply replies to a successful request.
   */
  public boolean succeeded()
  {
    return success;
  }

  /** Returns the carried info. */
  public String getInfo()
  {
    return info;
  }
}
