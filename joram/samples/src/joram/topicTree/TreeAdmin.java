/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
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
