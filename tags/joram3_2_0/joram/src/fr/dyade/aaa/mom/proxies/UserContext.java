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
package fr.dyade.aaa.mom.proxies;

import fr.dyade.aaa.agent.AgentId;

/** 
 * The <code>UserContext</code> class is used for keeping a user's
 * references.
 */
class UserContext implements java.io.Serializable
{
  /** Password of the user. */
  String password;
  /** <code>true</code> if the user is an administrator. */
  boolean admin = false;
  /** Identifier of the user's JMS proxy, if any. */
  AgentId proxyId = null;

  /**
   * Constructs a <code>UserContext</code>.
   *
   * @param password  User's password.
   * @param admin  <code>true</code> for an administrator.
   */
  UserContext(String password, boolean admin)
  {
    this.password = password;
    this.admin = admin;
  }
}