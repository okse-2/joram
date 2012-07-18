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
public class Admin {
	
	private static final String propFile = "elasticity.properties";
		
	public static void main(String args[]) throws Exception {
		
		System.out.println("[Admin]\tStarted...");
		
		Properties props = new Properties();
		InputStream reader = new FileInputStream(propFile);
		props.load(reader);
		reader.close();
		
		// Connecting the administrator:
		AdminModule.connect("root", "root", 60);
				
		// Creating access for user anonymous on servers
		User.create("anonymous", "anonymous", 0);
		User.create("anonymous", "anonymous", 1);
		User.create("anonymous", "anonymous", 2);
	    
		Queue rq2 = Queue.create(2);
		
		Properties propAQ = new Properties();
		propAQ.setProperty("remoteAgentID",rq2.getName()); // + ";" + rq2.getName());
		//propAQ.setProperty("period",String.valueOf(Constants.QUEUE_PERIOD));
		Queue aq0 = Queue.create(0,"org.objectweb.joram.mom.dest.AliasInQueue",propAQ);
		Queue aq1 = Queue.create(1,"org.objectweb.joram.mom.dest.AliasInQueue",propAQ);

		// Setting free access to the destinations:
		aq0.setFreeWriting();
		aq1.setFreeWriting();
		rq2.setFreeReading();
		rq2.setFreeWriting();
		
		// Creating the connection factories for connecting to the servers:
		javax.jms.ConnectionFactory cf0 =
				TcpConnectionFactory.create(props.getProperty("serv0_ip"), 16010);
		javax.jms.ConnectionFactory cf1 =
				TcpConnectionFactory.create(props.getProperty("serv1_ip"), 16011);
		javax.jms.ConnectionFactory cf2 =
				TcpConnectionFactory.create(props.getProperty("serv2_ip"), 16012);

		// Binding the objects in JNDI:
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("alias0", aq0);
		jndiCtx.bind("alias1", aq1);
		jndiCtx.bind("remote2", rq2);
		jndiCtx.bind("cf0", cf0);
		jndiCtx.bind("cf1", cf1);
		jndiCtx.bind("cf2", cf2);
		jndiCtx.close();
		
		AdminModule.disconnect();
		System.out.println("[Admin]\tDone.");
	} 
}
