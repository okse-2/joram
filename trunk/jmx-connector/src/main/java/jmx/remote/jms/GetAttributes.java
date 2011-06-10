package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class GetAttributes implements Serializable {
	// Appel a la methode getAttributes(ObjectName name,String[] attributes) 
	ObjectName name;
	String[] attributes;
	public GetAttributes(ObjectName name, String[] attributes){
		this.name = name;
		this.attributes = attributes;
	}

}
