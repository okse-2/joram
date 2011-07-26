package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>UnregisterMbean</b>  is the object that is sent by a requestor who wishes to appeal JMX unregisterMbean(ObjectName n).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class UnregisterMbean implements Serializable {
	ObjectName name;
	public UnregisterMbean(ObjectName name){
		this.name = name;
		
	}
	

}
