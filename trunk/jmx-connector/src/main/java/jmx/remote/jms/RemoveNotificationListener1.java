package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * <b>RemoveNotificationListener1</b>  is the object that is sent by a requestor who wishes to appeal JMX removeNotificationListener1(ObjectName name,NotificationFilter filter,Object handback).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class RemoveNotificationListener1 extends RemoveNotificationListener implements Serializable {
	NotificationFilter filter;
	public RemoveNotificationListener1(ObjectName name,NotificationFilter filter,Object handback){
		super(name,handback);
		this.filter = filter;
	}

}
