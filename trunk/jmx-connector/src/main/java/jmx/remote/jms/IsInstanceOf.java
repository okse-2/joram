package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>IsInstanceOf</b>  is the object that is sent by a requestor who wishes to appeal JMX IsInstanceOf(ObjectName name, String className).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class IsInstanceOf implements Serializable{
	ObjectName name;
	String className;
	public IsInstanceOf(ObjectName name, String className){
		this.name = name;
		this.className = className;
	}

}
