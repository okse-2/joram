/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2010 ScalAgent Distributed Technologies
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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Updates properties on the URL acquisition queue to change the acquisition
 * period and URL.
 */
public class Producer {
  static Context ictx = null;

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("update properties on the collector queue...");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
//    Topic topic = (Topic) ictx.lookup("topic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(null);

    Message msg = sess.createMessage();
    msg.setStringProperty("expiration", "0");
    msg.setStringProperty("persistent", "true");
    msg.setStringProperty("acquisition.period", "30000");
    msg.setStringProperty("collector.url", "http://www.gnu.org/licenses/lgpl-3.0.txt");
    msg.setStringProperty("collector.type", "5");
    producer.send(queue, msg);
//    producer.send(topic, msg);

    System.out.println("messages sent.");

    cnx.close();
  }
}
