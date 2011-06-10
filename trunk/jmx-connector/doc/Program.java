package jmx.remote.jms;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ConnectException;

import javax.jms.JMSException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.admin.AdminException;


public class Program {
	private static MBeanServer mbs = null;
	public static void main(String [] args) throws MalformedObjectNameException, NullPointerException, IOException{
		A objetA = new A();
	    mbs = ManagementFactory.getPlatformMBeanServer();
	    System.out.println(mbs.toString());
	    ObjectName name = new ObjectName("SimpleAgent:name=A");
	    try {
			mbs.registerMBean(objetA, name);
			System.out.println("-->Enregistrement de la classe A dans le MBS");
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/**Lancement du JMSConnecteur*/
		/*try {
			JMSConnector jmsConnector = new JMSConnector();
			System.out.println("--->Lancement du JMSConnecteurServeur");
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdminException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

}
