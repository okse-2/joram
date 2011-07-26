package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.ObjectName;

/**
 * <b>RemoveNotificationListener3</b>  is the object that is sent by a requestor who wishes to appeal JMX removeNotificationListener3(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class RemoveNotificationListener3 extends RemoveNotificationListener implements Serializable {
	ObjectName listener;
	NotificationFilter filter;
	
	public RemoveNotificationListener3(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback){
		super(name,handback);
		this.listener = listener;
		this.filter = filter;
	}

}
