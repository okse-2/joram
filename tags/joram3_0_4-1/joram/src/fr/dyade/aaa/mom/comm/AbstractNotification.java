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
package fr.dyade.aaa.mom.comm;

/**
 * The <code>AbstractNotification</code> class is the superclass of the
 * notifications exchanged between a client agent and a MOM destination agent.
 */
public abstract class AbstractNotification
                    extends fr.dyade.aaa.agent.Notification
{
  /**
   * In the case where the client agent is a proxy agent linking an external
   * client with the MOM, this field identifies the connection context of
   * the interaction the notification is involved in.
   * <p>
   * Keeping this information finally allows the proxy to route a reply to the 
   * external client through the right connection.
   * <p>
   * When the client is not a proxy, this field default value is 0.
   */
  private int connectionKey = 0;


  /**
   * Constructs an <code>AbstractNotification</code>.
   *
   * @param key  Key of the connection used for reaching the external client,
   *          taken into account if positive.
   */
  public AbstractNotification(int key)
  {
    if (key > 0)
      connectionKey = key;
  }


  /** Returns the connection key, 0 if no connection is involved. */
  public int getConnectionKey()
  {
    return connectionKey;
  }
}
