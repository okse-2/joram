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
 * A <code>TSubAckRequest</code> instance is used by a
 * <code>TopicSubscriber</code> for acknowledging a received message.
 */
public class TSubAckRequest extends AbstractJmsRequest
{
  /** Subscription where acknowledging the received message. */
  private String subName;
  /** Message identifier. */
  private String id;

  /**
   * Constructs a <code>TSubAckRequest</code> instance.
   *
   * @param subName  Name of the subscription where acknowledging the message.
   * @param id  The message identifier.
   */
  public TSubAckRequest(String subName, String id)
  {
    super(null);
    this.subName = subName;
    this.id = id;
  }


  /** Returns the name of the subscription where acknowledging the message. */
  public String getSubName()
  {
    return subName;
  }

  /** Returns the acknowledged message identifier. */
  public String getId()
  {
    return id;
  }
}
