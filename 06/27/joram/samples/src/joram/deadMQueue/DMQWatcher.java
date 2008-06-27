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

import javax.jms.*;
import javax.naming.*;

/**
 * Listens to the dead message queues.
 */
public class DMQWatcher
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Listens to the dead message queues...");

    ictx = new InitialContext();
    Queue userDmq = (Queue) ictx.lookup("userDmq");
    Queue destDmq = (Queue) ictx.lookup("destDmq");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cnxFact");
    ictx.close();

    Connection cnx = cf.createConnection("dmq", "dmq");

    Session session = cnx.createSession(true, 0);
    MessageConsumer userWatcher = session.createConsumer(userDmq);
    MessageConsumer destWatcher = session.createConsumer(destDmq);
    userWatcher.setMessageListener(new DMQListener("User DMQ"));
    destWatcher.setMessageListener(new DMQListener("Dest DMQ"));

    cnx.start();

    System.in.read();
    cnx.close();
  }
}
