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
 * A <code>TSubReceiveRequest</code> is sent by a <code>TopicSubscriber</code>
 * requesting to receive a message.
 */
public class TSubReceiveRequest extends AbstractJmsRequest
{
  /** The name of the subscription on which requesting the message. */
  private String subName;
  /** The time to live value of the request (negative for infinite). */
  private long timeToLive;


  /**
   * Constructs a <code>TSubReceiveRequest</code>.
   *
   * @param subName  The name of the subscription on which requesting the
   *          message.
   * @param timeToLive  Time to live value in milliseconds, negative for
   *          infinite.
   */
  public TSubReceiveRequest(String subName, long timeToLive)
  {
    super(null);
    this.subName = subName;
    this.timeToLive = timeToLive;
  }

  /** Returns the name of the subscription on which requesting the message. */
  public String getSubName()
  {
    return subName;
  }

  /** Returns the time to live value in milliseconds. */
  public long getTimeToLive()
  {
    return timeToLive;
  }
}
