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

/**
 * An <code>UnsubscribeRequest</code> instance is used by a <b>client</b> agent
 * for removing one of or all its subscriptions to a topic.
 */
public class UnsubscribeRequest extends AbstractRequest
{
  /**
   * Name of the subscription to delete, null for deleting all subscriber's
   * subscriptions.
   */
  private String name;


  /**
   * Constructs an <code>UnsubscribeRequest</code> instance involved in an
   * external client - MOM interaction.
   *
   * @param key  See superclass.
   * @param requestId  See superclass.
   * @param name  Name of the subscription to delete, null for deleting all
   *          the requester's subscriptions.
   */
  public UnsubscribeRequest(int key, String requestId, String name)
  {
    super(key, requestId);
    this.name = name;
  }
    
  /**
   * Constructs an <code>UnsubscribeRequest</code> instance not involved in an
   * external client - MOM interaction.
   *
   * @param requestId  See superclass.
   * @param name  Name of the subscription to delete, null for deleting all
   *          the requester's subscriptions.
   */
  public UnsubscribeRequest(String requestId, String name)
  {
    this(0, requestId, name);
  }


  /**
   * Returns the name of the subscription to remove, null for all requester's
   * subscriptions.
   */
  public String getName()
  {
    return name;
  }
} 
