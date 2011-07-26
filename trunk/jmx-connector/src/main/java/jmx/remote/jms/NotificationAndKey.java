package jmx.remote.jms;

import java.io.Serializable;

import javax.management.Notification;
/**
 * When a notification is issued by an MBean registered in the MBeanServer we instantiate the object <i>NotificatinoAndKey</i> in the  <i>handleNotification</i> method, of the <i>NotificationListener</i> interface  passing it a parameters,  <i>notification</i> and  </i>key<i>,  and then object is sent to the client so that it can receive the notification.
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */

public class NotificationAndKey implements Serializable {
	Notification notification;
	Object handback;
	public NotificationAndKey(Notification notification,Object handback){
		this.notification = notification;
		this.handback = handback;
		
	}

}
