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

package elasticity.topics.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;

import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.Topic;

import elasticity.interfaces.Service;
import elasticity.services.AmazonService;

/**
 * Joram service, manages Joram topics.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class JoramService extends Service {

	private static final String tcpProxyService = "org.objectweb.joram.mom.proxies.tcp.TcpProxyService";

	//Scripts used by this service.
	private static final String launchServerSh = "/home/ubuntu/joram/bin/launch-vm-server.sh";

	//Values so that the ports of worker i are base+i.
	private static final int domainPortBase = 16050;
	private static final int serverPortBase = 16000;

	/** Specific admin wrapper for background jobs. */
	//private AdminWrapper aw;

	/** A link to the VM service. */
	private AmazonService as;

	/** List of currently used topics' VMs. */
	private LinkedList<String> vms;

	/** The 'elastic' topic. */
	private Topic et;

	@Override
	protected void initService(Properties props) throws Exception {
		//Initialize the service beneath.
		as = new AmazonService();
		try {
			as.init(props);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,"Error while initializing Amazon Service.");
			throw e;
		}

		//Get the elastic topic.
		InitialContext jndiCtx = new InitialContext();
		et = (Topic) jndiCtx.lookup("t0");
		jndiCtx.close();


		//Get the properties.
		int init = Integer.valueOf(props.getProperty("init_topics"));

		//Set initial machines, on which current topics are running.
		vms = new LinkedList<String>();
		for (int i = 1; i <= init; i++) {
			for (Server s : AdminModule.getServers()) {
				if (s.getId() == i) {
					vms.addLast(s.getHostName());
					break;
				}
			}
		}

		logger.log(Level.INFO,"Initialization completed.");
	}

	/**
	 * Creates a new topic on a new VM
	 * and links it to t0.
	 * 
	 * @throws Exception
	 */
	public Topic addTopic() throws Exception {
		String ip = as.runInstance();
		logger.log(Level.FINE,"Created new VM with IP: " + ip);
		
		vms.addLast(ip);
		
		launchServer(ip,vms.size());
		Topic t = createTopic(vms.size());
		
		String param = t.getName() + ";";
		param = param + ip + ";";
		param = param + (serverPortBase + vms.size());
		et.scale(1, param);

		logger.log(Level.INFO,"Added new topic successfully.");
		return t;
	}

	/**
	 * Launches a Joram server.
	 * 
	 * @param ip The address of the machine where the server is to be created.
	 * @param num The number of the server to be created.
	 * @throws Exception
	 */
	private void launchServer(String ip, int num) throws Exception {
		//AdminModule.connect("root", "root", 60);
		String[] service = new String[1];
		service[0] = tcpProxyService;
		String[] serviceArg = new String[1];
		serviceArg[0] = Integer.toString(serverPortBase + num);

		try {
			AdminModule.removeServer(num);
		} catch (Exception e) {
			// Server existence is not given.
		}
		AdminModule.addServer(num,ip,"D0",domainPortBase + num,"" + num,service,serviceArg);
		logger.log(Level.INFO,"Added new server logically..");

		String platformConfig = AdminModule.getConfiguration();
		File platformConfigFile = new File("new_a3servers.xml");
		FileOutputStream fos = new FileOutputStream(platformConfigFile);
		PrintWriter pw = new PrintWriter(fos);
		pw.println(platformConfig);
		pw.flush();
		pw.close();
		fos.close();

		// Add new server
		String[] command = {launchServerSh,ip,Integer.toString(num)};
		Runtime.getRuntime()
		.exec(command);

		logger.log(Level.INFO,"Started server #" + num + " remotely on " + ip);
	}

	/**
	 * Creates and sets a topic.
	 * Also creates the user anonymous if necessary.
	 * 
	 * @param num The ID of the Joram server.
	 * 
	 * @return The created topic.
	 * @throws Exception
	 */
	private Topic createTopic(int num) throws Exception {
		boolean done = false;
		while (!done) {
			try {
				User.create("anonymous","anonymous",num);
				done = true;
				logger.log(Level.FINE,"Connected to Joram server.");
			} catch (Exception e) {
				logger.log(Level.FINE,"Trying to connect to Joram server...");
			}
		}

		Topic t = Topic.create(num,"topic" + num,"org.objectweb.joram.mom.dest.ElasticTopic",null);
		t.setFreeReading();
		t.setFreeWriting();

		logger.log(Level.INFO,"Created Topic successfully on Joram#" + num);
		return t;
	}

	/**
	 * Removes last added topic along with its Joram server and VM.
	 * Does NOT handle topics' subscribers.
	 */
	public void removeTopic() throws Exception {
		try {
			et.scale(-1, "");
			AdminModule.removeServer(vms.size());
			String ip = vms.removeLast();
			as.terminateInstance(ip);
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while removing Joram server!");
			throw e;
		}

		logger.log(Level.INFO,"Removed topic successfully.");
	}
}	
