package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>GetAttributes</b>  is the object that is sent by a requestor who wishes to appeal JMX getAttributes(ObjectName name, String[] attributes).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class GetAttributes implements Serializable {
	// Appel a la methode getAttributes(ObjectName name,String[] attributes) 
	ObjectName name;
	String[] attributes;
	public GetAttributes(ObjectName name, String[] attributes){
		this.name = name;
		this.attributes = attributes;
	}

}
