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

import fr.dyade.aaa.mom.messages.Message;

import java.util.Vector;

/**
 * A <code>QBrowseReply</code> instance is used by a JMS client proxy for
 * forwarding a <code>BrowseReply</code> destination notification,
 * actually replying to a client <code>QBrowseRequest</code>.
 */
public class QBrowseReply extends AbstractJmsReply
{
  /** The vector of messages carried by this reply. */
  private Vector messages;

  /**
   * Constructs a <code>QBrowseReply</code> instance.
   *
   * @param destReply  The queue reply actually forwarded.
   */
  public QBrowseReply(fr.dyade.aaa.mom.comm.BrowseReply destReply)
  {
    super(destReply.getCorrelationId());
    this.messages = destReply.getMessages();
  }

  /**
   * Constructs a <code>QBrowseReply</code> instance.
   *
   * @param correlationId  The identifier of the replied request.
   * @param messages  The vector of browsed messages.
   */
  public QBrowseReply(String correlationId, Vector messages)
  {
    super(correlationId);
    this.messages = messages;
  }

  /** Returns the vector of messages carried by this reply. */
  public Vector getMessages()
  {
    return messages;
  }

  /**
   * Transforms this reply into a vector of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Vector soapCode()
  {
    Vector vec = new Vector();

    vec.add("QBrowseReply");

    // Coding the reply fields:
    vec.add(getCorrelationId());
    
    // Coding and adding the messages into a vector:
    Vector msgs = new Vector();
    while (! messages.isEmpty())
      msgs.add(((Message) messages.remove(0)).soapCode());

    vec.add(msgs);

    return vec;
  }

  /** 
   * Transforms a vector of primitive values into a
   * <code>QBrowseReply</code> reply.
   */
  public static QBrowseReply soapDecode(Vector vec)
  {
    vec.remove(0);

    String correlationId = (String) vec.remove(0);
    Vector codedMsgs = (Vector) vec.remove(0);

    Vector decodedMsgs = new Vector();
    // Decoding the messages:
    while (! codedMsgs.isEmpty())
      decodedMsgs.add(Message.soapDecode((Vector) codedMsgs.remove(0)));

    return new QBrowseReply(correlationId, decodedMsgs);
  }
}
