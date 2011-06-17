package jmx.remote.jms;

import java.io.Serializable;
import javax.management.ObjectName;
import javax.management.QueryExp;

public class QueryName implements Serializable {
	// Appel a la methode queryNames(ObjectName name,QueryExp query)
	ObjectName name;
	QueryExp query;
	
	public QueryName(ObjectName name,QueryExp query){
		this.name = name;
		this.query = query;
		
	}

}
