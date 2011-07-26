package jmx.remote.jms;
import java.io.Serializable;

import javax.management.ObjectName;

/**
 * <b>Invoke</b>  is the object that is sent by a requestor who wishes to appeal JMX invoke(ObjectName name,String operationName,Object[] parametres,String[] signature).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */

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
