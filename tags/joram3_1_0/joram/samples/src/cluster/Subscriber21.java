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

public class Subscriber21
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Subscribes and listens to topic on server2...");

    ictx = new InitialContext();
    ConnectionFactory cnxF = (ConnectionFactory) ictx.lookup("cf2");
    Topic dest = (Topic) ictx.lookup("top2");
    ictx.close();

    Connection cnx = cnxF.createConnection("subscriber21", "subscriber21");
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer sub = sess.createConsumer(dest);

    sub.setMessageListener(new Listener());

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println();
    System.out.println("Subscriber closed.");
  }
}
