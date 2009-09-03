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
package classic;

import javax.jms.*;
import javax.naming.*;

/**
 * Sends messages on the queue.
 */
public class Sender
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Sends messages on the queue...");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
    ictx.close();

    QueueConnection qc = qcf.createQueueConnection();
    QueueSession qs = qc.createQueueSession(true, 0);
    QueueSender qsend = qs.createSender(queue);
    TextMessage msg = qs.createTextMessage();

    int i;
    for (i = 0; i < 10; i++) {
      msg.setText("Test number " + i);
      qsend.send(msg);
    }

    qs.commit();

    System.out.println(i + " messages sent.");

    qc.close();
  }
}
