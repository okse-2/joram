/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.jms;

import org.objectweb.joram.shared.client.TempDestDeleteRequest;

import java.util.Vector;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.naming.NamingException;

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

  /** 
   * Constructs an empty temporary queue.
   */
  public TemporaryQueue()
  {}

  /** Returns a String image of the queue. */
  public String toString()
  {
    return "TempQueue:" + agentId;
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
        if (agentId.equals(cons.targetName))
          throw new JMSException("Consumers still exist for this temp."
                                 + " queue.");
      }
    }
    // Sending the request to the server:
    cnx.syncRequest(new TempDestDeleteRequest(agentId));

    if (JoramTracing.dbgClient.isLoggable(BasicLevel.DEBUG))
      JoramTracing.dbgClient.log(BasicLevel.DEBUG, this + ": deleted.");
  }

  /**
   * Returns the connection this temporary queue belongs to,
   * <code>null</code> if not known.
   */
  public Connection getCnx()
  {
    return cnx;
  }

 
  /**
   * Decodes a <code>TemporaryQueue</code> which traveled through the
   * SOAP protocol.
   */  
  public Object decode(Hashtable h) {
    return new TemporaryQueue((String) h.get("agentId"), null);
  }
}
