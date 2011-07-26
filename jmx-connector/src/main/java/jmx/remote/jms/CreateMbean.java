package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;
/**
 * <b>CreateMBean</b> is the object that is sent by a requestor who wishes to appeal JMX createMBean(String className,ObjectName name)
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class CreateMbean implements Serializable {
	String className;
	ObjectName name;
	public CreateMbean(String className,ObjectName name){
		this.className = className;
		this.name = name;
		
	}

}
