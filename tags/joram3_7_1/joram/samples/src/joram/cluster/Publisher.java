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

public class Publisher
{
  static Context ictx;

  public static void main(String[] arg) throws Exception
  {
    System.out.println();
    System.out.println("Publishes messages on topic on server0...");

    ictx = new InitialContext();
    ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf0");
    Topic dest = (Topic) ictx.lookup("top0");
    ictx.close();

    Connection cnx = cnxF.createConnection("publisher00", "publisher00");
    Session sess = cnx.createSession(true, 0);
    MessageProducer pub = sess.createProducer(dest);

    TextMessage msg = sess.createTextMessage();

    int i;
    for (i = 0; i < 10; i++) {
      msg.setText("Msg " + i);
      pub.send(msg);
    }

    sess.commit();

    System.out.println(i + " messages published.");

    cnx.close();
  }
}
