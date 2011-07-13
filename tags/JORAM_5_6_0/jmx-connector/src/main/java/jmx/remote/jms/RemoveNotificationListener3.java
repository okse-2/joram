package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationListener;
import javax.management.ObjectName;

public class RemoveNotificationListener3 implements Serializable {
	ObjectName name;
	NotificationListener listener;
	Object handback;
	
	public RemoveNotificationListener3(ObjectName name,Object handback){
		this.name = name;
		this.handback = handback;
				
	}

}
