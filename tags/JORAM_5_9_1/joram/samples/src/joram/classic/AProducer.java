/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package classic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;

public class AProducer {
  public static void main(String[] args) throws Exception {
    Session session = null;
    Connection cnx = null;
    MessageProducer  producer = null;
    TextMessage message = null;
    Queue queue = null;
    ConnectionFactory cf = null;

    Context initialContext = new InitialContext();
    cf = (ConnectionFactory) initialContext.lookup("cf");
    queue = (Queue) initialContext.lookup("queue");
    initialContext.close();

    cnx = cf.createConnection();
    
    session = cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
    cnx.start();

    producer = session.createProducer(queue);
    message = session.createTextMessage();

    for (int i = 1; i <= 100000; i++) {
      message.setText("Test number " + i);
      producer.send(message);

      if ((i % 100) == 0)session.commit();

      if ((i % 1000) == 0) System.out.println(i + " messages sent");
    }
    session.commit();
    
    producer.close();
    session.close();
    cnx.close();
  }
}

