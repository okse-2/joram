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
 * An <code>AddAdminId</code> notification is sent by a JMS administrator
 * to its proxy for adding a new admin identification.
 */
public class AddAdminId extends JmsAdminRequest
{
  /** New admin name. */
  private String name;
  /** New admin password. */
  private String pass;

  /**
   * Constructs an <code>AddAdminId</code> instance.
   *
   * @param name  New admin name.
   * @param pass  New admin password.
   */
  public AddAdminId(String name, String pass)
  {
    this.name = name;
    this.pass = pass;
  }

  /** Returns the new admin name. */
  public String getName()
  {
    return name;
  }

  /** Returns the new password. */
  public String getPass()
  {
    return pass;
  }
}
