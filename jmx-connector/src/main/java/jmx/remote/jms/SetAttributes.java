package jmx.remote.jms;

import java.io.Serializable;

import javax.management.AttributeList;
import javax.management.ObjectName;

public class SetAttributes implements Serializable {
	ObjectName name;
	AttributeList attributes;
	public SetAttributes(ObjectName name,AttributeList attributes){
		this.name = name;
		this.attributes = attributes;
	}

}
