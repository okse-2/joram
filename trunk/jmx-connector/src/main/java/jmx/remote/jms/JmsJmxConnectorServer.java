package jmx.remote.jms;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.jms.JMSException;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AdminException;


public class JmsJmxConnectorServer extends JMXConnectorServer {
	private JMXServiceURL urlServer;
	private final Map envServer;
	private URI jmsURL;
	private boolean stopped = true;

    public JmsJmxConnectorServer(JMXServiceURL url,Map environment,MBeanServer server) throws IOException{
    	          //super(server);
    	          this.urlServer = url;
    	          this.envServer = environment;
    	         // this.jmsURL = JmsJmxConnectorSupport.getProviderURL(url);
    	         //set any props in the url
    	         // JmsJmxConnectorSupport.populateProperties(this, jmsURL);
    }
    	  

	
	public void start() throws IOException {
		// TODO Auto-generated method stub
		stopped = false;
		try {
			JMSConnector jmsConnector = new JMSConnector();
			System.out.println("Instantiation de la classe JmsConnector");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdminException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


		
		
	}

	public void stop() throws IOException {
		// TODO Auto-generated method stub
		if(!stopped){
			stopped = true;
			//.....
		}
		
	}

	public boolean isActive() {
		// TODO Auto-generated method stub
		return !stopped;
	}

	public JMXServiceURL getAddress() {
		// TODO Auto-generated method stub
		return urlServer;
	}

	public Map<String, ?> getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

}
