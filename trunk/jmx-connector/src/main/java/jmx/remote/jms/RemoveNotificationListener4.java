package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

public class RemoveNotificationListener4 implements Serializable {
	ObjectName name;
	NotificationFilter filter;
	Object handback;
	public RemoveNotificationListener4(ObjectName name,NotificationFilter filter,Object handback){
		this.name = name;
		this.filter = filter;
		this.handback = handback;
	}

}
