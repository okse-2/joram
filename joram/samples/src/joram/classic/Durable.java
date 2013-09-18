/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 - 2013 ScalAgent Distributed Technologies
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

import javax.jms.Topic;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Subscribes and sets a listener to the topic.
 */
public class Durable {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    System.out.println("Subscribes and listens to the topic...");

    ictx = new InitialContext();
    Topic topic = (Topic) ictx.lookup("topic");
    ConnectionFactory tcf = (ConnectionFactory) ictx.lookup("tcf");
    ictx.close();

    Connection cnx = tcf.createConnection();
    cnx.setClientID("cnx_dursub");
    Session session = cnx.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    TopicSubscriber subscriber = session.createDurableSubscriber(topic, "durable");
    subscriber.setMessageListener(new MsgListener());

    cnx.start();

    System.in.read();

    cnx.close();

    System.out.println("Subscription closed.");
  }
}
