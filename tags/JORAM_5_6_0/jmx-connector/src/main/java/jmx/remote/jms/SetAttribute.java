package jmx.remote.jms;
import java.io.Serializable;
import javax.management.ObjectName;
import javax.management.Attribute;


public class SetAttribute implements Serializable{
	ObjectName name;
	Attribute attribute;
	public SetAttribute(ObjectName n, Attribute at){
		name = n;
		attribute = at;
	}

}
