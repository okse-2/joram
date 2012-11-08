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
package perfs;

import javax.jms.*;
import javax.naming.*;

/**
 */
public class PerfsPublisher
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    ictx = new InitialContext();
    Topic queue = (Topic) ictx.lookup("topic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(queue);

    BytesMessage msg = sess.createBytesMessage();
    byte[] content = new byte[1024];
    for (int i = 0; i< 1024; i++)
      content[i] = (byte) (i & 0xFF); // non-zero byte values from -127 to 128
    msg.writeBytes(content);

    int counter = 0;
    while (true) {
      counter++;
      msg.setLongProperty("time", System.currentTimeMillis());
      producer.send(msg);
      msg.clearProperties();

      if (counter == 50) {
        counter = 0;
        //sess.commit();
      }
    }
  }
}
