/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
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
