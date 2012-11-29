package elasticity.eval;

import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

/**
 * Sets up the Joram configuration for tests.
 * 
 * @author Ahmed El Rheddane
 */
public class Setup {
	
	public static void main(String args[]) throws Exception {
		System.out.println("[Setup]\tStarted...");
		
		// Connecting the administrator (using TcpProxyService port)
		AdminModule.connect("10.0.0.2",16101,"root","root", 60);
				
		// Creating access for user anonymous on servers
		User.create("anonymous", "anonymous", 101);
		User.create("anonymous", "anonymous", 102);
		User.create("anonymous", "anonymous", 1);
	    
		//Worker
		Queue rq1 = Queue.create(1);
		
		//Producers
		Properties propAQ = new Properties();
		propAQ.setProperty("remoteAgentID",rq1.getName());
		Queue aq1 = Queue.create(101,"org.objectweb.joram.mom.dest.AliasInQueue",propAQ);
		Queue aq2 = Queue.create(102,"org.objectweb.joram.mom.dest.AliasInQueue",propAQ);

		//Setting free access to the destinations
		aq1.setFreeWriting();
		aq2.setFreeWriting();
		rq1.setFreeReading();
		rq1.setFreeWriting();
		
		// Creating the connection factories for connecting to the servers:
		javax.jms.ConnectionFactory cfp1 =
				TcpConnectionFactory.create("10.0.0.2", 16101);
		javax.jms.ConnectionFactory cfp2 =
				TcpConnectionFactory.create("10.0.0.3", 16102);
		javax.jms.ConnectionFactory cfw1 =
				TcpConnectionFactory.create("10.0.0.4", 16001);

		// Binding the objects in JNDI:
		javax.naming.Context jndiCtx = new javax.naming.InitialContext();
		jndiCtx.bind("producer1", aq1);
		jndiCtx.bind("producer2", aq2);
		jndiCtx.bind("worker1", rq1);
		jndiCtx.bind("cfp1", cfp1);
		jndiCtx.bind("cfp2", cfp2);
		jndiCtx.bind("cfw1", cfw1);
		jndiCtx.close();
		
		AdminModule.disconnect();
		System.out.println("[Setup]\tDone.");
	} 
}
