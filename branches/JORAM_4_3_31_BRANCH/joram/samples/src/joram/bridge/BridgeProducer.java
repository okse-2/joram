/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
 * Initial developer(s): Frederic Maistre (Bull SA)
 * Contributor(s):
 */
package bridge;

import javax.jms.*;

/**
 * Produces messages on the foreign destination.
 */
public class BridgeProducer
{
  public static void main(String[] args) throws Exception
  {
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    Queue queue = (Queue) jndiCtx.lookup("foreignDest");
    QueueConnectionFactory cnxFact =  
      (QueueConnectionFactory) jndiCtx.lookup("foreignCF");
    jndiCtx.close();

    QueueConnection cnx = cnxFact.createQueueConnection();
    QueueSession sess = cnx.createQueueSession(true, 0);
    QueueSender sender = sess.createSender(queue);

    TextMessage msg = sess.createTextMessage();

    for (int i = 1; i < 11; i++) {
      msg.setText("Foreign message number " + i);
      sender.send(msg);
    }

    sess.commit();

    cnx.close();
  }
}
