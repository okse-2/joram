/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 2004 - Bull SA
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package deadMQueue;

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Administers an agent server for the deadMQueue samples.
 */
public class DMQAdmin {

  public static void main(String[] args) throws Exception {
    System.out.println("DMQ administration...");

    AdminModule.connect("root", "root", 60);

    User.create("anonymous", "anonymous", 0);    

    ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);

    DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);
    dmq.setFreeReading();
    dmq.setFreeWriting();
    
    Queue queue1 = Queue.create(0);
    queue1.setFreeReading();
    queue1.setFreeWriting();

    queue1.setDMQ(dmq);
    queue1.setThreshold(2);
    
    Queue queue2 = Queue.create(0);

    queue2.setDMQ(dmq);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue1", queue1);
    jndiCtx.bind("queue2", queue2);
    jndiCtx.bind("dmq", dmq);
    jndiCtx.bind("cf", cf);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
