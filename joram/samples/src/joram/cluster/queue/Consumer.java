/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 France Telecom R&D
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
package cluster.queue;

import javax.jms.*;
import javax.naming.*;

/**
 * Consumes messages from the cluster queue.
 */
public class Consumer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    ConnectionFactory cf = null;
    Queue dest = null;

    if (args.length != 1)
     throw new Exception("Bad number of argument");

    ictx = new InitialContext();
    try {
      if (args[0].equals("-")) {
        // Choose a connection factory and the associated topic depending of
        // the location property.
        cf = (ConnectionFactory) ictx.lookup("clusterCF");
        dest = (Queue) ictx.lookup("clusterQueue");
      } else {
        cf = (ConnectionFactory) ictx.lookup("cf" + args[0]);
        dest = (Queue) ictx.lookup("queue" + args[0]);
        System.setProperty("location", "server" + args[0]);
      }
    } finally {
      ictx.close();
    }

    Connection cnx = cf.createConnection("anonymous", "anonymous");
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer sub = session.createConsumer(dest);

    String location = System.getProperty("location");
    if (location != null)
      System.out.println("Subscribes and listens to queue on " + location);

    sub.setMessageListener(new MsgListener("location" + location + " listener"));
    cnx.start();

    System.out.println("Press a key to exit..");
    System.in.read();

    cnx.close();
  }
}
