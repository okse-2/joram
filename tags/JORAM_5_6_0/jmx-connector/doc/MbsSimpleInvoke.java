package jmx.remote.jms;
import javax.management.*;

import java.lang.management.*;

public class MbsSimpleInvoke {
	ObjectName name;
	String operationName;
	Object[] params = new Object[0];
	//params[0] = new Integer(cardNum);
	String[] sig = new String[0];
	//sig[0] = "java.lang.Integer";


	   MBeanServer mbs = null;

	   public MbsSimpleInvoke() {

	      // Get the platform MBeanServer
	       mbs = ManagementFactory.getPlatformMBeanServer();

	      // Unique identification of MBeans
	      A ABean = new A();
	      ObjectName AName = null;

	      try {
	         // Uniquely identify the MBeans and register them with the platform MBeanServer 
	         AName = new ObjectName("SimpleAgent:name=A");
	         mbs.registerMBean(ABean, AName); //On enregistre l'objet Bean de la classe A dans le MBeanServer
	      } catch(Exception e) {
	         e.printStackTrace();
	      }
	   }

	   // Utility method: so that the application continues to run
	   private static void waitForEnterPressed() {
	      try {
	         System.out.println("Press  to quit the MbeanServer...");
	         System.in.read();
	      } catch (Exception e) {
	         e.printStackTrace();
	      }
	    }
	   
	   public void invoke() throws MalformedObjectNameException, NullPointerException, InstanceNotFoundException, ReflectionException, MBeanException{
		   name = new ObjectName("SimpleAgent:name=A");
		   operationName = "affiche";
		   mbs.invoke(name, operationName, params, sig);
		   
	   }
	   
	   public static void main(String [] args) throws MalformedObjectNameException, InstanceNotFoundException, NullPointerException, ReflectionException, MBeanException{
		   MbsSimpleInvoke mbsSimpleInvoke = new MbsSimpleInvoke();
		   mbsSimpleInvoke.invoke();
	   }

	

}
