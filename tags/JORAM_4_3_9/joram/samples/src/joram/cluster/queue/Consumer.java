/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - France Telecom R&D
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package cluster.queue;

import javax.jms.*;
import javax.naming.*;

/**
 * Consumes messages from the cluster queue.
 */
public class Consumer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {

    int i = new Integer(args[0]).intValue();
    System.setProperty("location", ""+i);

    System.out.println();
    System.out.println("Listens to the cluster queue " + i);

    ictx = new InitialContext();
    Destination queue = (Destination) ictx.lookup("clusterQueue");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("qcf"+i);
    ictx.close();

    Connection cnx = cf.createConnection("user"+i,"user"+i);
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer recv = sess.createConsumer(queue);
    recv.setMessageListener(new MsgListener("location" + i + " listener"));

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println();
    System.out.println("Consumer closed.");
  }
}
