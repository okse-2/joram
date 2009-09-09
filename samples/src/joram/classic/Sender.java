/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s):
 */
package classic;

import javax.jms.*;
import javax.naming.*;

/**
 * Sends messages on the queue.
 */
public class Sender {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Sends messages on the queue...");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
    ictx.close();

    QueueConnection cnx = qcf.createQueueConnection();
    QueueSession session = cnx.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    QueueSender sender = session.createSender(queue);
    
    for (int i = 0; i < 10; i++) {
      TextMessage msg = session.createTextMessage();
      msg.setText("Test number " + i);
      sender.send(msg);
    }

    System.out.println("10 messages sent.");

    cnx.close();
  }
}
