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

/**
 * A <code>QueueMessage</code> is used by a JMS client proxy for
 * forwarding a queue <code>QueueMsgReply</code> holding a message, and
 * actually replying to a <code>QRecReceiveRequest</code>.
 */
public class QueueMessage extends AbstractJmsReply
{
  /** The message carried by this reply. */
  private Message message = null;

  /**
   * Constructs a <code>QueueMessage</code> instance.
   *
   * @param destReply  The <code>fr.dyade.aaa.mom.comm.QueueMsgReply</code>
   *          actually forwarded.
   */
  public QueueMessage(fr.dyade.aaa.mom.comm.QueueMsgReply destReply)
  {
    super(destReply.getCorrelationId());
    this.message = destReply.getMessage();
  }

  /** Constructs an empty <code>QueueMessage</code> instance. */
  public QueueMessage()
  {
    super(null);
  }

  /** Returns the message carried by this reply. */
  public Message getMessage()
  {
    return message;
  }
}
