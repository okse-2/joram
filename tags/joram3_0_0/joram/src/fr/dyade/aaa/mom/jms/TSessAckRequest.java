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

import java.util.Vector;

/**
 * A <code>TSessAckRequest</code> instance is used by a
 * <code>TopicSession</code> for acknowledging the messages it consumed.
 */
public class TSessAckRequest extends AbstractJmsRequest
{
  /** Name of the subscription where acknowledging the messages. */
  private String subName;
  /** Vector of message identifiers. */
  private Vector ids;

  /**
   * Constructs a <code>TSessAckRequest</code> instance.
   *
   * @param subName  Name of the subscription where acknowledging the messages.
   * @param ids  Vector of acknowledged message identifiers.
   */
  public TSessAckRequest(String subName, Vector ids)
  {
    super(null);
    this.subName = subName;
    this.ids = ids;
  }

  /** Returns the name of the subscription where acknowledging the messages. */
  public String getSubName()
  {
    return subName;
  }

  /** Returns the vector of acknowledged messages identifiers. */
  public Vector getIds()
  {
    return ids;
  }
}
