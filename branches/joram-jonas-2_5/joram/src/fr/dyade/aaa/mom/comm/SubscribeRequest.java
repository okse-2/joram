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
 * A <code>SubscribeRequest</code> instance is used by a <b>client</b> agent
 * for subscribing to a topic.
 */
public class SubscribeRequest extends AbstractRequest
{
  /**
   * String name of the subscription, null and empty strings are equally
   * considered as the default id.
   */
  private String name = "";
  /**
   * String selector for filtering messages, null or empty string for no
   * selection.
   */
  private String selector;

  /**
   * Constructs a <code>SubscribeRequest</code> instance involved in an
   * external client - MOM interaction.
   *
   * @param key  See superclass.
   * @param requestId See superclass.
   * @param name  String name of the subscription, null or empty string are
   *          equally considered as the default id.
   * @param selector  Selector expression for filtering messages, null or 
   *          empty string for no selection.
   */
  public SubscribeRequest(int key, String requestId, String name,
                          String selector)
  {
    super(key, requestId);
    if (name != null)
      this.name = name;
    this.selector = selector;
  }

  /**
   * Constructs a <code>SubscribeRequest</code> instance not involved in an
   * external client - MOM interaction.
   *
   * @param requestId See superclass.
   * @param name  String name of the subscription, null or empty string are
   *          equally considered as the default id.
   * @param selector  Selector expression for filtering messages, null or 
   *          empty string for no selection.
   */
  public SubscribeRequest(String requestId, String name, String selector)
  {
    this(0, requestId, name, selector);
  }


  /** Returns the subscription name. */
  public String getName()
  {
    return name;
  }

  /** Returns the request selector. */
  public String getSelector()
  {
    return selector;
  }
} 
