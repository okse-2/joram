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

package elasticity.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminWrapper;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import elasticity.interfaces.Service;

/**
 * Joram service, manages Joram workers (queues + consumers).
 * 
 * @author Ahmed El Rheddane
 *
 */
public class JoramService extends Service {
	
	private static final String tcpProxyService = "org.objectweb.joram.mom.proxies.tcp.TcpProxyService";

	//Scripts used by this service.
	private static final String launchServerSh = "/home/ubuntu/joram/bin/launch-vm-server.sh";
	private static final String launchClientSh = "/home/ubuntu/joram/bin/launch-vm-client.sh";

	//Values so that the ports of worker i are base+i.
	private static final int domainPortBase = 16050;
	private static final int serverPortBase = 16000;

	/** Specific admin wrapper for background jobs. */
	private AdminWrapper aw;

	/** A link to the VM service. */
	private AmazonService as;

	/** List of currently used workers' VMs. */
	private LinkedList<String> wVms;
	
	/** List of pre-provisioned VMs. */
	private LinkedList<String> pVms;

	/** Current number of workers. */
	private int size;
	
	/** Size of pre-provisioned VMs. */
	private int ppvm;

	/** Number of servers per VM. */
	private int spvm;


	@Override
	protected void initService(Properties props) throws Exception {
		//Setting specific admin connection
		try {
			InitialContext jndiCtx = new InitialContext();
			ConnectionFactory cf = (ConnectionFactory) jndiCtx.lookup("cfp2");
			jndiCtx.close();
			
			Connection cn = cf.createConnection("root","root");
			cn.start();
			
			aw = new AdminWrapper(cn);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Error while setting admin connection!");
			throw e;
		}
		
		//Initialize the service beneath.
		as = new AmazonService();
		try {
			as.init(props);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,"Error while initializing Amazon Service.");
			throw e;
		}

		//Get the properties.
		size = Integer.valueOf(props.getProperty("init_workers_size"));
		spvm = Integer.valueOf(props.getProperty("servers_per_vm"));
		ppvm = Integer.valueOf(props.getProperty("pre_provision_size"));

		//Set initial machines, on which current workers are working.
		int vmSize = (int) Math.ceil((double) size / (double) spvm);
		wVms = new LinkedList<String>();
		for (int i = 0; i < vmSize; i++) {
			int id = i * spvm + 1;
			for (Server s : aw.getServers()) {
				if (s.getId() == id) {
					wVms.offerLast(s.getHostName());
					break;
				}
			}
		}
		
		//Pre-provisioning, if requested.
		pVms = new LinkedList<String>();
		for (int i = 0; i < ppvm; i++) {
			/*PreProvision pp = new PreProvision();
			pp.start();*/
			String ip = addJoramHost();
			pVms.offerLast(ip);
		}

		logger.log(Level.INFO,"Initialization completed.");
	}

	/**
	 * Deletes a worker (i.e., the queue, we keep Joram servers).
	 * 
	 * @throws Exception
	 */
	public void delWorker() throws Exception {
		logger.log(Level.INFO,"Deleting worker..");
		//AdminModule.stopServer(size);
		//AdminModule.removeServer(size);
		
		deleteQueue(size--);

		if ((size % spvm) == 0) {
			String w = wVms.pollLast();
			String p = pVms.pollLast();
			if (p != null) {
				pVms.offerFirst(w);
			}
		}

		logger.log(Level.INFO,"Deleted worker.");
	}

	/**
	 * Creates a new worker:
	 * - Creates the queue,
	 * - Starts the consumer java client. 
	 * 
	 * @return The new workers' destination.
	 * @throws Exception
	 */
	public Queue addWorker() throws Exception {
		logger.log(Level.INFO,"Adding new worker..");
		String ip;

		if ((size % spvm) != 0) {
			ip = wVms.peekLast();
			logger.log(Level.INFO,"Used spot on existing VM.");
		} else {
			if (ppvm > 0) {
				ip = pVms.pollFirst();
				while (ip == null) {
					//Wait for VM to be provisioned..
					Thread.sleep(1000);
					ip = pVms.pollFirst();
				}
				PreProvision pp = new PreProvision();
				pp.start(); //Replaces the VM we just polled.
				logger.log(Level.INFO,"Used pre-provisioned VM..");
			} else {
				ip = addJoramHost();
			}
			wVms.offerLast(ip);
		}
		
		Queue newQ = createQueue(ip,++size);
		launchWorker(ip,size);
		return newQ;
	}

	/**
	 * Creates a VM and launches spvm Joram servers on it.
	 * 
	 * @return IP of the created Joram host.
	 */
	private String addJoramHost() throws Exception {
		String ip = as.runInstance();
		
		logger.log(Level.INFO,"Created new VM with IP: " + ip);
		
		int s = (wVms.size() + pVms.size()) * spvm + 1;
		for (int i = s; i < s + spvm; i++) {
			launchServer(ip,i);
		}
		
		logger.log(Level.INFO,"Successfully created a VM with its Joram servers..");
		return ip;
	} 
	
	/**
	 * Launches a Joram server.
	 * Called by addJoramHost.
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
			aw.removeServer(num);
		} catch (Exception e) {
			// Server existence is not given.
		}
		aw.addServer(num,ip,"D1",domainPortBase + num,"W" + num,service,serviceArg);
		logger.log(Level.INFO,"Added new server logically..");

		String platformConfig = aw.getConfiguration();
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
	 * Creates and sets a message queue.
	 * Also creates the user anonymous if necessary.
	 * 
	 * @param ip The address of the machine where the Joram server is running.
	 * @param num The ID of the Joram server.
	 * 
	 * @return The created queue.
	 * @throws Exception
	 */
	private Queue createQueue(String ip, int num) throws Exception {
		boolean done = false;
		while (!done) {
			try {
				User.create("anonymous","anonymous",num);
				done = true;
				logger.log(Level.INFO,"Connected to Joram server.");
			} catch (Exception e) {
				logger.log(Level.INFO,"Trying to connect to Joram server...");
			}
		}
		Queue newQ = Queue.create(num,"queue" + num);
		newQ.setFreeReading();
		newQ.setFreeWriting();
		javax.jms.ConnectionFactory newCf =
				TcpConnectionFactory.create(ip,serverPortBase + num);
		InitialContext ictx = new InitialContext();
		ictx.rebind("cfw" + num,newCf);
		ictx.rebind("worker" + num,newQ);
		ictx.close();
		
		logger.log(Level.INFO,"Created Queue successfully on Joram#" + num);
		return newQ;
	}
	
	/**
	 * Deletes a message queue.
	 * 
	 * @param num The ID of Joram server containing the queue.
	 * @throws Exception
	 */
	private void deleteQueue(int num) throws Exception {
		InitialContext ictx = new InitialContext();
		Queue w = (Queue) ictx.lookup("worker" + num);
		ictx.close();
		w.delete();
	}

	/**
	 * Launches a consumer client.
	 * 
	 * @param ip The address of the machine where it should be launched.
	 * @param num The ID of the worker.
	 * @throws Exception
	 */
	private void launchWorker(String ip, int num) throws Exception {
		String command[] = {launchClientSh,ip,Integer.toString(num)};
		Runtime.getRuntime()
		.exec(command);

		logger.log(Level.INFO,"Started client remotely.");
	}

	/**
	 * A class used to pre-provision a VM asynchronously.
	 * It also pre-launches all the Joram servers the VM can hold.
	 */
	private class PreProvision extends Thread {
		public void run() {
			try {
				logger.log(Level.INFO,"Preprovisioning one VM..");
				String ip = addJoramHost();
				pVms.offerLast(ip);
			} catch (Exception e) {
				logger.log(Level.SEVERE,"Error while pre-provisioning a VM!");
				e.printStackTrace(System.out);
			}
		}
	}
}