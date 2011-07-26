package jmx.remote.jms;

import java.io.Serializable;
import javax.management.ObjectName;
import javax.management.QueryExp;
/**
 * <b>QueryName</b>  is the object that is sent by a requestor who wishes to appeal JMX queryName(ObjectName name,QueryExp query).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class QueryName implements Serializable {
	// Appel a la methode queryNames(ObjectName name,QueryExp query)
	ObjectName name;
	QueryExp query;
	
	public QueryName(ObjectName name,QueryExp query){
		this.name = name;
		this.query = query;
		
	}

}
