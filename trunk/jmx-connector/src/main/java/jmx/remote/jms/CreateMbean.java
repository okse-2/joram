package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class CreateMbean implements Serializable {
	String className;
	ObjectName name;
	public CreateMbean(String className,ObjectName name){
		this.className = className;
		this.name = name;
		
	}

}
