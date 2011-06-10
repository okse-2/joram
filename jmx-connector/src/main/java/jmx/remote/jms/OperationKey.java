package jmx.remote.jms;

import java.io.Serializable;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * This class describes a Key for Operation object with a relative path, key for
 * other object is the name of the object (String).
 */
public class OperationKey implements Serializable {

ObjectName name;
NotificationListener listener;
NotificationFilter filter;
Object handback;
	public OperationKey(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback){
		this.name = name;
		this.listener = listener;
		this.filter = filter;
		this.handback = handback;
	}
 

 /**
  * Returns a hash code value for the object.
  * @see java.lang.Object#hashCode()
  */
 public int hashCode() {
   // Should compute a specific one.
   return name.hashCode();
 }

 /**
  * Indicates whether some other object is "equal to" this one.
  * @see java.lang.Object#equals(java.lang.Object)
  */
 public boolean equals(Object obj) {
   if (obj instanceof OperationKey) {
     OperationKey opk = (OperationKey)obj;
     if(opk.name.equals(name) && opk.listener.equals(listener) && opk.filter.equals(filter) && opk.handback.equals(handback)){
        return true;
     }  
     else 
    	 return false;
     
 }
   return false;
}
}
