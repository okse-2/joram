/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package collector;

import javax.jms.*;
import javax.naming.*;

/**
 * Consumes messages from the queue and from the topic.
 */
public class Consumer {
  static Context ictx = null;

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Listens to the collector queue and to the collector topic...");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
    // Topic topic = (Topic) ictx.lookup("topic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer recv = sess.createConsumer(queue);
    // MessageConsumer subs = sess.createConsumer(topic);

    recv.setMessageListener(new MsgListener("Collector Queue listener"));
    // subs.setMessageListener(new MsgListener("Colector Topic listener"));

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println();
    System.out.println("Consumer closed.");
  }
}
