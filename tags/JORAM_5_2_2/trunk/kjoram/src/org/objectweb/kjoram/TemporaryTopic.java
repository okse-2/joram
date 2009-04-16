/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package org.objectweb.kjoram;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * 
 */
public class TemporaryTopic extends Topic {
  public static Logger logger = Debug.getLogger(Session.class.getName());
  
  private final static String TMP_TOPIC_TYPE = "topic.tmp";

  public static boolean isTemporaryTopic(String type) {
    return type.equals(TMP_TOPIC_TYPE);
  }

  private Requestor requestor;

  public TemporaryTopic() {}

  /** 
   * Constructs a temporary topic.
   *
   * @param agentId  Identifier of the topic agent.
   * @param cnx  The connection the queue belongs to, <code>null</code> if
   *          not known. 
   */
  public TemporaryTopic(String agentId, Requestor requestor) {
    super(agentId, TMP_TOPIC_TYPE);
    this.requestor = requestor;
  }

  /** Returns a String image of the topic. */
  public String toString() {
    return "TempTopic:" + getUID();
  }

  /**
   *  delete a temporary topic.
   *
   * @exception IllegalStateException  If the connection is closed or broken.
   * @exception JMSException  If the request fails for any other reason.
   */
  public void delete() throws JoramException {
    if (requestor == null)
      throw new SecurityException(
          "Forbidden call as this TemporaryQueue" +
          " does not belong to this requestor.");

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "--- " + this + ": deleting...");

    // Sending the request to the server:
    if (requestor != null) {
      requestor.request(new TempDestDeleteRequest(getUID()));
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this + ": deleted.");
  }
}
