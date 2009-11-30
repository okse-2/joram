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
package deadMQueue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Listens to the dead message queues.
 */
public class DMQWatcher {
  public static void main(String[] args) throws Exception {
    System.out.println("Listens to the dead message queue...");

    Context ictx = new InitialContext();
    Queue dmq = (Queue) ictx.lookup("dmq");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection("anonymous", "anonymous");

    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer consumer = session.createConsumer(dmq);

    cnx.start();
    
    for (int i=0; i<3; i++) {
      TextMessage msg = (TextMessage) consumer.receive();
      System.out.println("\nreceives: \"" + msg.getText() + "\", " +
                         "JMS_JORAM_ERRORCOUNT=" + msg.getIntProperty("JMS_JORAM_ERRORCOUNT"));
      System.out.println("JMS_JORAM_ERRORCAUSE_1=" + msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1") + ", " +
                         "JMS_JORAM_ERRORCODE_1=" + msg.getStringProperty("JMS_JORAM_ERRORCODE_1"));
      System.out.println("JMSXDeliveryCount=" + msg.getIntProperty("JMSXDeliveryCount"));
    }

    cnx.close();
  }
}
