package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>CreateMBean3</b> is the object that is sent by a requestor who wishes to appeal JMX createMBean(String className, ObjectName name,ObjectName loaderName,Object[] parametres,String[] signature)
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class CreateMBean3 extends CreateMbean implements Serializable {
	ObjectName loaderName;
	Object[] parametres;
	String[] signature;
	public CreateMBean3(String className, ObjectName name,ObjectName loaderName,Object[] parametres,String[] signature) {
		super(className, name);
		this.loaderName = loaderName;
		this.parametres = parametres;
		this.signature = signature;
		// TODO Auto-generated constructor stub
	}
}
