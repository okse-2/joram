/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
package com.scalagent.ctrlgreen;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class ParameterPublisher {
  Destination dest = null;
  Session session = null;
  MessageProducer prod = null;

  /**
   * Initialize a Publisher.
   * @throws Exception
   */
  ParameterPublisher(Connection cnx, Destination dest) throws JMSException {
    if (dest == null)
      throw new IllegalStateException("ParameterPublisher: Cannot publish \"null\" destination");

    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    prod = session.createProducer(dest);
  }
  
  synchronized void publish(Parameters parameters) throws JMSException {
    ObjectMessage msg = session.createObjectMessage();
    msg.setObject(parameters);
    prod.send(msg);
  }

  synchronized void close() {
    try {
      if (session != null)
        session.close();
    } catch (JMSException exc) {
      Trace.error("ParameterPublisher: Cannot close session", exc);
    }
  }
}
