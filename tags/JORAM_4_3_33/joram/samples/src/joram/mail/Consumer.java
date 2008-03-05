/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2006 ScalAgent Distributed Technologies
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
package mail;

import javax.jms.*;
import javax.naming.*;

/**
 * Consumes messages from the queue and from the topic.
 */
public class Consumer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Trace1");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("mailQueue");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    System.out.println("Trace2");

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer cons = sess.createConsumer(queue);

    System.out.println("Listens to the MailQueue...");
    System.out.println("hit a key to stop.");

    cons.setMessageListener(new MsgListener("Queue listener"));

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println("Consumer closed.");
  }
}
