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
package fr.dyade.aaa.mom.comm;

import fr.dyade.aaa.mom.messages.Message;

/**
 * A <code>QueueMsgReply</code> instance is used by a queue for replying to a
 * <code>ReceiveRequest</code> by sending a message to a client.
 */
public class QueueMsgReply extends AbstractReply
{
  /** The message sent by the queue. */
  private Message message;


  /**
   * Constructs a <code>QueueMsgReply</code> instance.
   *
   * @param req  The <code>ReceiveRequest</code> actually replied.
   * @param message  The message to send.
   */
  public QueueMsgReply(ReceiveRequest req, Message message)
  {
    super(req.getConnectionKey(), req.getRequestId());
    this.message = message;
  }


  /** Returns the message wrapped by this reply. */
  public Message getMessage()
  {
    return message;
  }
} 
