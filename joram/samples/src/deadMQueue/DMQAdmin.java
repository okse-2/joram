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

import fr.dyade.aaa.joram.admin.*;

import javax.jms.*;
import javax.naming.*;

/**
 * Administers an agent server for the deadMQueue samples.
 */
public class DMQAdmin
{
  static Context ictx = null; 

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("DMQ administration phase... ");

    Admin admin = new Admin("root", "root", 60);

    Queue queue = admin.createQueue("queue");
    Topic topic = admin.createTopic("topic");

    DeadMQueue userDmq = admin.createDeadMQueue("userDmq");
    DeadMQueue destDmq = admin.createDeadMQueue("destDmq");

    User anonymous = admin.createUser("anonymous", "anonymous");
    User dmqWatcher = admin.createUser("dmq", "dmq");

    ConnectionFactory cnxFact = admin.createConnectionFactory();

    anonymous.setDMQ(userDmq);
    admin.setDestinationDMQ("queue", destDmq);
    admin.setDestinationDMQ("topic", destDmq);

    anonymous.setThreshold(2);
    admin.setQueueThreshold(queue, 2);

    admin.setFreeReading("queue");
    admin.setFreeWriting("queue");
    admin.setFreeReading("topic");
    admin.setFreeWriting("topic");
    admin.setReader(dmqWatcher, "userDmq");
    admin.setWriter(dmqWatcher, "userDmq");
    admin.setReader(dmqWatcher, "destDmq");
    admin.setWriter(dmqWatcher, "destDmq");

    admin.close();
    System.out.println("Admin closed.");

    System.out.println("Binding objects in JNDI... ");
    ictx = new InitialContext();
    ictx.rebind("queue", queue);
    ictx.rebind("topic", topic);
    ictx.rebind("userDmq", userDmq);
    ictx.rebind("destDmq", destDmq);
    ictx.rebind("cnxFact", cnxFact);
    ictx.close();
    System.out.println("Objects binded.");
  }
}
