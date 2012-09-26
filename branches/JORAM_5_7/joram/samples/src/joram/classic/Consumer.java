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
 * Consumes messages from the queue and from the topic.
 */
public class Consumer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Listens to " + args[0]);

    ictx = new InitialContext();
    Destination dest = (Destination) ictx.lookup(args[0]);
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer recv = sess.createConsumer(dest);

    recv.setMessageListener(new MsgListener("Listener on " + args[0]));

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println("Consumer closed.");
  }
}
