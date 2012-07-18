package elasticity.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * A class used to pre-provision vms in parallel.
 * 
 * @author Ahmed El Rheddane
 */
class PreProvision extends Thread {
	
	private LinkedList<String> preVms;
	
	public PreProvision(LinkedList<String> preVms) {
		this.preVms = preVms;
	}
	
	public void run() {
		String vm = JoramAmazonVMs.runInstance();
		preVms.offer(vm);
	}
}

/**
 * Just for tests.
 * 
 * @author Ahmed El Rheddane
 *
 */
public class JoramManager {

	private static Logger logger;
	
	private static final String propFile = "elasticity.properties";

	private static final int serverPortBase = 16010;
	private static final int domainPortBase = 16301;
	private static final String tcpProxyService = "org.objectweb.joram.mom.proxies.tcp.TcpProxyService";

	private static final String serverSh = "/root/joram/bin/launch-vm-server.sh";
	private static final String clientSh = "/root/joram/bin/launch-vm-client.sh";
	
	private static final int serverPerVm = 2;
	
	// Related to the number of sending servers..
	private static final int offset = 1;
	
	private static LinkedList<String> vms;
	private static int size;
	
	private static LinkedList<String> preVms;

	public static void init(int preSize, Logger superLogger) throws Exception {
		
		logger = superLogger;
		JoramAmazonVMs.logger = superLogger;
		
		Properties props = new Properties();
		InputStream reader = new FileInputStream(propFile);
		props.load(reader);
		reader.close();

		JoramAmazonVMs.awsImageId = props.getProperty("image_id");
		
		vms = new LinkedList<String>();
		vms.push("10.0.0.4");
		size = 1;

		preVms = new LinkedList<String>();
		for (int i = 0; i < preSize; i++) {
			PreProvision pp = new PreProvision(preVms);
			pp.start();
		}
		
		logger.log(Level.INFO,"[JM] Done pre-provisioning " + preSize + " vms.");
	}
	
	public static int getSize() {
		return size;
	}

	public static void delWorker() throws Exception {
		int servNum = size + offset;
		AdminModule.stopServer(servNum);
		AdminModule.removeServer(servNum);
		size--;
	
		if ((size % serverPerVm) == 0) {
			String vm =vms.pop();
			//To be suppressed effectively (needed for the logs though)
		}
		logger.log(Level.INFO,"[JM] Deleted worker.");
	}

	public static Queue addWorker() throws Exception {
		String vm;
		
		if ((size % serverPerVm) == 0) {
			if (preVms.isEmpty()) {
				vm = JoramAmazonVMs.runInstance();
				logger.log(Level.INFO,"[JM] Runned new VM, ip: " + vm);
			} else {
				vm = preVms.poll();
				PreProvision pp = new PreProvision(preVms);
				pp.start(); //Replaces the vm we just polled
				logger.log(Level.INFO,"[JM] Used pre-provisionned VM.");
			}
			vms.push(vm);
		} else {
			 vm = vms.peek();
			 logger.log(Level.INFO,"[JM] Used spot on existing VM.");
		}
		
		size++;
		return addServer(vm,size + offset);
	}
	
	private static Queue addServer(String servIp, int servNum) throws Exception {
		//AdminModule.connect("root", "root", 60);
		String[] service = new String[1];
		service[0]=tcpProxyService;
		String[] serviceArg = new String[1];
		serviceArg[0]=Integer.toString(serverPortBase+servNum);
		AdminModule.addServer(servNum,servIp,"D1",domainPortBase+servNum,"S"+servNum,service,serviceArg);
		
		logger.log(Level.INFO,"[JM] Added new server logically..");

		String platformConfig = AdminModule.getConfiguration();		
		File platformConfigFile = new File("new_a3servers.xml");
		FileOutputStream fos = new FileOutputStream(platformConfigFile);
		PrintWriter pw = new PrintWriter(fos);
		pw.println(platformConfig);
		pw.flush();
		pw.close();
		fos.close();

		// Add new server
		String[] command = {serverSh,servIp,Integer.toString(servNum)};
		Runtime.getRuntime()
		.exec(command);
		//Thread.sleep(5000);
		
		logger.log(Level.INFO,"[JM] Started new server remotely..");

		User.create("anonymous", "anonymous", servNum);
		Queue newRq = Queue.create(servNum);
		newRq.setFreeReading();
		newRq.setFreeWriting();
		javax.jms.ConnectionFactory newCf =
				TcpConnectionFactory.create(servIp,serverPortBase+servNum);

		InitialContext ictx = new InitialContext();
		ictx.rebind("cf"+servNum,newCf);
		ictx.rebind("remote"+servNum,newRq);
		ictx.close();
		
		logger.log(Level.INFO,"[JM] Created new worker on new server..");
		
		command[0] = clientSh;
		Runtime.getRuntime()
		.exec(command);
		//Thread.sleep(5000);
		
		logger.log(Level.INFO,"[JM] Started client remotely..");
		
		return newRq;
	}
}
