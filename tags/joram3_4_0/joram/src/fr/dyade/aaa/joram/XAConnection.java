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
 * Implements the <code>javax.jms.XAConnection</code> interface.
 */
public class XAConnection extends Connection implements javax.jms.XAConnection
{
  /**
   * Creates an <code>Connection</code> instance.
   *
   * @param factoryParameters  The factory parameters.
   * @param connectionImpl  The actual connection to wrap.
   *
   * @exception JMSSecurityException  If the user identification is incorrect.
   * @exception IllegalStateException  If the server is not listening.
   */
  public XAConnection(FactoryParameters factoryParameters,
                      ConnectionItf connectionImpl) throws JMSException
  {
    super(factoryParameters, connectionImpl);
  }


  /** 
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   * @exception JMSException  In case of an invalid acknowledge mode.
   */
  public javax.jms.Session
         createSession(boolean transacted, int acknowledgeMode)
         throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");

    return new Session(this, transacted, acknowledgeMode);
  }

  /** 
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed.
   */
  public javax.jms.XASession createXASession() throws JMSException
  {
    if (closed)
      throw new IllegalStateException("Forbidden call on a closed"
                                      + " connection.");
    return new XASession(this);
  }
}
