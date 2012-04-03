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
package alias;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import java.util.Properties;

/**
 * Manages a distributed architecture.
 * Creates a producer and 3 consumers on 3 JORAM servers.
 */
public class Admin {

	public static void main(String args[]) throws Exception {
		
		System.out.println("[Admin]\tStarted...");

		// Connecting the administrator:
		AdminModule.connect("root", "root", 60);

		// Creating access for user anonymous on servers 0 and 2:
		User.create("anonymous", "anonymous", 0);
		User.create("anonymous", "anonymous", 1);
		User.create("anonymous", "anonymous", 2);
		User.create("anonymous", "anonymous", 3);
		User.create("anonymous", "anonymous", 4);

		// Creating the clustered remote queues, and alias queue:
	    /*
		Properties propCQ = new Properties();
	    propCQ.setProperty("period","1000");
	    propCQ.setProperty("producThreshold","2000");
	    propCQ.setProperty("consumThreshold","1000");
	    propCQ.setProperty("autoEvalThreshold","false");
	    propCQ.setProperty("waitAfterClusterReq","5000");

	    Queue rq1 = Queue.create(1, null, Queue.CLUSTER_QUEUE, propCQ);
	    Queue rq2 = Queue.create(2, null, Queue.CLUSTER_QUEUE, propCQ);
	    
	    rq1.addClusteredQueue(rq2);
		*/
	    
		Queue rq1 = Queue.create(1);
		Queue rq2 = Queue.create(2);
		Queue rq3 = Queue.create(3);
		Queue rq4 = Queue.create(4);
		
		Properties propAQ = new Properties();
		propAQ.setProperty("remoteAgentID",rq1.getName());// + ";" + rq2.getName()); //+ ";" + rq3.getName());
		//propAQ.setProperty("period",String.valueOf(Constants.QUEUE_PERIOD));
		Queue aq = Queue.create(0,"org.objectweb.joram.mom.dest.AliasInQueue",propAQ);

		// Setting free access to the destinations:
		rq1.setFreeReading();
		rq1.setFreeWriting();
		rq2.setFreeReading();
		rq2.setFreeWriting();
		rq3.setFreeReading();
		rq3.setFreeWriting();
		rq4.setFreeReading();
		rq4.setFreeWriting();
		aq.setFreeWriting();
		
		// Creating the connection factories for connecting to the servers:
		javax.jms.ConnectionFactory cf0 =
				TcpConnectionFactory.create("10.0.0.2", 16010);		
		javax.jms.ConnectionFactory cf1 =
				TcpConnectionFactory.create("10.0.0.3", 16011);
		javax.jms.ConnectionFactory cf2 =
				TcpConnectionFactory.create("10.0.0.3", 16012);
		javax.jms.ConnectionFactory cf3 =
				TcpConnectionFactory.create("10.0.0.4", 16013);
		javax.jms.ConnectionFactory cf4 =
				TcpConnectionFactory.create("10.0.0.4", 16014);
		
		// Binding the objects in JNDI:
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("alias", aq);
		jndiCtx.bind("remote1", rq1);
		jndiCtx.bind("remote2", rq2);
		jndiCtx.bind("remote3", rq3);
		jndiCtx.bind("remote4", rq4);
		jndiCtx.bind("cf0", cf0);
		jndiCtx.bind("cf1", cf1);
		jndiCtx.bind("cf2", cf2);
		jndiCtx.bind("cf3", cf3);
		jndiCtx.bind("cf4", cf4);
		jndiCtx.close();

		AdminModule.disconnect();
		System.out.println("[Admin]\tDone.");
	} 
}
