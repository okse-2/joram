/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
 * A <code>Monitor_GetDestinations</code> instance requests the list of
 * the destinations of a given server.
 */
public class Monitor_GetDestinations extends Monitor_Request
{
  /** Identifier of the target server. */
  private int serverId;

  
  /**
   * Constructs a <code>Monitor_GetDestinations</code> instance.
   *
   * @param serverId  Identifier of the target server.
   */
  public Monitor_GetDestinations(int serverId)
  {
    this.serverId = serverId;
  }


  /** Returns the identifier of the target server. */
  public int getServerId()
  {
    return serverId;
  }
}
