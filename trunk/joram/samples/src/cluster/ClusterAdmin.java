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
package cluster;

import fr.dyade.aaa.joram.admin.*;

/**
 * Administers three agent servers for the cluster sample.
 */
public class ClusterAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Cluster administration...");

    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    User user00 = admin.createUser("publisher00", "publisher00", 0);
    User user10 = admin.createUser("subscriber10", "subscriber10", 1);
    User user20 = admin.createUser("subscriber20", "subscriber20", 2); 
    User user21 = admin.createUser("subscriber21", "subscriber21", 2);

    javax.jms.ConnectionFactory cf0 =
      admin.createConnectionFactory("localhost", 16010);
    javax.jms.ConnectionFactory cf1 =
      admin.createConnectionFactory("localhost", 16011);
    javax.jms.ConnectionFactory cf2 =
      admin.createConnectionFactory("localhost", 16012);

    javax.jms.Topic top0 = admin.createTopic(0);
    javax.jms.Topic top1 = admin.createTopic(1);
    javax.jms.Topic top2 = admin.createTopic(2);

    admin.setFreeReading(top0);
    admin.setFreeReading(top1);
    admin.setFreeReading(top2);
    admin.setFreeWriting(top0);
    admin.setFreeWriting(top1);
    admin.setFreeWriting(top2);

    admin.setCluster(top0, top1);
    admin.setCluster(top0, top2);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("cf0", cf0);
    jndiCtx.bind("cf1", cf1);
    jndiCtx.bind("cf2", cf2);
    jndiCtx.bind("top0", top0);
    jndiCtx.bind("top1", top1);
    jndiCtx.bind("top2", top2);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admins closed.");
  }
}
