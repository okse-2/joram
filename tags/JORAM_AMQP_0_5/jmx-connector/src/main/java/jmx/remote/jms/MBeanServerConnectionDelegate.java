package jmx.remote.jms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import fr.dyade.aaa.common.Pool;
  
  /***
   * Acts as a delegate for the MBeanServerConnection
   * @version $Revision: 1.1 $
   */
  public class  MBeanServerConnectionDelegate  implements MBeanServerConnection {
  
      protected Connection connection;
      protected ClientJMS clientJms;
      MBeanServerConnection mbs = ManagementFactory.getPlatformMBeanServer();
      FileWriter f;
      Pool poolRequestors;
      static HashMap hashTableNotificationListener; 
      static HashMap hashKey;
      Object key;
      int value = 0;
      
      
      public MBeanServerConnectionDelegate(ClientJMS clientJms, Connection connection) throws IOException{
          this.connection = connection;
          this.clientJms =  clientJms;
          String path = new File("").getAbsolutePath();
		  System.out.println("***MBC*****"+path);
	      f = new FileWriter(new File(path+"\\Ordre d'Appel des methodes.txt"), true);
	      //poolRequestors = new Pool("poolRequestors",10);  
	      hashTableNotificationListener = new HashMap();
	      hashKey = new HashMap();
      }
      public synchronized ObjectInstance createMBean(String className,ObjectName name) throws ReflectionException,InstanceAlreadyExistsException,MBeanRegistrationException,MBeanException,NotCompliantMBeanException,IOException{
    	  f.write("Appel a la methode createMBean(String className,ObjectName name) \n ");
    	  System.out.println("Appel a la methode createMBean(String className,ObjectName name)");
    	  CreateMbean createMbean = new CreateMbean(className, name);
    	  try {
			clientJms.doRequete(createMbean);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  try {
			return (ObjectInstance) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized ObjectInstance createMBean(String className,ObjectName name,ObjectName loaderName) throws ReflectionException,InstanceAlreadyExistsException,MBeanRegistrationException,MBeanException,NotCompliantMBeanException,InstanceNotFoundException,IOException{
    	  f.write("Appel a la methode createMBean(String className,ObjectName name,ObjectName loaderName) \n ");
    	  System.out.println("Appel a la methode createMBean(String className,ObjectName name,ObjectName loaderName)");
    	  CreateMbean2 createMbean2 = new CreateMbean2(className, name, loaderName);
    	  try {
			clientJms.doRequete(createMbean2);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  

    	  try {
			return (ObjectInstance) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized ObjectInstance createMBean(String className,ObjectName name,Object[] params,String[] signature) throws ReflectionException,InstanceAlreadyExistsException,MBeanRegistrationException,MBeanException,NotCompliantMBeanException,IOException{
    	  f.write("Appel a la methode createMBean(String className,ObjectName name,Object[] params,String[] signature) \n ");
    	  System.out.println("Appel a la methode createMBean(String className,ObjectName name,Object[] params,String[] signature)");
    	  CreateMbean3 createMbean3 = new CreateMbean3(className, name, params, signature);
    	  try {
			clientJms.doRequete(createMbean3);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  
    	  try {
			return (ObjectInstance) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    	  
      }
      
  
      public synchronized ObjectInstance createMBean(String className,ObjectName name,ObjectName loaderName,Object[] params,String[] signature) throws ReflectionException,InstanceAlreadyExistsException,MBeanRegistrationException,MBeanException,NotCompliantMBeanException,InstanceNotFoundException,IOException{
    	  f.write("Appel a la methode  createMBean(String className,ObjectName name,ObjectName loaderName,Object[] params,String[] signature) \n ");
    	  System.out.println("Appel a la methode  createMBean(String className,ObjectName name,ObjectName loaderName,Object[] params,String[] signature)");
    	  CreateMbean4 createMbean4 = new CreateMbean4(className, name, loaderName, params, signature);
    	  try {
			clientJms.doRequete(createMbean4);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  try {
			return (ObjectInstance) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized void unregisterMBean(ObjectName name) throws InstanceNotFoundException,MBeanRegistrationException,IOException{
    	  f.write("Appel a la methode unregisterMBean(ObjectName name) \n ");
    	  System.out.println("Appel a la methode unregisterMBean(ObjectName name)");
    	  UnregisterMbean unregisterMbean = new UnregisterMbean(name);
    	  try {
			clientJms.doRequete(unregisterMbean);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
  
      public synchronized ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException,IOException{
    	  f.write("Appel a la methode getObjectInstance(ObjectName name) \n ");
    	  System.out.println("Appel a la methode getObjectInstance(ObjectName name)");
    	  GetObjectInstance getObjectInstance = new GetObjectInstance(name);
    	  try {
			clientJms.doRequete(getObjectInstance);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  try {
			return (ObjectInstance) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized Set queryMBeans(ObjectName name,QueryExp query) throws IOException{
    	  f.write("Appel a la methode queryMBeans(ObjectName name,QueryExp query) \n ");
    	  System.out.println("Appel a la methode queryMBeans(ObjectName name,QueryExp query)");
    	  QueryMbeans queryMbeans = new QueryMbeans(name, query);
    	  try {
			clientJms.doRequete(queryMbeans);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  try {
			return (Set) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized Set queryNames(ObjectName name,QueryExp query) throws IOException{
    	  f.write("Appel a la methode queryNames(ObjectName name,QueryExp query) \n ");
    	  System.out.println("Appel a la methode queryNames() \n ");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  QueryName queryNames = new QueryName(name,query);
    	  try {
			clientJms.doRequete(queryNames);
		} catch (JMSException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	  try {
			return (Set) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized boolean isRegistered(ObjectName name) throws IOException{
    	  f.write("Appel a la methode isRegistered(ObjectName name)\n ");
    	  f.write("name = "+name);
    	  System.out.println("Appel a la methode isRegistered(ObjectName name) \n ");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  IsRegistered isRegistered = new IsRegistered(name);
    	  try {
			clientJms.doRequete(isRegistered);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	  try {
			return (Boolean) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
      }
  
      public synchronized Integer getMBeanCount() throws IOException{
    	  f.write("Appel a la methode getMBeanCount() \n ");
    	  System.out.println("Appel a la methode getMBeanCount()");
    	  GetMBeanCount getMBeanCount = new GetMBeanCount();
    	  try {
			clientJms.doRequete(getMBeanCount);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  try {
			return (Integer) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
      }
  
      public synchronized Object getAttribute(ObjectName name,String attribute) throws MBeanException,AttributeNotFoundException,InstanceNotFoundException,ReflectionException,IOException{
    	  f.write("Appel a la methode getAttribute(ObjectName name,String attribute) \n ");
    	  System.out.println("Appel a la methode getAttribute() \n ");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  GetAttribute getAttributes = new GetAttribute(name, attribute);
    	  try {
			clientJms.doRequete(getAttributes);
			File f = new File("trace-Client.txt");
			PrintStream pS = new PrintStream(f);
			Exception e = new Exception();
			e.printStackTrace(pS);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return getAttributes;
      }
 
     public synchronized AttributeList getAttributes(ObjectName name,String[] attributes) throws InstanceNotFoundException,ReflectionException,IOException{
    	  f.write("Appel a la methode getAttributes(ObjectName name,String[] attributes) \n ");
    	  System.out.println("Appel a la methode getAttributes() \n ");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  GetAttributes getAttributes = new GetAttributes(name, attributes);
    	  try {
			clientJms.doRequete(getAttributes);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	 return clientJms.attributesList;
     }
 
     public synchronized void setAttribute(ObjectName name,Attribute attribute) throws InstanceNotFoundException,AttributeNotFoundException,InvalidAttributeValueException,MBeanException,ReflectionException,IOException{
    	  f.write("Appel a la methode setAttribute(ObjectName name,Attribute attribute) \n ");
    	  System.out.println("Appel a la methode setAttribute(ObjectName name,Attribute attribute)");
    	  SetAttribute setAttribute = new SetAttribute(name, attribute);
    	  System.out.println("Appel a la methode setAttribute() \n ");
    	  try {
			clientJms.doRequete(setAttribute);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
     }
 
     public synchronized AttributeList setAttributes(ObjectName name,AttributeList attributes) throws InstanceNotFoundException,ReflectionException,IOException{
    	  f.write("Appel a la methode setAttributes(ObjectName name,AttributeList attributes) \n ");
    	  System.out.println("Appel a la methode setAttributes(ObjectName name,AttributeList attributes)");
    	  SetAttributes setAttributes = new SetAttributes(name, attributes);
    	  try {
			clientJms.doRequete(setAttributes);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  try {
			return (AttributeList) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return attributes;
    }
 
     public synchronized Object invoke(ObjectName name,String operationName,Object[] params,String[] signature) throws InstanceNotFoundException,MBeanException,ReflectionException,IOException{
    	  f.write("Appel a la methode invoke(ObjectName name,String operationName,Object[] params,String[] signature) \n ");
    	  System.out.println("Appel a la methode invoke()");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  Invoke invoke = new Invoke(name,operationName,params,signature);
    	  try {
			clientJms.doRequete(invoke);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  try {
			return clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return invoke;
		
     }
 
     public synchronized String getDefaultDomain() throws IOException{
    	  f.write("Appel a la methode getDefaultDomain() \n ");
    	  GetDefaultDomain getDefaultDomain = new GetDefaultDomain();
    	  System.out.println("Appel a la methode getDefaultDomain() \n ");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  try {
			clientJms.doRequete(getDefaultDomain);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String string = "appel a getDefaultDomain";
    	 return string;
     }
 

     public synchronized String[] getDomains() throws IOException{
    	f.write("Appel a la methode getDomains() \n ");
    	System.out.println("Appel a la methode getDomains()");
    	GetDomains getDomains = new GetDomains();
    	try {
			clientJms.doRequete(getDomains);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			return (String[]) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
     }
 
     public synchronized void addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) throws InstanceNotFoundException,IOException{
    	  f.write("Appel a la methode addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) \n ");
    	  System.out.println("--> Appel a la methode addNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)");
    	  value++;
    	  try{
    	  key = new Integer(value);
    	  System.out.println(key.toString());
    	  AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(name, listener, filter, handback);
    	  hashTableNotificationListener.put(key, objectAddNotificationListenerStored);
    	  hashKey.put(objectAddNotificationListenerStored, key);
    	  System.out.println("ici: "+hashKey.get(new AddNotificationListenerStored(name, listener, filter, handback)));
    	  System.out.println("hashhhhhhhhhhhhhhhhhhh");
    	  
    	  System.out.println("affichage du contenu de hashKey"+hashKey);
    	  
    	  AddNotificationListener addNotificationListener = new AddNotificationListener(name, filter,key);
    	
        
    	  try {
			 //clientJms.doRequete(hashTableNotificationContext);
        	 clientJms.doRequete(addNotificationListener);
        	 System.out.println("--> L'objet addNotificationListener contenant le name,filter et la key a ete envoye");
        	 
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }catch(Exception e){
        	e.printStackTrace();
        }
     }
 
     public synchronized void addNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) throws InstanceNotFoundException,IOException{
    	  f.write("Appel a la methode addNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) \n ");
    	 //mbs.addNotificationListener(name, listener, filter, handback);
    	  System.out.println("Appel a la methode addNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback)");
    	  AddNotificationListener2 objAddNotificationListener2 = new AddNotificationListener2(name, listener, filter, handback);
    	  
        
     }
 
    public synchronized void removeNotificationListener(ObjectName name,ObjectName listener) throws InstanceNotFoundException,ListenerNotFoundException,IOException{
    	  f.write("Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener) \n ");
    	  System.out.println("Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener)");
    	  
    	  //mbs.removeNotificationListener(name, listener);
        
     }
 
     public synchronized void removeNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) throws InstanceNotFoundException,ListenerNotFoundException,IOException{
    	  f.write("Appel a la methode removeNotificationListener(ObjectName name,ObjectName listener,NotificationFilter filter,Object handback) \n ");
    	 mbs.removeNotificationListener(name, listener,filter,handback);
        
     }
 
     public synchronized void removeNotificationListener(ObjectName name,NotificationListener listener) throws InstanceNotFoundException,ListenerNotFoundException,IOException{
    	  f.write("Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener) \n ");
    	  System.out.println("**************************Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener)" );
    	  Object keyRestored = null;
    	  value--;
    	  AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(name, listener, null, null); 
    	  Iterator<Map.Entry<Integer,AddNotificationListenerStored>> it = hashTableNotificationListener.entrySet().iterator();
    	  Map.Entry<Integer,AddNotificationListenerStored> pairKeyListener ;
    	  while (it.hasNext()) {

    		  pairKeyListener = it.next();
    	      if(pairKeyListener.getValue().equals(objectAddNotificationListenerStored)){
    	    	  keyRestored = pairKeyListener.getKey();
    			   System.out.println("la clé a ete touvé !! key = "+keyRestored);
    			   System.out.println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "+keyRestored);
    		    	  it.remove();//remove(keyRestored);
    		    	  System.out.println(hashTableNotificationListener.toString());
    		    	  System.out.println("***----> l'objet objectAddNotificationListenerStored a ete supprimé de la hashTableNotificationListener ");

    		    	  RemoveNotificationListener3 objectRemoveNotificationListener3 = new RemoveNotificationListener3(name,keyRestored);
    		    	  try {
    					clientJms.doRequete(objectRemoveNotificationListener3);
    				} catch (JMSException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
   	        }
   	    	
    	    	  
    	      }


    	  }
    	  
    	  
    	  
    	  
    	  
    /*  Set<Map.Entry<Object, AddNotificationListenerStored>> set = hashTableNotificationListener.entrySet();
    	  Iterator it = set.iterator();
    	  while (it.hasNext()){
    		  if(objectAddNotificationListenerStored.equals(it.next))
    	  }
          

    	  for (Map.Entry<Object, AddNotificationListenerStored> me : set) {
    	        if(me.getValue().equals(objectAddNotificationListenerStored)){
    	         keyRestored = me.getKey();
     			   System.out.println("la clé a ete touvé !! key = "+keyRestored);
     			   System.out.println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "+keyRestored);
     		    	  hashTableNotificationListener.remove(keyRestored);
     		    	  System.out.println("***----> l'objet objectAddNotificationListenerStored a ete supprimé de la hashTableNotificationListener ");

     		    	  RemoveNotificationListener3 objectRemoveNotificationListener3 = new RemoveNotificationListener3(name,keyRestored);
     		    	  try {
     					clientJms.doRequete(objectRemoveNotificationListener3);
     				} catch (JMSException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}
    	        }
    	    	
    	    	//System.out.print(me.getKey() + ": ");
    	        //System.out.println(me.getValue());
    	      }*/

    	  
    	  
    	  
    	  
    	 
    	//  Set cles = hashKey.keySet();
    	 // Iterator it = cles.iterator();
    	/*  while (it.hasNext()){
    		    
    		   AddNotificationListenerStored cle = (AddNotificationListenerStored) it.next();
    		   if(objectAddNotificationListenerStored.equals(cle)){
    			   objectAddNotificationListenerStored = cle;
    			   keyRestored = hashKey.get(objectAddNotificationListenerStored);
    			   System.out.println("la clé a ete touvé !! key = "+keyRestored);
    			   System.out.println("------------->    keyRestored de removeNotificationListener(ObjectName name,NotificationListener listener)  : "+keyRestored);
    		    	  hashTableNotificationListener.remove(keyRestored);
    		    	  System.out.println("***----> l'objet objectAddNotificationListenerStored a ete supprimé de la hashTableNotificationListener ");

    		    	  RemoveNotificationListener3 objectRemoveNotificationListener3 = new RemoveNotificationListener3(name,keyRestored);
    		    	  try {
    					clientJms.doRequete(objectRemoveNotificationListener3);
    				} catch (JMSException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			   
    		   }
    		  
    	  }*/
    	 
  
         
    
 
     public synchronized void removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) throws InstanceNotFoundException,ListenerNotFoundException,IOException{
    	  f.write("Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback) \n ");
    	 try{
    	  System.out.println("********************Appel a la methode removeNotificationListener(ObjectName name,NotificationListener listener,NotificationFilter filter,Object handback)");
    	  Object keyRestored = null;
    	  value--;
    	  AddNotificationListenerStored objectAddNotificationListenerStored = new AddNotificationListenerStored(name, listener, filter, handback); 
    	  Set cles = hashKey.keySet();
    	  Iterator it = cles.iterator();
    	  while (it.hasNext()){
    		    
    		   AddNotificationListenerStored cle = (AddNotificationListenerStored) it.next();
    		   if(objectAddNotificationListenerStored.equals(cle)){
    			   objectAddNotificationListenerStored = cle;
    			   keyRestored = hashKey.get(objectAddNotificationListenerStored);
    			   System.out.println("la clé a ete touvé !! key = "+keyRestored);
    			   break;
    			   
    		   }
    		  
    	  }
    	 
    	  System.out.println("------------->    keyRestored : "+keyRestored);
    	  hashTableNotificationListener.remove(keyRestored);
    	  System.out.println("***----> l'objet objectAddNotificationListenerStored a ete supprimé de la hashTableNotificationListener ");
    	  hashKey.remove(objectAddNotificationListenerStored);
    	  System.out.println("la keyStored a ete supprime de la hashKey");
    	  RemoveNotificationListener4 removeNotificationListener4 = new RemoveNotificationListener4(name, filter, keyRestored);
    	  clientJms.doRequete(removeNotificationListener4);
    	  System.out.println("Requete RemoveNotificationListener envoyéeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
         
     }
 
     public synchronized MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException,IntrospectionException,ReflectionException,IOException{
    	  f.write("Appel a la methode getMBeanInfo(ObjectName name) \n ");
    	  System.out.println("Appel a la methode getMBeanInfo() \n ");
    	  System.out.println("thread appelant : "+Thread.currentThread().getName());
    	  GetMBeanInfo getMBeanInfo = new GetMBeanInfo(name);
    	  System.out.println("paramètre de getMBeanInfo name = "+name);
    	  try {
			clientJms.doRequete(getMBeanInfo);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	 try {
			return (MBeanInfo) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
     }

     public synchronized boolean isInstanceOf(ObjectName name,String className) throws InstanceNotFoundException,IOException{
    	  f.write("Appel a la methode isInstanceOf(ObjectName name,String className) \n ");
    	  System.out.println("Appel a la methode isInstanceOf(ObjectName name,String className)");
    	  IsInstanceOf isInstanceOf = new IsInstanceOf(name, className);
    	  try {
			clientJms.doRequete(isInstanceOf);
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	  try {
			return (Boolean) clientJms.messageRecu.getObject();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
     }
   
 
 }


