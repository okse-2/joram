package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 * <b>QueryMbeans</b>  is the object that is sent by a requestor who wishes to appeal JMX queryMbeans(ObjectName name,QueryExp query).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class QueryMbeans implements Serializable {
	ObjectName name;
	QueryExp query;
	public QueryMbeans(ObjectName name,QueryExp query){
		this.name = name;
		this.query = query;
	}
}
