/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 2004 - ScalAgent Distributed Technologies
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
 * Initial developer(s): Jose Carlos Waeny
 * Contributor(s): Frederic Maistre (INRIA), Nicolas Tachker (ScalAgent)
 */
package chat;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.tcp.*;

/**
 * Launching JORAM administration:
 * connecting to JORAM server, creating chat agents and creating chat topic
 *
 * @author	JC Waeny 
 * @email       jc@waeny.2y.net
 * @version     1.0
 */
public class ChatAdmin
{
  public static void main(String args[]) throws Exception
  {
    System.out.println();
    System.out.println("Chat administration phase... ");

    // Connecting to JORAM server:
    AdminModule.connect("root", "root", 60);

    // Creating the JMS administered objects:        
    javax.jms.ConnectionFactory connFactory =
      TcpConnectionFactory.create("localhost", 16010);

    Topic topic = (Topic) Topic.create(0);

    // Creating an access for user anonymous:
    User user = User.create("anonymous", "anonymous", 0);

    // Setting free access to the topic:
    topic.setFreeReading();
    topic.setFreeWriting();

    // Binding objects in JNDI:
    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("factoryChat", connFactory);
    jndiCtx.bind("topicChat", topic);
    jndiCtx.close();
    
    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
