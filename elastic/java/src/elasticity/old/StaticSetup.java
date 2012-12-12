/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2004 Bull SA
 * Copyright (C) 2001 - 2004 ScalAgent Distributed Technologies
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
 * Contributor(s): Nicolas Tachker (ScalAgent), Ahmed El Rheddane (INRIA)
 */
package elasticity.old;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Manages a distributed architecture.
 * Creates a producer and 3 consumers on 3 JORAM servers.
 */
public class StaticSetup {
		
	public static void main(String args[]) throws Exception {
		
		System.out.println("[StaticSetup]\tStarted...");
		
		// Connecting the administrator:
		AdminModule.connect("10.0.0.2",16010,"root","root", 60);
		
		System.out.println("[StaticSetup]\tConnected to Admin Module.");
		// Creating access for user anonymous on servers
		User.create("anonymous", "anonymous", 0);
		User.create("anonymous", "anonymous", 1);
		User.create("anonymous", "anonymous", 2);
		User.create("anonymous", "anonymous", 3);
	    
		// Creating the destinations on servers
		Queue rq1 = Queue.create(1);
		Queue rq2 = Queue.create(2);
		Queue rq3 = Queue.create(3);
		
		Properties propAQ = new Properties();
		propAQ.setProperty("remoteAgentID",rq1.getName()); //+ ";" + rq2.getName() + ";" + rq3.getName());
		//propAQ.setProperty("period",String.valueOf(Constants.QUEUE_PERIOD));
		Queue aq0 = Queue.create(0,"org.objectweb.joram.mom.dest.AliasInQueue",propAQ);

		// Setting free access to the destinations:
		aq0.setFreeWriting();
		rq1.setFreeReading();
		rq1.setFreeWriting();
		rq2.setFreeReading();
		rq2.setFreeWriting();
		rq3.setFreeReading();
		rq3.setFreeWriting();
		
		// Creating the connection factories for connecting to the servers:
		javax.jms.ConnectionFactory cf0 =
				TcpConnectionFactory.create("10.0.0.2", 16010);
		javax.jms.ConnectionFactory cf1 =
				TcpConnectionFactory.create("10.0.0.3", 16011);
		javax.jms.ConnectionFactory cf2 =
				TcpConnectionFactory.create("10.0.0.4", 16012);
		javax.jms.ConnectionFactory cf3 =
				TcpConnectionFactory.create("10.0.0.5", 16013);
		
		// Binding the objects in JNDI:
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("alias0", aq0);
		jndiCtx.bind("remote1", rq1);
		jndiCtx.bind("remote2", rq2);
		jndiCtx.bind("remote3", rq3);
		jndiCtx.bind("cf0", cf0);
		jndiCtx.bind("cf1", cf1);
		jndiCtx.bind("cf2", cf2);
		jndiCtx.bind("cf3", cf3);
		jndiCtx.close();
		
		AdminModule.disconnect();
		System.out.println("[StaticSetup]\tDone.");
	} 
}
