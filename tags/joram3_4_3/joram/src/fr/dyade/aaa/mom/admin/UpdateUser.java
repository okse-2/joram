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
 * An <code>UpdateUser</code> instance requests the modification of a
 * user identification
 */
public class UpdateUser extends AdminRequest
{
  /** Name of the user. */
  private String userName;
  /** Identifier of the user's proxy. */
  private String proxId;
  /** New name of the user. */
  private String newName;
  /** New password of the user. */
  private String newPass;

  /**
   * Constructs an <code>UpdateUser</code> instance.
   *
   * @param userName  The name of the user.
   * @param proxId  Identifier of the user's proxy.
   * @param newName  The new name of the user.
   * @param newPass  The new password of the user.
   */
  public UpdateUser(String userName, String proxId, String newName,
                    String newPass)
  {
    this.userName = userName;
    this.proxId = proxId;
    this.newName = newName;
    this.newPass = newPass;
  }

  /** Returns the name of the user to update. */
  public String getUserName()
  {
    return userName;
  }

  /** Returns the user's proxy identifier. */
  public String getProxId()
  {
    return proxId;
  }

  /** Returns the new name of the user. */
  public String getNewName()
  {
    return newName;
  }
  
  /** Returns the new password of the user. */
  public String getNewPass()
  {
    return newPass;
  }
}
