package jmx.remote.jms;

import java.io.Serializable;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
/**
 * <b>AddNotificationListener</b>  is the object that is sent by a requestor who wishes to appeal JMX addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback). 
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class AddNotificationListener implements Serializable {
  public ObjectName name;
  public NotificationListener listener;
  public  NotificationFilter filter;
  Object key;
  public AddNotificationListener(ObjectName name,NotificationFilter filter,Object key){
	  this.name = name;
	  this.filter = filter;
	  this.key = key;
  }

}
