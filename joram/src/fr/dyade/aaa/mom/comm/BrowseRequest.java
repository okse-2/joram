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
 * A <code>BrowseRequest</code> instance is used by a <b>client</b> agent for 
 * requesting a "view" of the messages on a queue, without actually consuming
 * them.
 */
public class BrowseRequest extends AbstractRequest
{
  /**
   * String selector for filtering messages, null or empty for no selection.
   */
  private String selector;


  /**
   * Constructs a <code>BrowseRequest</code> instance involved in an
   * external client - MOM interaction.
   *
   * @param key  See superclass.
   * @param requestId  See superclass.
   * @param selector  Selector expression for filtering messages, null or empty
   *          for no selection.
   */
  public BrowseRequest(int key, String requestId, String selector)
  {
    super(key, requestId);
    this.selector = selector;
  }

  /**
   * Constructs a <code>BrowseRequest</code> instance not involved in an
   * external client - MOM interaction.
   *
   * @param requestId  See superclass.
   * @param selector  Selector expression for filtering messages, null or empty
   *          for no selection.
   */
  public BrowseRequest(String requestId, String selector)
  {
    this(0, requestId, selector);
  }


  /** Returns the selector of the request. */
  public String getSelector()
  {
    return selector;
  }
} 
