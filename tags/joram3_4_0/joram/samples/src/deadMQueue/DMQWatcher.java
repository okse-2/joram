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
package deadMQueue;

import javax.jms.*;
import javax.naming.*;

/**
 * Listens to the dead message queues.
 */
public class DMQWatcher
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Listens to the dead message queues...");

    ictx = new InitialContext();
    Queue userDmq = (Queue) ictx.lookup("userDmq");
    Queue destDmq = (Queue) ictx.lookup("destDmq");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cnxFact");
    ictx.close();

    Connection cnx = cf.createConnection("dmq", "dmq");

    Session session = cnx.createSession(true, 0);
    MessageConsumer userWatcher = session.createConsumer(userDmq);
    MessageConsumer destWatcher = session.createConsumer(destDmq);
    userWatcher.setMessageListener(new DMQListener("User DMQ"));
    destWatcher.setMessageListener(new DMQListener("Dest DMQ"));

    cnx.start();

    System.in.read();
    cnx.close();
  }
}
