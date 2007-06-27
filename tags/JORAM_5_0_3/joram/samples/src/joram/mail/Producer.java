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
 * Produces messages on the queue and on the topic.
 */
public class Producer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    ictx = new InitialContext();
    Topic topic = (Topic) ictx.lookup("mailTopic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(true, 0);
    MessageProducer producer = sess.createProducer(topic);

    System.out.println("Produces 5 messages on the MailTopic.");

    for (int i=0; i<5; i++) {
      TextMessage msg = sess.createTextMessage();

      msg.setBooleanProperty("showProperties", true);
      msg.setStringProperty("property1", "valeur#" + i);
      msg.setIntProperty("property2", i);
      msg.setText("Queue : Test number #" + i);
      producer.send(msg);
    }
    sess.commit();

    System.out.println("Messages sent.");

    cnx.close();
  }
}
