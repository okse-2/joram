package jmx.remote.jms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.objectweb.joram.client.jms.MessageProducer;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.TemporaryQueue;
import org.objectweb.joram.client.jms.admin.AdminException;

import com.sun.java.browser.net.ProxyService;

/**
 * In the Class <b>JmsJmxConnector</b>, the methodes of the client connector are implemented like : connect,getMBeanServerConnection() ...
 * 
 * The <i>MBeanServerConnection</i> is provided by the <i>MBeanServerConnectionDelegate</i> Class.
 * 
 * @author Djamel-Eddine Boumchedda
 *
 */
public class JmsJmxConnector implements JMXConnector {
	private JMXServiceURL jmsURL;
	private Map env;
	private boolean connected = false;
	MBeanServerConnectionDelegate mbeanServerConnectionDelegate;
	

	 public JmsJmxConnector(Map env,JMXServiceURL url) throws IOException{
		           this.env=env;
		           this.jmsURL= url;
		   		String path = new File("").getAbsolutePath();
		       /* File f = new File(path+"\\trace-Client");
				PrintStream pS = new PrintStream(f);
				Exception e = new Exception();
				e.printStackTrace(pS);*/
		           // set any props in the url
		         // JmsJmxConnectorSupport.populateProperties(this,jmsURL);
		           
	 }

	public void connect() throws IOException {
		// TODO Auto-generated method stub
		connect(this.env);
		
	}

	public void connect(Map<String, ?> env) throws IOException {
		// TODO Auto-generated method stub
	      try{
	    	
	    	//ClientJMS clientJms = new ClientJMS();
	    	//On créé la connection à partir de la connection Factory enregistrée dans la jndi
	    	//Récupération du contexte JNDI
	  		Context jndiContext = new InitialContext();
	  		//Recherche des objets administrés
	  		ConnectionFactory connectionFactory = (ConnectionFactory)jndiContext.lookup("ConnectionFactory");
	  		jndiContext.close();
	  		//Création des artéfacts nécessaires pour se connecter à la file et au sujet
	  		Connection connection = connectionFactory.createConnection();
	  		System.out.println("Connection : "+connection.toString());
	  		connection.start();  
	    	  
	    	  
			mbeanServerConnectionDelegate = new MBeanServerConnectionDelegate(connection); 
			
	    	
			
			ObjectName name = new ObjectName("SimpleAgent:name=A");
			String path = new File("").getAbsolutePath();
			System.out.println("*************"+path);
	        File f = new File(path+"\\trace-Client");
			PrintStream pS = new PrintStream(f);
			Exception e = new Exception();
			e.printStackTrace(pS);
			//mbeanServerConnectionDelegate.getAttribute(name, "a");
			/*mbeanServerConnectionDelegate.isRegistered(name);
			String operationName;
			Object[] params = new Object[0];
			String[] sig = new String[0];
			operationName = "affiche";
			mbeanServerConnectionDelegate.invoke(name, operationName, params, sig);
			
			mbeanServerConnectionDelegate.getAttribute(name,"a");
			mbeanServerConnectionDelegate.getMBeanInfo(name);
			String [] attributes = new String[1];
			attributes[0] = "a";
			mbeanServerConnectionDelegate.getAttributes(name, attributes);
			mbeanServerConnectionDelegate.getDefaultDomain();
			mbeanServerConnectionDelegate.queryNames(name, null);*/
			
		
	      }  catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} /*catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} *//* catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (AttributeNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} */
		
	}	
			
			/*	
			
			ClientJMS clientJms = new ClientJMS();
			/**Construction et Envoi des requettes jms
			GetAttributes getAttributes = new GetAttributes(new ObjectName("SimpleAgent:name=A"),"a");
			Attribute attribute = new Attribute("a",new Integer(3));
			SetAttributes setAttributes = new SetAttributes(new ObjectName("SimpleAgent:name=A"),attribute);
			**Invoke**
			
			*****Initialisation des paramètres de la méthode invoke avec 
			 * les params et la signature de la methode affiche*****
			
			ObjectName name,name2,name3;
			String operationName,operationName2;
			Object[] params = new Object[0];
			String[] sig = new String[0]; /*contient le type des paramètres passé dans la méthode qu'on veut appeler*
			name = new ObjectName("SimpleAgent:name=A");
			operationName = "affiche";
			
			/*****Initialisation des paramètres de la méthode invoke avec 
			 * les params et la signature de la  methode addValeurs*****
			
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
			**MBean Info**
			name3 = new ObjectName("SimpleAgent:name=A");
			GetMBeanInfo mbeanInfo = new GetMBeanInfo(name3);
			
			 clientJms.doRequete(getAttributes);
			 clientJms.doRequete(setAttributes);
			 clientJms.doRequete(getAttributes);
			 clientJms.doRequete(invoke); 
			 clientJms.doRequete(invoke2);   **Invoker la methode addValeurs **
			 clientJms.doRequete(mbeanInfo);
			
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AdminException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	
	public MBeanServerConnection getMBeanServerConnection() throws IOException {
		// TODO Auto-generated method stub
		return mbeanServerConnectionDelegate;
	}
	public MBeanServerConnection getMBeanServerConnection(
			Subject delegationSubject) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	public void addConnectionNotificationListener(
			NotificationListener listener, NotificationFilter filter,
			Object handback) {
		// TODO Auto-generated method stub
		
	}
	public void removeConnectionNotificationListener(
			NotificationListener listener) throws ListenerNotFoundException {
		// TODO Auto-generated method stub
		
	}
	public void removeConnectionNotificationListener(NotificationListener l,
			NotificationFilter f, Object handback)
			throws ListenerNotFoundException {
		// TODO Auto-generated method stub
		
	}
	public String getConnectionId() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}


}
