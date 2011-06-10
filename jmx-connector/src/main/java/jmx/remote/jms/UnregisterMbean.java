package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class UnregisterMbean implements Serializable {
	ObjectName name;
	public UnregisterMbean(ObjectName name){
		this.name = name;
		
	}
	

}
