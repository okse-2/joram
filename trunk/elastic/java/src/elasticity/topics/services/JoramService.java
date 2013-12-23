package elasticity.topics.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.admin.AdminWrapper;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
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
	private AdminWrapper aw;

	/** A link to the VM service. */
	private AmazonService as;

	/** List of currently used topics' VMs. */
	private LinkedList<String> vms;
	
	private Topic t0;
		
	/** Initial number of topics */
	private int init;

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
		
		//Get t0 (the "alias" topic)
		InitialContext jndiCtx = new InitialContext();
		t0 = (Topic) jndiCtx.lookup("t0");
		jndiCtx.close();
		

		//Get the properties.
		init = Integer.valueOf(props.getProperty("init_topics"));

		//Set initial machines, on which current topics are running.
		vms = new LinkedList<String>();
		for (int i = 1; i <= init; i++) {
			for (Server s : aw.getServers()) {
				if (s.getId() == i) {
					vms.offerLast(s.getHostName());
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
	public void addTopic() throws Exception {
		String ip = as.runInstance();
		logger.log(Level.INFO,"Created new VM with IP: " + ip);
		
		vms.offerLast(ip);
		launchServer(ip,vms.size());
		
		createTopic(vms.size());
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
	private Topic createTopic(int num) throws Exception {
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
		
		Topic newT = Topic.create(num,"topic" + num);
		newT.setFreeReading();
		newT.setFreeWriting();
		t0.setParent(newT);
		
		logger.log(Level.INFO,"Created Topic successfully on Joram#" + num);
		return newT;
	}
}	
