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
package fr.dyade.aaa.joram;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;


/**
 * Implements the <code>javax.jms.QueueReceiver</code> interface.
 */
public class QueueReceiver extends MessageConsumer
                           implements javax.jms.QueueReceiver
{
  /**
   * Constructs a receiver.
   *
   * @param sess  The session the receiver belongs to.
   * @param queue  The queue the receiver consumes on.
   * @param selector  The selector for filtering messages.
   *
   * @exception IllegalStateException  If the connection is broken.
   * @exception JMSException  If the creation fails for any other reason.
   */
  QueueReceiver(QueueSession sess, Queue queue,
                String selector) throws JMSException
  {
    super(sess, queue, selector);
  }

  /** Returns a string view of this receiver. */
  public String toString()
  {
    return "QueueRec:" + sess.ident;
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the receiver is closed.
   */
  public javax.jms.Queue getQueue() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed receiver.");

    return (javax.jms.Queue) dest;
  }
}
