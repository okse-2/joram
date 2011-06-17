package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class IsInstanceOf implements Serializable{
	ObjectName name;
	String className;
	public IsInstanceOf(ObjectName name, String className){
		this.name = name;
		this.className = className;
	}

}
