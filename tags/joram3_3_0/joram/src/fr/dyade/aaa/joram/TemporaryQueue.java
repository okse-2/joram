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

import fr.dyade.aaa.mom.jms.TempDestDeleteRequest;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.TemporaryQueue</code> interface.
 */
public class TemporaryQueue extends Queue implements javax.jms.TemporaryQueue
{
  /** The connection the queue belongs to, <code>null</code> if not known. */
  private Connection cnx;

  /** 
   * Constructs a temporary queue.
   *
   * @param agentId  Identifier of the queue agent.
   * @param cnx  The connection the queue belongs to, <code>null</code> if
   *          not known.
   */
  public TemporaryQueue(String agentId, Connection cnx)
  {
    super(agentId);
    this.cnx = cnx;
  }

  /** Returns a String image of the queue. */
  public String toString()
  {
    return "TempQueue:" + getName();
  }

  /**
   * API method.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void delete() throws JMSException
  {
    if (cnx == null)
      throw new JMSSecurityException("Forbidden call as this TemporaryQueue"
                                     + " does not belong to this connection.");

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, "--- " + this
                                 + ": deleting...");

    // Checking the connection's receivers:
    Session sess;
    MessageConsumer cons;
    for (int i = 0; i < cnx.sessions.size(); i++) {
      sess = (Session) cnx.sessions.get(i);
      for (int j = 0; j < sess.consumers.size(); j++) {
        cons = (MessageConsumer) sess.consumers.get(j);
        if (getName().equals(cons.targetName))
          throw new JMSException("Consumers still exist for this temp."
                                 + " queue.");
      }
    }
    // Sending the request to the server:
    cnx.syncRequest(new TempDestDeleteRequest(getName()));

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": deleted.");
  }

  /**
   * Returns the connection this temporary queue belongs to,
   * <code>null</code> if not known.
   */
  Connection getCnx()
  {
    return cnx;
  }
}
