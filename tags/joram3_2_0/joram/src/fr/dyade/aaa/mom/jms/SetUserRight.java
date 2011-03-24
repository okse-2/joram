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
package fr.dyade.aaa.mom.jms;

/**
 * A <code>SetUserRight</code> notification is used by an administrator to
 * set a user's right.
 * <p>
 * The user may either be set or unset as a READER or a WRITER on a
 * destination.
 */
public class SetUserRight extends JmsAdminRequest
{
  /** Name of the user's proxy. */
  private String pName;
  /** Name of the destination the user's rights are set for. */
  private String dest;
  /** Right set. */
  private int right;

  /**
   * Constructs a <code>SetUserRight</code> instance.
   *
   * @param requestId  See superclass.
   * @param pName  Name of the user's proxy agent.
   * @param dest  Name of the destination.
   * @param right  Right to set.
   */
  public SetUserRight(String pName, String dest, int right)
  {
    this.pName = pName;
    this.dest = dest;
    this.right = right;
  }


  /** Returns the name of the user's proxy. */
  public String getProxyName()
  {
    return pName;
  }

  /**
   * Returns the name of the destination the user's rights are being set for.
   */
  public String getDest()
  {
    return dest;
  }

  /** Returns the right value. */
  public int getRight()
  {
    return right;
  }
}