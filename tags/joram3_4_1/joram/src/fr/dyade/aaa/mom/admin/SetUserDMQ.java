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
 * A <code>SetUserDMQ</code> instance requests to set a given DMQ as the
 * DMQ for a given user.
 */
public class SetUserDMQ extends AdminRequest
{
  /** Identifier of the user's proxy the DMQ is set for. */
  private String userProxId;
  /** Identifier of the DMQ. */
  private String dmqId;

  /**
   * Constructs a <code>SetUserDMQ</code> instance.
   *
   * @param userId  Identifier of the user's proxy the DMQ is set for.
   * @param dmqId  Identifier of the DMQ.
   */
  public SetUserDMQ(String userProxId, String dmqId)
  {
    this.userProxId = userProxId;
    this.dmqId = dmqId;
  }

  
  /** Returns the ProxId of the user the DMQ is set for. */
  public String getUserProxId()
  {
    return userProxId;
  }

  /** Returns the identifier of the DMQ. */
  public String getDmqId()
  {
    return dmqId;
  }
}