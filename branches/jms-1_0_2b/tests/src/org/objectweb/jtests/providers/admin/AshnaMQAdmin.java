package org.objectweb.jtests.providers.admin;

import org.objectweb.jtests.jms.admin.Admin;
import javax.naming.*;
import com.ashnasoft.jms.client.admin.*;
import com.ashnasoft.jms.client.*;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Properties;

public class AshnaMQAdmin implements Admin {
    
    private String name = "AshnaMQ";
    private InitialContext ictx = null;
    private ASAdministrator admin = null;

    public AshnaMQAdmin() {
	    try {
	        Properties props = new Properties();
	        props.setProperty("java.naming.factory.initial", "com.ashnasoft.jms.client.jndi.InitialContextFactoryImpl");
	        props.setProperty("java.naming.factory.host", "localhost");
	        props.setProperty("java.naming.provider.url", "jndi:Ashna://localhost:9090");
	        props.setProperty("java.naming.security.principal","admin");
	        props.setProperty("java.naming.security.credentials","admin");
	        ictx = new InitialContext (props);
            admin = new ASAdministrator("localhost", 9090, "admin", "admin", false);
	    } catch (Exception e) {
	        e.printStackTrace();
	        System.exit(1);
	    }
    }
    
    public String getName() {
	    return name;
    }
    
    public InitialContext createInitialContext() throws NamingException {
	    return ictx;
    }
  
    public void createQueueConnectionFactory(String qcfName) {
	    try {
	        ASQueueConnectionFactory qcf = new ASQueueConnectionFactory("localhost", 9090, false);
	        ictx.rebind(qcfName, qcf);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }

    public void createTopicConnectionFactory(String tcfName) {
	    try {
	        ASTopicConnectionFactory tcf = new ASTopicConnectionFactory("localhost", 9090, false);
	        ictx.rebind(tcfName, tcf);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }
 
    public void createQueue(String queueName) {
	    try {	    
	        admin.createQueue(queueName);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }
 
    public void createTopic(String topicName) {
	    try {
            admin.createTopic(topicName);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }

    public void deleteQueue(String queueName) {
	    try {
	        admin.removeQueue(queueName);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }
  
    public void deleteTopic(String topicName) {
	    try {
	        admin.removeTopic(topicName);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }
  
    public void deleteTopicConnectionFactory(String tcfName) {
	    try {
	        ictx.unbind(tcfName);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }

    public void deleteQueueConnectionFactory(String qcfName) {
	    try {
	        ictx.unbind(qcfName);
	    } 
	    catch (Exception e) {
	        e.printStackTrace();
	    }
    }
}
