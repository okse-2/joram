package jmx.remote.jms;
import java.io.Serializable;
import javax.management.ObjectName;
import javax.management.Attribute;

/**
 * <b>SetAttribute</b>  is the object that is sent by a requestor who wishes to appeal JMX setAttribute(ObjectName name, Attribute attribute).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class SetAttribute implements Serializable{
	ObjectName name;
	Attribute attribute;
	public SetAttribute(ObjectName n, Attribute at){
		name = n;
		attribute = at;
	}

}
