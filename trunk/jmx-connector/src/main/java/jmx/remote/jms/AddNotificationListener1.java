package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.ObjectName;

/**
 * <b>AddNotificationListener1</b>  is the object that is sent by a requestor who wishes to appeal JMX addNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback). 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class AddNotificationListener1 extends AddNotificationListener implements Serializable {
	ObjectName listener;
	public AddNotificationListener1(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) {
		super(name, filter, handback);
		this.listener = listener;
		
	}

}
