package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;
/**
 * <b>CreateMBean2</b> is the object that is sent by a requestor who wishes to appeal JMX createMBean(String className, ObjectName name,Object[] parametres,String[] signature)
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class CreateMBean2 extends CreateMbean implements Serializable {
	Object[] parametres;
	String[] signature;
	public CreateMBean2(String className, ObjectName name,Object[] parametres,String[] signature) {
		super(className, name);
		this.parametres = parametres;
		this.signature = signature;
		
		// TODO Auto-generated constructor stub
	}

	
}
