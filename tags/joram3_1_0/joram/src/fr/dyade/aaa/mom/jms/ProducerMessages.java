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
 * A <code>ProducerMessages</code> instance is sent by a
 * <code>MessageProducer</code> when sending messages.
 */
public class ProducerMessages extends AbstractJmsRequest
{
  /** The produced messages. */
  private Vector messages;

  /**
   * Constructs a <code>ProducerMessages</code> instance.
   *
   * @param dest  Name of the destination the messages are sent to.
   */
  public ProducerMessages(String dest)
  {
    super(dest);
    messages = new Vector();
  }

  /** Adds a message to deliver. */
  public void addMessage(Message msg)
  {
    messages.add(msg);
  }

  /** Adds messages to deliver. */
  public void addMessages(Vector msgs)
  {
    messages.addAll(msgs);
  }

  /** Returns the vector of sent messages. */
  public Vector getMessages()
  {
    return messages;
  }
}
