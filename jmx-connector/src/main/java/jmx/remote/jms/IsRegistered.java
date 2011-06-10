package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class IsRegistered implements Serializable {
	ObjectName name;
	public IsRegistered(ObjectName name){
		this.name = name;
	}

}
