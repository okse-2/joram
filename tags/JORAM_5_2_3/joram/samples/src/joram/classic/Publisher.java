/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
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
package classic;

import javax.jms.*;
import javax.naming.*;

/**
 * Publishes messages on the topic.
 */
public class Publisher {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Publishes messages...");

    ictx = new InitialContext();
    Topic topic = (Topic) ictx.lookup("topic");
    TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
    ictx.close();

    TopicConnection cnx = tcf.createTopicConnection();
    TopicSession session = cnx.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
    TopicPublisher publisher = session.createPublisher(topic);
    
    

    int i;
    for (i = 0; i < 10; i++) {
      TextMessage msg = session.createTextMessage();
      msg.setText("Test number " + i);
      publisher.publish(msg);
    }

    System.out.println(i + " messages published.");

    cnx.close();
  }
}
