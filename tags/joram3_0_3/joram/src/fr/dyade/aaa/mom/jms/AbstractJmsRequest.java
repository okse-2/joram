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
 * The <code>AbstractJmsRequest</code> class is used by Joram for sending
 * requests to MOM JMS proxies.
 */
public abstract class AbstractJmsRequest implements java.io.Serializable
{
  /**
   * Identifier of the MOM agent the request is destinated to, null if it is
   * the proxy.
   */
  private String to;
  /** Identifier of the request. */
  private String requestId;


  /**
   * Constructs an <code>AbstractJmsRequest</code>.
   *
   * @param to  String identifier of the MOM agent the request is destinated
   *          to, null if it is the client proxy.
   */
  public AbstractJmsRequest(String to)
  {
    this.to = to;
  }

  /** Sets the request identifier. */
  public void setIdentifier(String requestId)
  {
    this.requestId = requestId;
  }

  /**
   * Returns the String identifier of the MOM agent this request is destinated
   * to.
   */
  public String getTo()
  {
    return to;
  }

  /** Returns this request identifier. */
  public String getRequestId()
  {
    return requestId;
  }
}
