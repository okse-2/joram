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
 * A <code>PingRequest</code> is used by a <b>client</b> agent for checking
 * the existence of a destination agent.
 */
public class PingRequest extends AbstractRequest
{
  /**
   * Constructs a <code>PingRequest</code> involved in an external client -
   * MOM interaction.
   *
   * @param key  See superclass.
   * @param requestId  See superclass.
   */
  public PingRequest(int key, String requestId)
  {
    super(key, requestId);
  }

  /**
   * Constructs a <code>PingRequest</code> not involved in an external client -
   * MOM interaction.
   *
   * @param requestId  See superclass.
   */
  public PingRequest(String requestId)
  {
    super(0, requestId);
  }
}
