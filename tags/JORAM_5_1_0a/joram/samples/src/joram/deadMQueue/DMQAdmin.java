/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - Bull SA
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
    
    System.out.println();
    System.out.println("DMQ administration...");

    AdminModule.connect("root", "root", 60);

    Queue queue = Queue.create(0);

    DeadMQueue dmq = (DeadMQueue) DeadMQueue.create(0);

    User anoUser = User.create("anonymous", "anonymous", 0);
    User dmqUser = User.create("dmq", "dmq", 0);

    ConnectionFactory cnxFact = TcpConnectionFactory.create("localhost", 16010);

    queue.setDMQ(dmq);
    queue.setThreshold(2);
    queue.setFreeReading();
    queue.setFreeWriting();
    
    dmq.setReader(dmqUser);
    dmq.setWriter(dmqUser);

    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    jndiCtx.bind("queue", queue);
    jndiCtx.bind("dmq", dmq);
    jndiCtx.bind("cnxFact", cnxFact);
    jndiCtx.close();

    AdminModule.disconnect();
    System.out.println("Admin closed.");
  }
}
