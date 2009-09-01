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
 * Contributor(s):
 */
package classic;

import javax.jms.*;
import javax.naming.*;

/**
 * Subscribes and sets a listener to the topic.
 */
public class Subscriber {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Subscribes and listens to the topic...");

    ictx = new InitialContext();
    Topic topic = (Topic) ictx.lookup("topic");
    TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
    ictx.close();

    TopicConnection cnx = tcf.createTopicConnection();
    TopicSession session = cnx.createTopicSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    TopicSubscriber subscriber = session.createSubscriber(topic);
    subscriber.setMessageListener(new MsgListener());

    cnx.start();

    System.in.read();

    cnx.close();

    System.out.println("Subscription closed.");
  }
}
