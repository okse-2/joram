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

import fr.dyade.aaa.mom.messages.Message;

import java.util.Vector;

/**
 * A <code>SubMessages</code> is used by a JMS proxy for sending messages
 * to a subscriber.
 */
public class SubMessages extends AbstractJmsReply
{
  /** The name of the subscription the messages are destinated to. */
  private String subName;
  /** The vector of messages carried by this reply. */
  private Vector messages;

  /**
   * Constructs a <code>SubMessages</code> instance.
   *
   * @param correlationId  See superclass.
   * @param name  The name of the subscription the messages are destinated to.
   * @param messages  The vector of sent messages.
   */
  public SubMessages(String correlationId, String subName, Vector messages)
  {
    super(correlationId);
    this.subName = subName;
    this.messages = messages;
  }

  /** Constructs an empty <code>SubMessages</code> instance. */
  public SubMessages()
  {
    super(null);
    this.messages = new Vector();
  }

  /** Returns the name of the subscription the messages are destinated to. */
  public String getSubName()
  {
    return subName;
  }

  /** Returns the vector of sent messages. */
  public Vector getMessages()
  {
    return messages;
  }

  /** Returns the first vector element. */
  public Message getMessage()
  {
    return (Message) messages.get(0);
  }
}
