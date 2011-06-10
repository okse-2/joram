package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class CreateMbean4 implements Serializable {
	String className;
	ObjectName name;
	ObjectName loaderName;
	Object[] params;
	String[] signature;
	public CreateMbean4(String className,ObjectName name,ObjectName loaderName,Object[] params,String[] signature){
		this.className = className;
		this.name = name;
		this.loaderName = loaderName;
		this.signature = signature;
	}

}
