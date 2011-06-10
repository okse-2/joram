package jmx.remote.jms;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;


public class ObjectNotificationListener implements NotificationListener {
    Session session;
    MessageProducer producer;
    Destination queueNotification;
    NotificationFilter filter;
    Object handback;
	public ObjectNotificationListener(Session session, MessageProducer producer,Destination queueNotification,NotificationFilter filter,Object handback){
		this.session = session;
		this.producer = producer;
		this.queueNotification = queueNotification;
		this.filter = filter;
		this.handback = handback;
		
		
	}
	public ObjectNotificationListener(){
		
	}
	public void handleNotification(Notification notification, Object handback) {
		ObjectMessage messageReponse = null;
		try {
			messageReponse = session.createObjectMessage();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 try {
			 NotificationAndKey notificationAndKey = new NotificationAndKey(notification, handback);
			 messageReponse.setObject((Serializable) notificationAndKey);
			 System.out.println("Noooooooooootificationn and keyyyyyyyyyyyyyyyyyyyyyyysssssssssssssss");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	     try {
			producer.send(queueNotification, messageReponse);
			System.out.println("L'objet listener contenant la notification et le handback a ete envoye");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

     }

}
