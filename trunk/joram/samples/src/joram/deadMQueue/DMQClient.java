/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
package deadMQueue;

import org.objectweb.joram.client.jms.admin.*;

import javax.jms.*;
import javax.naming.*;

/**
 * Producer/Consumer generating dead messages.
 */
public class DMQClient
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    Topic topic = (Topic) ictx.lookup("topic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cnxFact");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session prodSession = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Session consSession = cnx.createSession(true, 0);
    MessageProducer qProducer = prodSession.createProducer(queue);
    MessageProducer tProducer = prodSession.createProducer(topic);
    
    MessageConsumer qConsumer = consSession.createConsumer(queue);
    MessageConsumer tConsumer = consSession.createConsumer(topic);

    cnx.start();

    TextMessage msg = prodSession.createTextMessage();

    // Producing expired messages:
    msg.setText("Expiry test");
    qProducer.send(msg, javax.jms.DeliveryMode.NON_PERSISTENT, 4, 1);
    tProducer.send(msg, javax.jms.DeliveryMode.NON_PERSISTENT, 4, 1);

    qConsumer.receiveNoWait();
    tConsumer.receiveNoWait();

    // Producing "undeliverable" messages: 
    msg.setText("Undeliverability test");
    qProducer.send(msg);
    tProducer.send(msg);
    qConsumer.receive();
    tConsumer.receive();
    consSession.rollback();
    qConsumer.receive();
    tConsumer.receive();
    consSession.rollback();

    cnx.close();
  }
}
