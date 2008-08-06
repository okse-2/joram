/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package org.objectweb.joram.client.jms;

import javax.jms.JMSException;
import javax.jms.TopicSubscriber;

/**
 * Implements the <code>javax.jms.XATopicSession</code> interface.
 */
public class XATopicSession extends XASession
                            implements javax.jms.XATopicSession
{
  /**
   * Constructs an <code>XATopicSession</code> instance.
   *
   * @param cnx  The connection the session belongs to.
   * @param rm   The resource manager.
   *
   * @exception JMSException  Actually never thrown.
   */
  XATopicSession(XATopicConnection cnx, 
                 TopicSession ts, 
                 XAResourceMngr rm) throws JMSException
  {
    super(cnx, ts, rm);
  }

  
  /** Returns a String image of this session. */
  public String toString()
  {
    return "XATopicSess:" + sess.getId();
  }


  /** API method. */ 
  public javax.jms.TopicSession getTopicSession() throws JMSException
  {
    return (javax.jms.TopicSession) sess;
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(javax.jms.Topic topic,
                                          String selector,
                                          boolean noLocal)
         throws JMSException
  {
    return ((TopicSession) sess).createSubscriber(topic, selector, noLocal);
  }

  /**
   * Delegates the call to the wrapped JMS session.
   */
  public TopicSubscriber createSubscriber(javax.jms.Topic topic)
         throws JMSException
  {
    return ((TopicSession) sess).createSubscriber(topic);
  }
}
