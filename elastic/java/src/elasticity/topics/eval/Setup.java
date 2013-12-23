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
	
	private static final int SERV_NBR = 4;
	
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
		
		// Connecting the administrator (using TcpProxyService port)
		ConnectionFactory cfa = TcpConnectionFactory.create("localhost",16000);
		AdminModule.connect(cfa,"root","root");
		servers = AdminModule.getServers();
		
		Topic[] t = new Topic[SERV_NBR];
		
		Context jndiCtx = new InitialContext();
		for (int i = 0; i < SERV_NBR; i++) {
			User.create("anonymous", "anonymous", i);
			if (i == 0) {
				Properties props = new Properties();
				props.setProperty("root","");
				t[i] = Topic.create(i,"t" + i,"org.objectweb.joram.mom.dest.ElasticTopic",props);
			} else {
				t[i] = Topic.create(i,"t" + i,"org.objectweb.joram.mom.dest.ElasticTopic",null);
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