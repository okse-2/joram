/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package classic;

import javax.jms.*;
import javax.naming.*;

/**
 * Subscribes and sets a listener to the topic.
 */
public class Subscriber
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Subscribes and listens to the topic...");

    ictx = new InitialContext();
    Topic topic = (Topic) ictx.lookup("topic");
    TopicConnectionFactory tcf = (TopicConnectionFactory) ictx.lookup("tcf");
    ictx.close();

    TopicConnection tc = tcf.createTopicConnection();
    TopicSession ts = tc.createTopicSession(true, 0);
    TopicSubscriber tsub = ts.createSubscriber(topic);

    tsub.setMessageListener(new MsgListener());

    tc.start();

    System.in.read();
    ts.commit();

    tc.close();

    System.out.println();
    System.out.println("Subscription closed.");
  }
}
