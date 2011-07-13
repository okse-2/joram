package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;

public class RemoveNotificationListener1 implements Serializable {
	ObjectName name;
	ObjectName objectNameListener;
	public RemoveNotificationListener1(ObjectName name,ObjectName listener){
		this.name = name;
		this.objectNameListener = listener;
	}

}
