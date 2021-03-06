/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
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
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */

package elasticity.eval;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Sets up the Joram configuration for tests.
 * 
 * @author Ahmed El Rheddane
 */
public class Setup {
	
	
	private static Server[] servers;
	
	private static String getServerAddress(int id) {
		for (Server s : servers) {
			if (s.getId() == id)
				return s.getHostName(); 
		}
		return null;
	}
	
	public static void main(String args[]) throws Exception {
		System.out.println("[Setup]\tStarted...");
		
		// Connecting the administrator (using TcpProxyService port)
		AdminModule.connect("localhost",16101,"root","root", 120);
		servers = AdminModule.getServers();
		
		// Creating access for user anonymous on servers
		User.create("anonymous", "anonymous", 101);
		User.create("anonymous", "anonymous", 102);
		User.create("anonymous", "anonymous", 1);
		User.create("anonymous", "anonymous", 2);
	    
		//Worker
		Queue rq1 = Queue.create(1,"queue1");
		
		//Producers
		Properties propAQ = new Properties();
		propAQ.setProperty("remoteAgentID",rq1.getName());
		Queue aq1 = Queue.create(101,"queue101","org.objectweb.joram.mom.dest.AliasInQueue",propAQ);
		Queue aq2 = Queue.create(102,"queue102","org.objectweb.joram.mom.dest.AliasInQueue",propAQ);
		
		//Setting free access to the destinations
		aq1.setFreeWriting();
		aq2.setFreeWriting();
		rq1.setFreeReading();
		rq1.setFreeWriting();
		
		// Creating the connection factories for connecting to the servers:
		ConnectionFactory cfp1 =
				TcpConnectionFactory.create(getServerAddress(101), 16101);
		ConnectionFactory cfp2 =
				TcpConnectionFactory.create(getServerAddress(102), 16102);
		ConnectionFactory cfw1 =
				TcpConnectionFactory.create(getServerAddress(1), 16001);
		ConnectionFactory cfw2 =
				TcpConnectionFactory.create(getServerAddress(2), 16002);
		
		cfp1.getParameters().connectingTimer = 120;
		cfp2.getParameters().connectingTimer = 120;

		// Binding the objects in JNDI:
		Context jndiCtx = new InitialContext();
		jndiCtx.bind("producer1", aq1);
		jndiCtx.bind("producer2", aq2);
		jndiCtx.bind("worker1", rq1);
		jndiCtx.bind("cfp1", cfp1);
		jndiCtx.bind("cfp2", cfp2);
		jndiCtx.bind("cfw1", cfw1);
		jndiCtx.bind("cfw2", cfw2);
		jndiCtx.close();
		
		AdminModule.disconnect();
		System.out.println("[Setup]\tDone.");
	} 
}