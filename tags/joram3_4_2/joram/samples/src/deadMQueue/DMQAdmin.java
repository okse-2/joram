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

/**
 * Administers an agent server for the deadMQueue samples.
 */
public class DMQAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("DMQ administration...");

    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    javax.jms.Queue queue = admin.createQueue(0);
    javax.jms.Topic topic = admin.createTopic(0);

    DeadMQueue userDmq = admin.createDeadMQueue(0);
    DeadMQueue destDmq = admin.createDeadMQueue(0);

    User ano = admin.createUser("anonymous", "anonymous", 0);
    User dmq = admin.createUser("dmq", "dmq", 0);

    javax.jms.ConnectionFactory cnxFact =
      admin.createConnectionFactory("localhost", 16010);

    admin.setUserDMQ(ano, userDmq);
    admin.setDestinationDMQ(queue, destDmq);
    admin.setDestinationDMQ(topic, destDmq);

    admin.setUserThreshold(ano, 2);
    admin.setQueueThreshold(queue, 2);

    admin.setFreeReading(queue);
    admin.setFreeWriting(queue);
    admin.setFreeReading(topic);
    admin.setFreeWriting(topic);
    admin.setReader(dmq, userDmq);
    admin.setWriter(dmq, userDmq);
    admin.setReader(dmq, destDmq);
    admin.setWriter(dmq, destDmq);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("userDmq", userDmq);
    jndiCtx.bind("destDmq", destDmq);
    jndiCtx.bind("cnxFact", cnxFact);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admin closed.");
  }
}
