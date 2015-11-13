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

package elasticity.topics.eval;


import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Class to setup experiments on tree topics.
 * 
 * @author Ahmed El Rheddane
 *
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
		System.out.println("[Setup] Started...");

		int size = Integer.parseInt(args[0]) + 1;

		// Connecting the administrator (using TcpProxyService port)
		ConnectionFactory cfa = TcpConnectionFactory.create("localhost",16000);
		AdminModule.connect(cfa,"root","root");
		servers = AdminModule.getServers();

		Topic[] t = new Topic[size];

		Context jndiCtx = new InitialContext();
		for (int i = 0; i < size; i++) {
			User.create("anonymous", "anonymous", i);
			if (i == 0) {
				Properties props = new Properties();
				props.setProperty("root","");
				t[i] = Topic.create(i,"t" + i,"org.objectweb.joram.mom.dest.ElasticTopic", props);
			} else {
				t[i] = Topic.create(i,"t" + i,"org.objectweb.joram.mom.dest.ElasticTopic", null);
				t[0].scale(1,t[i].getName() + ";" + getServerAddress(i) + ";" + (16000 + i));
			}

			t[i].setFreeWriting();
			t[i].setFreeReading();

			ConnectionFactory cf = TcpConnectionFactory.create(getServerAddress(i), 16000 + i);
			jndiCtx.bind("t" + i, t[i]);
			jndiCtx.bind("cf" + i, cf);

			System.out.println("[Setup] Topic created..");
		}

		jndiCtx.close();
		AdminModule.disconnect();

		System.out.println("[Setup] Done.");
	}
}