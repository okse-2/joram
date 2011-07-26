package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>IsRegistered</b>  is the object that is sent by a requestor who wishes to appeal JMX isRegistered(ObjectName name).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class IsRegistered implements Serializable {
	ObjectName name;
	public IsRegistered(ObjectName name){
		this.name = name;
	}

}
