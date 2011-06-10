package jmx.remote.jms;

import java.io.Serializable;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

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
