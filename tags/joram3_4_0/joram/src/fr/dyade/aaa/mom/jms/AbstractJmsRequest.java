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
package fr.dyade.aaa.mom.jms;

/**
 * The <code>AbstractJmsRequest</code> class is used by Joram clients for
 * sending requests to their MOM JMS proxies.
 */
public abstract class AbstractJmsRequest implements java.io.Serializable
{
  /**
   * The request target is either a destination agent name, or a subscription 
   * name.
   */
  private String target;
  /** Identifier of the request. */
  private String requestId;


  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   *
   * @param target  String identifier of the request target, either a queue
   *          name, or a subscription name.
   */
  public AbstractJmsRequest(String target)
  {
    this.target = target;
  }

  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   */
  public AbstractJmsRequest()
  {}

  /** Sets the target. */
  public void setTarget(String target)
  {
    this.target = target;
  }

  /** Sets the request identifier. */
  public void setRequestId(String requestId)
  {
    this.requestId = requestId;
  }
  
  /** Returns the String identifier of this request target.  */
  public String getTarget()
  {
    return target;
  }

  /** Returns this request identifier. */
  public String getRequestId()
  {
    return requestId;
  }
}
