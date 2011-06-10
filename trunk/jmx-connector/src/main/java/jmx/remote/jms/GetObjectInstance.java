package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class GetObjectInstance implements Serializable {
	ObjectName name;
	public GetObjectInstance(ObjectName name){
		this.name = name;
	}

}
