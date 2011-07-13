package jmx.remote.jms;

import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;



public class AddNotificationListenerStored {
	ObjectName name;
	NotificationListener listener;
	NotificationFilter filter;
	Object handback;
	public AddNotificationListenerStored(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback){
		this.name = name;
		this.listener = listener;
		this.filter = filter;
		this.handback = handback;
	}
	public boolean equals(Object object){
		System.out.println("equals ");
	 if (object instanceof  AddNotificationListenerStored){
	   AddNotificationListenerStored objectANLStored = (AddNotificationListenerStored)object;
		System.out.println("equals " + this.name.equals(objectANLStored.name) + "  " + this.listener.equals(objectANLStored.listener));
	   if(this.filter == null && this.handback == null){
		   
	   
	   if(this.name.equals(objectANLStored.name) && (this.listener.equals(objectANLStored.listener))){// && (this.filter.equals(objectANLStored.filter)) && (this.handback.equals(objectANLStored.handback))){
		   System.out.println("Les deux objet sont egauuuuuuuuuuuuuuuuuuuuuux!!!!!!!");
		   return true;
	   }
	   	}
	   }
	System.out.println("il n'y a pas d'objet egals!!!!!!!!!!!!!!!!!!!!");   
	return false;
	}
	
	 public int hashCode() {
		   // Should compute a specific one.
		   int h = this.name.hashCode()+this.listener.hashCode();
		   System.out.println("HashCode de l'objet : = "+h);
		   return this.name.hashCode()+this.listener.hashCode();
		
		 }

}
