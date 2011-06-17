package jmx.remote.jms;
import java.io.Serializable;

import javax.management.ObjectName;


public class GetAttribute implements Serializable {
	ObjectName name;
	String attributes;
	public GetAttribute(ObjectName n, String a){
		name = n;
		attributes = a;
	}

}
