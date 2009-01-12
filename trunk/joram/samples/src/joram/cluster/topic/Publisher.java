/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package cluster.topic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

public class Publisher {
  
  static Context ictx = null;

  public static void main(String[] args) throws Exception {
    
    ConnectionFactory cf = null;
    Topic dest = null;

    if (args.length != 1)
     throw new Exception("Bad number of argument");

    ictx = new InitialContext();
    try {
      if (args[0].equals("-")) {
        // Choose a connection factory and the associated topic depending of
        // the location property.
        cf = (ConnectionFactory) ictx.lookup("clusterCF");
        dest = (Topic) ictx.lookup("clusterTopic");
      } else {
        cf = (ConnectionFactory) ictx.lookup("cf" + args[0]);
        dest = (Topic) ictx.lookup("topic" + args[0]);
        System.setProperty("location", "server" + args[0]);
      }
    } finally {
      ictx.close();
    }

    Connection cnx = cf.createConnection("anonymous", "anonymous");
    Session session = cnx.createSession(true, 0);
    MessageProducer pub = session.createProducer(dest);

    String location = System.getProperty("location");
    if (location != null)
      System.out.println("Publishes messages on topic on " + location);

    TextMessage msg = session.createTextMessage();

    int i;
    for (i = 0; i < 10; i++) {
      msg.setText("Msg " + i);
      pub.send(msg);
    }
    session.commit();

    System.out.println(i + " messages published.");

    cnx.close();
  }
}
