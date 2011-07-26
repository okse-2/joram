package jmx.remote.jms;
import java.io.Serializable;
import javax.management.ObjectName;

/**
 * <b>GetMBeanInfo</b>  is the object that is sent by a requestor who wishes to appeal JMX getMBeanInfo(ObjectName n).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */

public class GetMBeanInfo implements Serializable {
	ObjectName name;
	public GetMBeanInfo(ObjectName n){
		name = n;
	}

}
