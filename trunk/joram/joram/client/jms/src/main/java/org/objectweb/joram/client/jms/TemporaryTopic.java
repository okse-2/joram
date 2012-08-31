/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
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


import javax.jms.JMSException;
import javax.jms.JMSSecurityException;

import org.objectweb.joram.shared.client.TempDestDeleteRequest;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * Implements the <code>javax.jms.TemporaryTopic</code> interface.
 * <p>
 * A TemporaryTopic object is a Topic object created for the duration of a
 * Connection. It is a system-defined topic that can be consumed only by the
 * Connection that created it. A TemporaryTopic object can be created either
 * at the Session or TopicSession level.
 */
public class TemporaryTopic extends Topic implements javax.jms.TemporaryTopic {
  /** define serialVersionUID for interoperability */
  private static final long serialVersionUID = 1L;
  
  /** The connection the topic belongs to, <code>null</code> if not known. */
  private Connection cnx;

  // Used by jndi2 SoapObjectHelper
  public TemporaryTopic() {}

  /** 
   * Constructs a temporary topic.
   *
   * @param agentId  Identifier of the topic agent.
   * @param cnx  The connection the queue belongs to, <code>null</code> if
   *          not known. 
   */
  public TemporaryTopic(String agentId, Connection cnx) {
    super(agentId, (byte) (TOPIC_TYPE | TEMPORARY));
    this.cnx = cnx;
  }

  /** Returns a String image of the topic. */
  public String toString() {
    return "TemporaryTopic" + agentId;
  }

  /**
   * API method.
   * Deletes this temporary topic. If there are existing subscribers still using it,
   * a JMSException will be thrown.
   * 
   * @exception IllegalStateException  If the connection is closed or broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void delete() throws JMSException {
    if (cnx == null)
      throw new JMSSecurityException("Forbidden call as this TemporaryQueue does not belong to this connection.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": deleting...");

    // Checking the connection's subscribers:
    cnx.checkConsumers(agentId);

    // Sending the request to the server:
    cnx.syncRequest(new TempDestDeleteRequest(agentId));

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": deleted.");
  }

  /**
   * Returns the connection this temporary topic belongs to,
   * <code>null</code> if not known.
   */
  Connection getCnx() {
    return cnx;
  }
}
