package jmx.remote.jms;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Scanner;

import javax.jms.*;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JFrame;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.TemporaryQueue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;

/**
 * <b>ClientJMS est la classe représentant un client.</b>
 * <p>
 * <ul>
 * <li>Destination : Le ClientJMS construit sa propre destination qui est une queue Temporaire, il la construit a partir de l'objet session.</li>
 * </ul>
 * <ul>
 * <li>Propirété  du Message Envoyé : Dans cette version le Client JMS initialise le champ Propriété de chaque message avant de l'envoyer vers le connecteur en mettant comme propriété le nom du connecteur destinataire afin que le connecteur puisse verifier s'il est bien le destinataire.
 * 								     Il fait aussi un setJMSReplyTo sur le message à envoyé en mettant en paramètre sa destination, pour que le connecteur destiné a recevoir le message lui reponde à cette destination.</li>
 * </ul>
 * </p>
 * @author Djamel-Eddine Boumchedda
 * @version 1.3
 *
 */

public class ClientJMS implements MessageListener{
	private static ConnectionFactory connectionFactory;
	private static Topic topic;
	private static Queue QReponse,QRequete;
	static String[] signatureMethode;
	static Object[] paramMethode;
	static String operationName,objectName;
	Session session = null;
	Session session2;
	ObjectMessage message,messageRecu;
	ObjectMessage messageNotificationRecu = null;
	MessageProducer producer,producerNotification;
	MessageConsumer consumer,consumerNotification;
	Destination dest;
	Queue queue;
	TemporaryQueue queueTemporaire;
	public Connection connection; 
	AttributeList attributesList;
	Queue queueNotification;
	Boolean notificationRecu = false;


	
	public ClientJMS() throws NamingException, JMSException, AdminException, IOException{
		//Récupération du contexte JNDI
		Context jndiContext = new InitialContext();
		//Recherche des objets administrés
		ConnectionFactory connectionFactory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");
		//Queue qtoto = (Queue)jndiContext.lookup("Qtoto");
		//le client crée sa destination dans le serveur
		//AdminModule.connect(connectionFactory, "root", "root");
		
		//queue = Queue.create("QX");
		//queue.setFreeReading();
		//queue.setFreeWriting();
		
		//Queue QRequete = (Queue)jndiContext.lookup("QRequete");
		//Queue QReponse = (Queue)jndiContext.lookup("QReponse");
		//Topic topic = (Topic)jndiContext.lookup("Topic");
		jndiContext.close();
		//Création des artéfacts nécessaires pour se connecter à la file et au sujet
		connection = connectionFactory.createConnection();
		session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
		session2 = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
        //Le client crée sa destination temporaire
		queueTemporaire = (TemporaryQueue) session.createTemporaryQueue();
		Queue qtoto = (Queue) session.createQueue("Qtoto");
		queueNotification = (Queue) session.createQueue("QNotification");
		consumer = session.createConsumer(queueTemporaire);
		producer = session.createProducer(qtoto);
		consumerNotification = session2.createConsumer(queueNotification);
		producerNotification = session.createProducer(queueNotification);
		consumerNotification.setMessageListener(this);
		connection.start();
	}
	
	
	
	
	public void onMessage(Message message){
		messageNotificationRecu = (ObjectMessage) message;
		//HashMap hashTableNotificationContext =  MBeanServerConnectionDelegate.hashTableNotificationContext;
		HashMap hashTableNotificationListener =  MBeanServerConnectionDelegate.hashTableNotificationListener;
		//HashMap myNotificationListener = JMSConnector.myNotificationListener;
		 NotificationAndKey notificationAndKey = null;
		try {
			notificationAndKey = (NotificationAndKey) ((ObjectMessage) message).getObject();
			System.out.println("NotificationAndKey = "+notificationAndKey.toString());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 System.out.println("--> L'objet NotificationAndKey contenant la notification et le handback a ete recu!");
		 AddNotificationListenerStored objectAddNotificationListenerStored = (AddNotificationListenerStored) hashTableNotificationListener.get(notificationAndKey.handback);
		 System.out.println("--> L'objet objectAddNotificationListenerStored a été recupere");
		 //System.out.println("--> On recupere le handback");
		 //Object handBackRecovered = hashTableNotificationContext.get(notificationAndKey.handback);
		 //System.out.println("--> On recupere le listener");
		 //NotificationListener notificationListenerRecovered = (NotificationListener) hashTableNotificationListener.get(notificationAndKey.handback);
		 //notificationListenerRecovered.handleNotification(notificationAndKey.notification, handBackRecovered);
		 objectAddNotificationListenerStored.listener.handleNotification(notificationAndKey.notification, objectAddNotificationListenerStored.handback);
		 System.out.println("La Notification a été faite");
	     System.out.println("NotiffffffffffffEndddddddddddddddddddddd!!!");
		notificationRecu = true;
		
	}
	
	
	
	/**
	 * <b>doRequete est la méthode qui est appelée par le client pour envoyé un Message(ObjectMesage).</b>
	 * 
	 * @param Object       <ul><li>L'objet qu'on fait passé en paramètre c'est celui qu'on veut envoyer.</ul></li>
	 * @return ObjectMessage   <ul><li>Cette méthode retourne le message reçu qui est un ObjectMessage.</ul></li>
	 * @throws JMSException
	 */
	
	public ObjectMessage doRequete(Object o) throws JMSException{
		  
		      
		
		/***Envoi d'un message Object dans la QueueRequete***/
	   try{
		message = session.createObjectMessage();
		message.setObject((Serializable) o);
		message.setStringProperty("Connecteur", "toto");
		if(o instanceof AddNotificationListener){
			message.setJMSReplyTo(queueNotification);
			System.out.println("Queue Notification : "+queueNotification.toString());
			System.out.println("***********************************************************Requete de notification envoye");
			producer.send(message);
		
			return null;
		}
		else{
		message.setJMSReplyTo(queueTemporaire);
		System.out.println("QueueTemporaire : "+queueTemporaire.toString());
		producer.send(message);
		System.out.println("Requete envoye");
		messageRecu = (ObjectMessage)consumer.receive();
		System.out.println("Thread retour : "+Thread.currentThread().getName().toString());
		if(messageRecu.getObject() != null && !(messageRecu.getObject() instanceof MBeanInfo)&& !(messageRecu.getObject() instanceof NotificationAndKey)){
			System.out.println("MessageRecu");
		    System.out.println(messageRecu.getObject().toString());
		}
		if(messageRecu.getObject() instanceof AttributeList){
			attributesList = (AttributeList) messageRecu.getObject();
		}
	  
	   }
		/*else if(messageRecu.getObject() instanceof MBeanInfo){
			char reponse = ' ';
			do{
		    
			Scanner sc = new Scanner(System.in);
			int i;
			//do{
				System.out.println("****Menu*****");
				System.out.println("1.getClassName");
				System.out.println("2.getAttributes");
				System.out.println("3.getOperations");
				i = sc.nextInt();
			//}while(i!=1 && i!=2 && i!=3);
			if(i == 1){
				System.out.println(messageRecu.getObject().getClass());
			}
			else if(i == 2){
				System.out.println("Liste des Attributs de la classe : "+messageRecu.getObject().getClass());
				int taille = ((MBeanInfo)messageRecu.getObject()).getAttributes().length;
				MBeanAttributeInfo [] mbeanAttributeInfo = new MBeanAttributeInfo[taille];
				mbeanAttributeInfo = ((MBeanInfo)messageRecu.getObject()).getAttributes();
				System.out.println("Nom de l'Attribut    |    Son Type ");
				System.out.println("----------------------------------");
				for (int j = 0; j < mbeanAttributeInfo.length; j++) {
					 System.out.println("  "+mbeanAttributeInfo[j].getName()+"                |     "+ mbeanAttributeInfo[j].getType());
					
				}
				
			}
			else if(i == 3){
				System.out.println("Liste des Operations");
				int taille2 = ((MBeanInfo)messageRecu.getObject()).getOperations().length;
				MBeanOperationInfo [] mbeanOperationInfo = new MBeanOperationInfo[taille2];
				mbeanOperationInfo = ((MBeanInfo)messageRecu.getObject()).getOperations();
				for (int j = 0; j < mbeanOperationInfo.length; j++) {
					System.out.println("methode n°"+(j+1)+": "+mbeanOperationInfo[j]);
					
				}
				System.out.println("-->Vous pouvez avoir une desciption plus détaillé des méthodes avec ce sous Menu ci-dessous");
				char reponse2 = ' ';
				int k = 0;
				do{
					System.out.println("***********Menu Methodes*************");
					for (int j = 0; j < mbeanOperationInfo.length; j++) {
						 System.out.println((j+1)+". Description de la méthode n°"+(j+1));
						
				    }
					k = sc.nextInt();
					while(k>taille2){
						System.out.println("Entrez un chiffre entre 1 et "+taille2);
					}
					
					k--; //On decremente k pour ne pas deborder dans le tableau mbeanOperationInfo
					MBeanParameterInfo [] mbeanParameterInfo = mbeanOperationInfo[k].getSignature();
				    System.out.println("*****************************");
				    System.out.println("Desciption de la methode n°"+(k+1));
				    System.out.println("*****************************");
				    System.out.println("  Nom : "+mbeanOperationInfo[k].getName());
				    System.out.println("  Class : "+mbeanParameterInfo.getClass());
				    System.out.println("  Type de Retour : "+mbeanOperationInfo[k].getReturnType());
				    System.out.print("  Paramètres : ");
				    if(mbeanParameterInfo.length == 0){
				    	System.out.println("Aucun");
				    }
				    else{
				       System.out.print("(");	
				       for (int j = 0; j < mbeanParameterInfo.length; j++) {
				    	   System.out.print(mbeanParameterInfo[j].getType());
				    	   if(j != mbeanParameterInfo.length -1)
				    		   System.out.print(",");
						
				       }
				       System.out.print(")");
				       System.out.println(" ");
				    }
				    do{
					  System.out.println("Voulez-vous quitter ce Sous-Menu ? O/N");
					  reponse2 = sc.next().charAt(0); 
				    }while(reponse2 != 'O' && reponse2 != 'N');
				    
				}while(reponse2 != 'O');
				
			}
			
			do{
			  System.out.println("Voulez-vous quitter le Menu Principale ? O/N");
			  reponse = sc.next().charAt(0); 
			}while(reponse != 'O' && reponse != 'N');
		}while(reponse != 'O');
		System.out.println("Au revoir!");				
		}*/
	   }catch (Exception e) {
		// TODO: handle exception
		   e.printStackTrace();
	}
		return messageRecu;
		
	}
	public static void main(String [] args) throws NamingException, JMSException, MalformedObjectNameException, NullPointerException, AdminException, IOException{
		/**
		 * DEBUT Connecteur RMI
		 
		MBeanServerConnection mbsc = null;
		JMXConnector connecteur = null;
		
		
		
		JMXServiceURL url = null;
		try {
			url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9000");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			connecteur = JMXConnectorFactory.connect(url, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		mbsc = connecteur.getMBeanServerConnection();
		
		***********FIN Connecteur RMI***********/
		
		ClientJMS ClientJMS = new ClientJMS();

		
		System.out.println("Le Client JMS est lancé");
		//Envoyer une requette vers la file QueueRequette
		GetAttribute getAttributes = new GetAttribute(new ObjectName("SimpleAgent:name=A"),"a");
		Attribute attribute = new Attribute("a",new Integer(3));
		SetAttribute setAttributes = new SetAttribute(new ObjectName("SimpleAgent:name=A"),attribute);
		/**Invoke**/
		
		/*****Initialisation des paramètres de la méthode invoke avec 
		 * les params et la signature de la methode affiche*****/
		
		ObjectName name,name2,name3;
		String operationName,operationName2;
		Object[] params = new Object[0];
		String[] sig = new String[0]; /*contient le type des paramètres passé dans la méthode qu'on veut appeler*/
		name = new ObjectName("SimpleAgent:name=A");
		operationName = "affiche";
		
		/*****Initialisation des paramètres de la méthode invoke avec 
		 * les params et la signature de la  methode addValeurs*****/
		
		Object[] params2 = new Object[2];
		String[] sig2 = new  String[2];
		sig2[0] = "int";
		sig2[1] = "int";
		params2[0] = new Integer(3);
		params2[1] = new Integer(4);
		
		
		name2 = new ObjectName("SimpleAgent:name=A");
		operationName2 = "addValeurs";
		Invoke invoke = new Invoke(name,operationName,params,sig);
		
		Invoke invoke2 = new Invoke(name2,operationName2,params2,sig2);
		/**MBean Info**/
		name3 = new ObjectName("SimpleAgent:name=A");
		GetMBeanInfo mbeanInfo = new GetMBeanInfo(name3);
		
		 ClientJMS.doRequete(getAttributes);
		 ClientJMS.doRequete(setAttributes);
		 ClientJMS.doRequete(getAttributes);
		 ClientJMS.doRequete(invoke); 
		 ClientJMS.doRequete(invoke2);   /**Invoker la methode addValeurs **/
		 ClientJMS.doRequete(mbeanInfo);
	}
	/**
	 * Le code précédent crée un connecteur RMI client qui est configuré pour se connecter à un connecteur RMI serveur crée par un agent JMX.
	 * Comme vous le voyez, on définit l'url de l'agent. Cela permet au connecteur client de récupérer un stub du connecteur RMI serveur via le registre RMI s'exécutant sur le port 9999 dans la machine local. 
	 * Le connecteur client crée est une instance de l'interface JMXConnector en appelant la méthode connect() de la classe JMXConnectorFactory. 
	 * La méthode connect() est appelée avec comme paramètre l'url de l'agent JMX distant. 
	 * Une instance de MBeanServerConnection, nommée mbsc, est créée en appelant la méthode getMBeanServerConnection() de l'instance de JMXConnector appelée jmxc. 
	 * Le connecteur client est maintenant connecté au MBeanServer crée par l'agent JMX distant, il peut dorénavant enregistrer des MBeans et effectuer des opérations d'une manière transparente sur eux. 
	 */
		
		
}