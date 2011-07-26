package jmx.remote.jms;

import java.lang.management.ManagementFactory;

import javax.jms.JMSException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
/**
 * In the Class <b>MBeansToMBeanServer</b>, are registered the MBeans in the MBeanServer.
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class MBeansToMBeanServer {
	public static void main(String[] args) {
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		//Instantiation of MBeans
		A objetA = new A();
		BroadcastingUser broadcastingUser = new BroadcastingUser("john");
		
		try {
			MBeansToMBeanServer mBeansToMBeanServer =new MBeansToMBeanServer();
			mBeansToMBeanServer.registerMBeanToMBeanServer(objetA, mbs);
			mBeansToMBeanServer.registerMBeanToMBeanServer(broadcastingUser, mbs);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    
}
	
	/**
	 * <b>registerMBeanToMBeanServer</b> this method registers the  MBean object  in the MBeanServer with an ObjectName.
	 * 
	 * @param Object 
	 * @param MBeanServer
	 * @return ObjectName      
	 * @throws JMSException
	 */
	
	public ObjectName registerMBeanToMBeanServer(Object objectMBean,MBeanServer mbs) throws MalformedObjectNameException, NullPointerException{
		ObjectName name;
		name = new ObjectName("SimpleAgent:name="+objectMBean.getClass().getName());
		try {
			mbs.registerMBean(objectMBean, name);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("-->Registration of class: "+"'"+ objectMBean.getClass().getName()+"'"+" in the MBeanServeur: "+"'"+mbs.getClass().getName()+"'");
		return name;
		
	}
}

