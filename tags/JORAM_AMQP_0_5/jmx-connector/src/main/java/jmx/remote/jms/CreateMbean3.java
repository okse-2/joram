package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class CreateMbean3 implements Serializable {
	String className;
	ObjectName name;
	Object[] parametres;
	String[] signature;
	public CreateMbean3(String className,ObjectName name,Object[] params,String[] signature){
		this.className = className;
		this.name = name;
		this.parametres = params;
		this.signature = signature;
	}
	

}
