package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class CreateMbean2 implements Serializable {
	String className;
	ObjectName name;
	ObjectName loaderName;
	public CreateMbean2(String className,ObjectName name,ObjectName loaderName){
		this.className = className;
		this.name = name;
		this.loaderName = loaderName;
		
	}

}
