package jmx.remote.jms;
import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>GetAttribute</b>  is the object that is sent by a requestor who wishes to appeal JMX getAttribute(ObjectName n, String a).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class GetAttribute implements Serializable {
	ObjectName name;
	String attributes;
	public GetAttribute(ObjectName n, String a){
		name = n;
		attributes = a;
	}

}
