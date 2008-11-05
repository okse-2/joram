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
 * update properties on the collector queue and on the collector topic.
 */
public class Producer {
  static Context ictx = null;

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("update properties on the collector queue and on the collector topic...");

    ictx = new InitialContext();
    Queue queue = (Queue) ictx.lookup("queue");
//    Topic topic = (Topic) ictx.lookup("topic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(null);

    Message msg = sess.createMessage();
    msg.setStringProperty("collector.expirationMessage", "0");
    msg.setStringProperty("collector.persistentMessage", "true");
    msg.setStringProperty("collector.period", "30000");
    msg.setStringProperty("collector.url", "http://svn.forge.objectweb.org/cgi-bin/viewcvs.cgi/*checkout*/joram/trunk/joram/history");
    msg.setStringProperty("collector.type", "5");
    producer.send(queue, msg);
//    producer.send(topic, msg);

    System.out.println("messages sent.");

    cnx.close();
  }
}
