/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
package cluster;

import javax.jms.*;
import javax.naming.*;

public class Publisher
{
  static Context ictx;

  public static void main(String[] arg) throws Exception
  {
    System.out.println();
    System.out.println("Publishes messages on topic on server0...");

    ictx = new InitialContext();
    TopicConnectionFactory cnxF = (TopicConnectionFactory) ictx.lookup("tcf0");
    Topic dest = (Topic) ictx.lookup("top0");
    ictx.close();

    TopicConnection cnx = cnxF.createTopicConnection("publisher00",
                                                     "publisher00");
    TopicSession sess = cnx.createTopicSession(true, 0);
    TopicPublisher pub = sess.createPublisher(dest);

    TextMessage msg = sess.createTextMessage();

    int i;
    for (i = 0; i < 10; i++) {
      msg.setText("Msg " + i);
      pub.publish(msg);
    }

    sess.commit();

    System.out.println(i + " messages published.");

    cnx.close();
  }
}
