package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.ObjectName;

public class RemoveNotificationListener2 implements Serializable {
	ObjectName name;
	ObjectName objectNameListener;
	NotificationFilter filter;
	Object handback;
	public RemoveNotificationListener2(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback){
		this.name = name;
		this.objectNameListener = listener;
		this.filter = filter;
		this.handback = handback;
	}

}
