package jmx.remote.jms;
import java.io.Serializable;

import javax.management.ObjectName;

public class Invoke implements Serializable {
	ObjectName name;
	String operationName;
	Object[] parametres;
	String[] signature; 
	public Invoke(ObjectName n,String opN,Object[] p,String[] s){
		name = n;
		operationName = opN;
		parametres = p;
		signature = s;
		
	}
}
