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
package cluster;

import javax.jms.*;
import javax.naming.*;

public class Subscriber20
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Subscribes and listens to topic on server2...");

    ictx = new InitialContext();
    ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf2");
    Topic dest = (Topic) ictx.lookup("top2");
    ictx.close();

    Connection cnx = cnxF.createConnection("subscriber20", "subscriber20");
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer sub = sess.createConsumer(dest);

    sub.setMessageListener(new Listener());

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println();
    System.out.println("Subscriber closed.");
  }
}
