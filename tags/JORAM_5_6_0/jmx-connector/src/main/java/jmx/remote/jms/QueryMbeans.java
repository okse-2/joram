package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;
import javax.management.QueryExp;

public class QueryMbeans implements Serializable {
	ObjectName name;
	QueryExp query;
	public QueryMbeans(ObjectName name,QueryExp query){
		this.name = name;
		this.query = query;
	}
}
