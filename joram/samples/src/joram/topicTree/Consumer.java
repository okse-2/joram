/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
package topicTree;

import javax.jms.*;
import javax.naming.*;

/**
 * Consumes messages from the topic hierarchy.
 */
public class Consumer
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Listens to the topics...");

    ictx = new InitialContext();
    Topic news = (Topic) ictx.lookup("news");
    Topic business = (Topic) ictx.lookup("business");
    Topic sports = (Topic) ictx.lookup("sports");
    Topic tennis = (Topic) ictx.lookup("tennis");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false,
                                     javax.jms.Session.AUTO_ACKNOWLEDGE);
    MessageConsumer newsConsumer = sess.createConsumer(news);
    MessageConsumer businessConsumer = sess.createConsumer(business);
    MessageConsumer sportsConsumer = sess.createConsumer(sports);
    MessageConsumer tennisConsumer = sess.createConsumer(tennis);

    newsConsumer.setMessageListener(new MsgListener("News reader got: "));
    businessConsumer.setMessageListener(new MsgListener("Business reader"
                                                        + " got: "));
    sportsConsumer.setMessageListener(new MsgListener("Sports reader got: "));
    tennisConsumer.setMessageListener(new MsgListener("Tennis reader got: "));

    cnx.start();

    System.in.read();

    cnx.close();

    System.out.println();
    System.out.println("Consumers closed.");
  }
}
