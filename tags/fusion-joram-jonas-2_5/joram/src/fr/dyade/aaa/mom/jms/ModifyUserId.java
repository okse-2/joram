/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.mom.jms;

/**
 * A <code>ModifyUserId</code> notification is used by an administrator to
 * modify the identification of a user.
 */
public class ModifyUserId extends JmsAdminRequest
{
  /** String name of the user to modify. */
  private String name;
  /** New name of the user to set. */
  private String newName;
  /** New password of the user to set. */
  private String newPass;

  /**
   * Constructs a <code>ModifyUserId</code> instance.
   *
   * @param name  String name of the user.
   * @param newName  New user name.
   * @param newPass  New user password.
   */
  public ModifyUserId(String name, String newName, String newPass)
  {
    this.name = name;
    this.newName = newName;
    this.newPass = newPass;
  }


  /** Returns the name of the user. */
  public String getName()
  {
    return name;
  }

  /** Returns the new user name. */
  public String getNewName()
  {
    return newName;
  }

  /** Returns the new user password. */
  public String getNewPass()
  {
    return newPass;
  }
}
