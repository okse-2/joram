package jmx.remote.jms;

import java.io.Serializable;

import javax.management.ObjectName;
/**
 * <b>RemoveNotificationListener2</b>  is the object that is sent by a requestor who wishes to appeal JMX removeNotificationListener2(ObjectName name,ObjectName listener,Object handback).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class RemoveNotificationListener2 extends RemoveNotificationListener implements Serializable {
	ObjectName listener;
	public RemoveNotificationListener2(ObjectName name,ObjectName listener,Object handback){
		super(name,handback);
		this.listener = listener;
	}

}
