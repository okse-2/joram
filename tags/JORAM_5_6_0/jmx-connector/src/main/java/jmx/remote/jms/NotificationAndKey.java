package jmx.remote.jms;

import java.io.Serializable;

import javax.management.Notification;

public class NotificationAndKey implements Serializable {
	Notification notification;
	Object handback;
	public NotificationAndKey(Notification notification,Object handback){
		this.notification = notification;
		this.handback = handback;
		
	}

}
