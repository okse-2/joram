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

import fr.dyade.aaa.joram.admin.*;


/**
 * Administers an agent server for the topic tree samples.
 */
public class TreeAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Tree administration...");

    AdminItf admin = new fr.dyade.aaa.joram.admin.AdminImpl();
    admin.connect("root", "root", 60);

    javax.jms.Topic news = admin.createTopic(0);
    javax.jms.Topic business = admin.createTopic(0);
    javax.jms.Topic sports = admin.createTopic(0);
    javax.jms.Topic tennis = admin.createTopic(0);

    admin.setFather(news, business);
    admin.setFather(news, sports);
    admin.setFather(sports, tennis);

    javax.jms.ConnectionFactory cf =
      admin.createConnectionFactory("localhost", 16010);

    User user = admin.createUser("anonymous", "anonymous", 0);

    admin.setFreeReading(news);
    admin.setFreeWriting(news);
    admin.setFreeReading(business);
    admin.setFreeWriting(business);
    admin.setFreeReading(sports);
    admin.setFreeWriting(sports);
    admin.setFreeReading(tennis);
    admin.setFreeWriting(tennis);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("news", news);
    jndiCtx.bind("business", business);
    jndiCtx.bind("sports", sports);
    jndiCtx.bind("tennis", tennis);
    jndiCtx.bind("cf", cf);
    jndiCtx.close();

    admin.disconnect();
    System.out.println("Admin closed.");
  }
}
