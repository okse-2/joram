package bridge;

import javax.naming.*;


import java.util.Properties;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;



public class CrossAdmin  {

   
    public static void main(String[] args) {
	new CrossAdmin().run();
    }
          
    public void run() {
	try{
	    AdminModule.connect("root", "root", 60);
	    javax.naming.Context jndiCtx = new javax.naming.InitialContext();
    
	    User.create("anonymous", "anonymous", 0);
	    User.create("anonymous", "anonymous", 1);
    
	    // create The foreign destination and connectionFactory
	    Queue foreignQueue = Queue.create(1, "foreignQueue");
	    foreignQueue.setFreeReading();
	    foreignQueue.setFreeWriting();
	    System.out.println("foreign queue = " + foreignQueue);
    
	    Topic foreignTopic = Topic.create(1, "foreignTopic");
	    foreignTopic.setFreeReading();
	    foreignTopic.setFreeWriting();
	    System.out.println("foreign topic = " + foreignTopic);
    
	    javax.jms.ConnectionFactory foreignCF = TcpConnectionFactory.create("localhost", 16011);
    
	    // bind foreign destination and connectionFactory
	    jndiCtx.rebind("foreignQueue", foreignQueue);
	    jndiCtx.rebind("foreignTopic", foreignTopic);
	    jndiCtx.rebind("foreignCF", foreignCF);
    
    
	    // Setting the bridge properties
	    Properties prop = new Properties();
	    // Foreign QueueConnectionFactory JNDI name: foreignCF
	    prop.setProperty("connectionFactoryName", "foreignCF");
	    // Foreign Queue JNDI name: foreignDest
	    prop.setProperty("destinationName", "foreignTopic");
	    // automaticRequest
	    prop.setProperty("automaticRequest", "false");

	    // Creating a Queue bridge on server 0:
	    Queue joramQueue = Queue.create(0,
					    "org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeQueue",
					    prop);
	    joramQueue.setFreeReading();
	    joramQueue.setFreeWriting();
	    System.out.println("joram queue = " + joramQueue);
    
	    // Setting the bridge properties
	    prop = new Properties();
	    // Foreign QueueConnectionFactory JNDI name: foreignCF
	    prop.setProperty("connectionFactoryName", "foreignCF");
	    // Foreign Queue JNDI name: foreignDest
	    prop.setProperty("destinationName", "foreignQueue");
    
	    // Creating a Topic bridge on server 0:
	    Topic joramTopic = Topic.create(0,
					    "org.objectweb.joram.mom.dest.jmsbridge.JMSBridgeTopic",
					    prop);
	    joramTopic.setFreeReading();
	    joramTopic.setFreeWriting();
	    System.out.println("joram topic = " + joramTopic);

	    javax.jms.ConnectionFactory joramCF = TcpConnectionFactory.create();

	    jndiCtx.rebind("joramQueue", joramQueue);
	    jndiCtx.rebind("joramTopic", joramTopic);
	    jndiCtx.rebind("joramCF", joramCF);
    
	    jndiCtx.close();

	    AdminModule.disconnect();
	    System.out.println("Admin closed.");
	}catch(Exception exc){
	}
    }
}
