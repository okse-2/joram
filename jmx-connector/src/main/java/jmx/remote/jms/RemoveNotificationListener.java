package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * <b>RemoveNotificationListener</b>  is the object that is sent by a requestor who wishes to appeal JMX removeNotificationListener(ObjectName name,Object handback).
 * 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class RemoveNotificationListener  implements Serializable {
	ObjectName name;
	Object handback;
	
	public RemoveNotificationListener(ObjectName name,Object handback){
		this.name = name;
		this.handback = handback;
				
	}

}
