package jmx.remote.jms;

import java.io.Serializable;

import javax.management.AttributeList;
import javax.management.ObjectName;

/**
 * <b>SetAttributes</b>  is the object that is sent by a requestor who wishes to appeal JMX setAttributes(ObjectName name, AttributeList attributes).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class SetAttributes implements Serializable {
	ObjectName name;
	AttributeList attributes;
	public SetAttributes(ObjectName name,AttributeList attributes){
		this.name = name;
		this.attributes = attributes;
	}

}
