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

import javax.jms.JMSException;
import javax.jms.IllegalStateException;

/**
 * Implements the <code>javax.jms.TopicConnection</code> interface.
 */
public class TopicConnection extends Connection
                             implements javax.jms.TopicConnection
{
  /**
   * Creates a <code>TopicConnection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param connectionImpl  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public TopicConnection(FactoryParameters factoryParameters,
                         ConnectionItf connectionImpl) throws JMSException
  {
    super(factoryParameters, connectionImpl);
  }


  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target destination does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer
         createConnectionConsumer(javax.jms.Topic topic, String selector,
                                  javax.jms.ServerSessionPool sessionPool,
                                  int maxMessages) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new ConnectionConsumer(this, (Topic) topic, selector,
                                  sessionPool, maxMessages);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception InvalidSelectorException  If the selector syntax is wrong.
   * @exception InvalidDestinationException  If the target topic does
   *              not exist.
   * @exception JMSException  If the method fails for any other reason.
   */
  public javax.jms.ConnectionConsumer
         createDurableConnectionConsumer(javax.jms.Topic topic, String subName,
                                         String selector,
                                         javax.jms.ServerSessionPool sessPool,
                                         int maxMessages) throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new ConnectionConsumer(this, (Topic) topic, subName, selector,
                                  sessPool, maxMessages);
  }

  /**
   * API method.
   * 
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.TopicSession
         createTopicSession(boolean transacted, int acknowledgeMode)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");
    
    return new TopicSession(this, transacted, acknowledgeMode);
  }
}
