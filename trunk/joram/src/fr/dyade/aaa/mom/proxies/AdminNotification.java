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
import fr.dyade.aaa.agent.Notification;

/**
 * An <code>AdminNotification</code> is sent by a <code>JmsProxy</code>
 * dedicated to an administrator for registering itself to the local
 * <code>AdminTopic</code>.
 */
public class AdminNotification extends Notification
{
  /** The administrator's name. */
  private String name;
  /** The administrator's password. */
  private String pass;

  /**
   * Constructs an <code>AdminNotification</code> instance.
   *
   * @param name  The name of the administrator.
   * @param pass  The password of the administrator.
   */
  AdminNotification(String name, String pass)
  {
    this.name = name;
    this.pass = pass;
  }


  /** Returns the name of the administrator. */
  public String getName()
  {
    return name;
  }

  /** Returns the password of the administrator. */
  public String getPass()
  {
    return pass;
  }
}
