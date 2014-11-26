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
package joram.ctrlgreen;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Session;

public class InventoryPublisher {
  Destination dest = null;
  Session session = null;
  MessageProducer prod = null;

  /**
   * Initialize a Publisher.
   * @throws Exception
   */
  InventoryPublisher(Connection cnx, Destination dest) throws JMSException {
    if (dest == null)
      throw new IllegalStateException("InventoryPublisher: Cannot publish \"null\" destination");

    session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    prod = session.createProducer(dest);
  }
  
  synchronized void publish(String inventory) throws JMSException {
    TextMessage msg = session.createTextMessage();
    msg.setText(inventory);
    prod.send(msg);
  }

  synchronized void close() {
    try {
      if (session != null)
        session.close();
    } catch (JMSException exc) {
      Trace.error("InventoryPublisher: Cannot close session", exc);
    }
  }
}
