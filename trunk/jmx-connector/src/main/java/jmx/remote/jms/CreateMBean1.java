package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;
/**
 * <b>CreateMBean1</b> is the object that is sent by a requestor who wishes to appeal JMX createMBean(String className, ObjectName name,ObjectName loaderName)
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class CreateMBean1 extends CreateMbean implements Serializable  {
	ObjectName loaderName;

	public CreateMBean1(String className, ObjectName name,ObjectName loaderName) {
		super(className, name);
		this.loaderName = loaderName;
		// TODO Auto-generated constructor stub
	}

	

}
