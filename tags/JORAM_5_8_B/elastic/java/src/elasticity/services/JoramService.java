package elasticity.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;

import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;


import elasticity.interfaces.Service;

/**
 * Joram service, manages Joram workers.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class JoramService extends Service {
	private static final String tcpProxyService = "org.objectweb.joram.mom.proxies.tcp.TcpProxyService";
	
	//Scripts used by this service.
	private static final String launchServer = "/root/joram/bin/launch-vm-server.sh";
	private static final String launchClient = "/root/joram/bin/launch-vm-client.sh";
	
	//Values so that the ports of worker i are base+i.
	private static final int domainPortBase = 16050;
	private static final int serverPortBase = 16000;
	
	
	/** A link to the VM service. */
	private AmazonService as;
	
	/** List of pre-rpovisionned VMs. */
	private LinkedList<String> preVms;
	
	/** List of currently used VMs. */
	private LinkedList<String> vms;
	
	/** Current number of workers. */
	private int size;
	
	/** Number of servers per VM. */
	private int spvm;
	

	@Override
	protected void initService(Properties props) throws Exception {
		//Initializes the service beneath.
		as = new AmazonService();
		try {
			as.init(props);
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE,"Error while initializing Amazon Service.");
			throw e;
		}
		
		//Get the properties..
		int ppSize = Integer.valueOf(props.getProperty("pre_provision_size"));
		spvm = Integer.valueOf(props.getProperty("servers_per_vm"));
		
		//Pre-provisioning, if requested.
		preVms = new LinkedList<String>();
		for (int i = 0; i < ppSize; i++) {
			PreProvision pp = new PreProvision(as,preVms);
			pp.start();
		}
		
		//Hard-coded setup of initial configuration. 
		vms = new LinkedList<String>();
		vms.push("10.0.0.4");
		size = 1;
		
		logger.log(Level.INFO,"Initialization completed.");
	}

	/**
	 * Deletes a worker (a consumer along with its queue and Joram server).
	 * 
	 * @throws Exception
	 */
	public void delWorker() throws Exception {
		logger.log(Level.INFO,"Deleting worker..");
		AdminModule.stopServer(size);
		AdminModule.removeServer(size);
		
		size--;
		if ((size % spvm) == 0) {
			String vm =vms.pop();
			//To be suppressed effectively (needed for the logs though)
		}
		
		logger.log(Level.INFO,"Deleted worker.");
	}

	/**
	 * Creates a new worker (i.e. Joram server, queue and consumer).
	 * 
	 * @return The new workers' destination.
	 * @throws Exception
	 */
	public Queue addWorker() throws Exception {
		logger.log(Level.INFO,"Adding new worker..");
		String vm;
		
		if ((size % spvm) == 0) {
			if (preVms.isEmpty()) {
				vm = as.runInstance();
				logger.log(Level.INFO,"Runned new VM with IP: " + vm + "..");
			} else {
				vm = preVms.poll();
				PreProvision pp = new PreProvision(as,preVms);
				pp.start(); //Replaces the VM we just polled
				logger.log(Level.INFO,"Used pre-provisionned VM..");
			}
			vms.push(vm);
		} else {
			 vm = vms.peek();
			 logger.log(Level.INFO,"Used spot on existing VM.");
		}
		
		return addServer(vm,++size);
	}
	
	/**
	 * Creates the worker on the given machine, with the given number.
	 * Called by addWoker.
	 * 
	 * @param servIp The address of the machine where the server is to be created.
	 * @param servNum the number of the server to be created.
	 * @return The new worker's destination.
	 * @throws Exception
	 */
	private Queue addServer(String servIp, int servNum) throws Exception {
		//AdminModule.connect("root", "root", 60);
		String[] service = new String[1];
		service[0]=tcpProxyService;
		String[] serviceArg = new String[1];
		serviceArg[0]=Integer.toString(serverPortBase+servNum);
		AdminModule.addServer(servNum,servIp,"D1",domainPortBase+servNum,"W"+servNum,service,serviceArg);
		
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
		String[] command = {launchServer,servIp,Integer.toString(servNum)};
		Runtime.getRuntime()
		.exec(command);
		//Thread.sleep(5000);
		
		logger.log(Level.INFO,"Started new server remotely..");

		User.create("anonymous", "anonymous", servNum);
		Queue newRq = Queue.create(servNum);
		newRq.setFreeReading();
		newRq.setFreeWriting();
		javax.jms.ConnectionFactory newCf =
				TcpConnectionFactory.create(servIp,serverPortBase+servNum);

		InitialContext ictx = new InitialContext();
		ictx.rebind("cfw"+servNum,newCf);
		ictx.rebind("worker"+servNum,newRq);
		ictx.close();
		
		logger.log(Level.INFO,"Created new worker on new server..");
		
		command[0] = launchClient;
		Runtime.getRuntime()
		.exec(command);
		//Thread.sleep(5000);
		
		logger.log(Level.INFO,"Started client remotely.");
		return newRq;
	}
}

/**
 * A class used to pre-provision vms in parallel.
 */
class PreProvision extends Thread {
	
	private LinkedList<String> preVms;
	private AmazonService as;
	
	public PreProvision(AmazonService as, LinkedList<String> preVms) {
		this.preVms = preVms;
		this.as = as;
	}
	
	public void run() {
		try {
		String vm = as.runInstance();
		preVms.offer(vm);
		} catch (Exception e) {
			//Well, this is awkward...
		}
	}
}