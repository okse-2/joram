package jmx.remote.jms;
import java.io.Serializable;
import javax.management.ObjectName;
public class GetMBeanInfo implements Serializable {
	ObjectName name;
	public GetMBeanInfo(ObjectName n){
		name = n;
	}

}
