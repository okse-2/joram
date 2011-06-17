package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.ObjectName;

public class AddNotificationListener2 implements Serializable {
	ObjectName name;
	ObjectName listener;
	NotificationFilter filter;
	Object handback;
	public AddNotificationListener2(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback){
		this.name = name;
		this.listener = listener;
		this.filter = filter;
		this.handback = handback;
	}

}
