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
package fr.dyade.aaa.mom.comm;

import fr.dyade.aaa.agent.AgentId;


/**
 * A <code>SetRightRequest</code> instance is used by a client agent
 * for setting users right on a destination.
 */
public class SetRightRequest extends AdminRequest
{
  /** Identifier of the user, <code>null</code> stands for all users. */
  private AgentId client;
  /**
   * Right to set, (-)3 for (un)setting an admin right, (-)2 for
   * (un)setting a writing permission, (-)1 for (un)setting a reading
   * permission, and 0 for removing all the user's permissions.
   */
  private int right;

  
  /**
   * Constructs a <code>SetRightRequest</code> instance.
   *
   * @param id  Identifier of the request, may be null.
   * @param client  AgentId of client which right is to be set,
   *          <code>null</code> for all users.
   * @param right  Right to grant, authorized values: -3, -2, -1, 1, 2, 3.
   */
  public SetRightRequest(String id, AgentId client, int right)
  {
    super(id);
    this.client = client;
    this.right = right;
  }

 
  /** Returns the AgentId of the client which right is set. */
  public AgentId getClient()
  {
    return client;
  }

  /**
   * Returns the right to set, (-)3 for (un)setting an admin right, (-)2 for
   * (un)setting a writing permission, (-)1 for (un)setting a reading
   * permission, and 0 for removing all the user's permissions.
   */
  public int getRight()
  {
    return right;
  }
} 
