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
 * A <code>CreateUserRequest</code> instance requests the creation of a JMS
 * user proxy.
 */
public class CreateUserRequest extends AdminRequest
{
  /** Name of the user. */
  private String userName;
  /** Password of the user. */
  private String userPass;
  /** Id of the server where deploying the proxy. */
  private int serverId;

  /**
   * Constructs a <code>CreateUserRequest</code> instance.
   *
   * @param userName  The name of the user.
   * @param userPass  The password of the user.
   * @param serverId  The id of the server where deploying its proxy.
   */
  public CreateUserRequest(String userName, String userPass, int serverId)
  {
    this.userName = userName;
    this.userPass = userPass;
    this.serverId = serverId;
  }

  /** Returns the name of the user to create. */
  public String getUserName()
  {
    return userName;
  }
  
  /** Returns the password of the user. */
  public String getUserPass()
  {
    return userPass;
  }

  /** Returns the id of the server where deploying its proxy. */
  public int getServerId()
  {
    return serverId;
  }
}
