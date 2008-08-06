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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package soap;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.*;
import org.objectweb.joram.client.jms.soap.TopicSoapConnectionFactory;


/**
 * Administers a platform for the soap samples.
 */
public class SoapAdmin
{
  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Soap administration...");

    TopicSoapConnectionFactory soapCf = (TopicSoapConnectionFactory)
      TopicSoapConnectionFactory.create("localhost", 8080, 60);
    soapCf.getParameters().connectingTimer = 60;

    AdminModule.connect(soapCf, "root", "root");

    Queue queue = (Queue) Queue.create(0);
    Topic topic = (Topic) Topic.create(0);

    User soapUser = User.create("anonymous", "anonymous", 1);

    queue.setFreeReading();
    queue.setFreeWriting();
    topic.setFreeReading();
    topic.setFreeWriting();

    AdminModule.disconnect();

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("soapCf", soapCf);
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("topic", topic);
    jndiCtx.close();

    System.out.println("Admin finished.");
  }
}
