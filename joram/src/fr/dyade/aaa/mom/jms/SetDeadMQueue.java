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
 * A <code>SetDeadMQueue</code> instance is used by a JMS administrator for
 * setting or unsetting a given dead message queue as a default DMQ, or the
 * DMQ of a given destination, or the DMQ of a given user.
 */
public class SetDeadMQueue extends JmsAdminRequest
{
  /** Name of the destination or user's proxy. */
  private String name;
  /**
   * Identifier of the dead message queue, <code>null</code> for actually
   * unsetting an already set DMQ. 
   */
  private String dmqId;
  /** <code>true</code> if the target is a user. */
  private boolean toUser;

  /**
   * Constructs a <code>SetDeadMQueue</code> instance.
   *
   * @param name  Name of the destination or user's proxy, <code>null</code> 
   *          for a default setting.
   * @param dmqId  The dmq identifier, <code>null</code> for unsetting a DMQ.
   * @param toUser  <code>true</code> if the target is a user.
   */
  public SetDeadMQueue(String name, String dmqId, boolean toUser)
  {
    this.name = name;
    this.dmqId = dmqId;
    this.toUser = toUser;
  }

  
  /**
   * Returns the name of a destination, or the identifier of a user proxy,
   * or <code>null</code>.
   */
  public String getName()
  {
    return name;
  }

  /**
   * Returns the identifier of the dead message queue, <code>null</code> for
   * unsetting a DMQ.
   */
  public String getDMQId()
  {
    return dmqId;
  }

  /** Returns <code>true</code> if the target is a user. */
  public boolean toUser()
  {
    return toUser;
  }
}
