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
package archi;

import fr.dyade.aaa.joram.admin.*;

/**
 * Administers two agent servers for the archi samples.
 */
public class ArchiAdmin
{
  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Archi administration...");

    // Connecting the administrator:
    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    // Creating access for user anonymous on servers 0 and 2:
    User user0 = admin.createUser("anonymous", "anonymous", 0);
    User user2 = admin.createUser("anonymous", "anonymous", 2);

    // Creating the destinations on server 1:
    javax.jms.Queue queue = admin.createQueue(1);
    javax.jms.Topic topic = admin.createTopic(1);

    // Setting free access to the destinations:
    admin.setFreeReading(queue);
    admin.setFreeReading(topic);
    admin.setFreeWriting(queue);
    admin.setFreeWriting(topic);

    // Creating the connection factories for connecting to the servers 0 and 2:
    javax.jms.ConnectionFactory cf0 =
      admin.createConnectionFactory("localhost", 16010);
    javax.jms.ConnectionFactory cf2 =
      admin.createConnectionFactory("localhost", 16012);

    // Binding the objects in JNDI:
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admin closed.");
  } 
}
