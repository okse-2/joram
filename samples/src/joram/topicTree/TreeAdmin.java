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

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers an agent server for the topic tree samples.
 */
public class TreeAdmin {

  public static void main(String[] args) throws Exception {
    
    System.out.println();
    System.out.println("Tree administration...");

    AdminModule.connect("root", "root", 60);

    Topic news = Topic.create(0);
    Topic business = Topic.create(0);
    Topic sports = Topic.create(0);
    Topic tennis = Topic.create(0);

    business.setParent(news);
    sports.setParent(news);
    tennis.setParent(sports);

    javax.jms.ConnectionFactory cf =
      TcpConnectionFactory.create("localhost", 16010);

    User.create("anonymous", "anonymous", 0);

    news.setFreeReading();
    news.setFreeWriting();
    business.setFreeReading();
    business.setFreeWriting();
    sports.setFreeReading();
    sports.setFreeWriting();
    tennis.setFreeReading();
    tennis.setFreeWriting();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("news", news);
    jndiCtx.bind("business", business);
    jndiCtx.bind("sports", sports);
    jndiCtx.bind("tennis", tennis);
    jndiCtx.bind("cf", cf);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
