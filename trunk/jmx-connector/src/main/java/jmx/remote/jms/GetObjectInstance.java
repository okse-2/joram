package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>GetObjectInstance</b>  is the object that is sent by a requestor who wishes to appeal JMX getObjectInstance(ObjectName name).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */

public class GetObjectInstance implements Serializable {
	ObjectName name;
	public GetObjectInstance(ObjectName name){
		this.name = name;
	}

}
