package org.objectweb.jtests.providers.admin;

import org.objectweb.jtests.jms.admin.Admin;
import javax.naming.*;
import fiorano.jms.runtime.admin.*;
import fiorano.jms.md.*;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.Properties;

public class FioranoMQAdmin implements Admin {
    
    private String name = "FioranoMQ";
    private InitialContext ictx = null;
    private MQAdminService adminService = null;

    public FioranoMQAdmin() {
	try {
	    Properties props = new Properties();
	    props.setProperty("java.naming.factory.initial", "fiorano.jms.runtime.naming.FioranoInitialContextFactory");
	    props.setProperty("java.naming.factory.host", "localhost");
	    props.setProperty("java.naming.provider.url", "http://localhost:1856");
	    props.setProperty("java.naming.security.principal","anonymous");
	    props.setProperty("java.naming.security.credentials","anonymous");
	    ictx = new InitialContext (props);
	    MQAdminConnectionFactory acf = (MQAdminConnectionFactory) ictx.lookup ("primaryACF");
	    MQAdminConnection ac = acf.createMQAdminConnection ("admin", "passwd");
	    adminService = ac.getMQAdminService();    
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
	    QueueConnectionFactoryMetaData factoryMetaData = new QueueConnectionFactoryMetaData();
	    factoryMetaData.setName(qcfName);
	    factoryMetaData.setDescription ("Queue Connection Factory");
	    factoryMetaData.setConnectURL ("http://localhost:1856");
	    adminService.createQueueConnectionFactory (factoryMetaData);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void createTopicConnectionFactory(String tcfName) {
	try {
	    TopicConnectionFactoryMetaData factoryMetaData = new TopicConnectionFactoryMetaData();
	    factoryMetaData.setName(tcfName);
	    factoryMetaData.setDescription ("Topic Connection Factory");
	    factoryMetaData.setConnectURL ("http://localhost:1856");
	    adminService.createTopicConnectionFactory (factoryMetaData);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public void createQueue(String queueName) {
	try {
	    QueueMetaData queueMetaData = new QueueMetaData();
	    queueMetaData.setName(queueName);
	    adminService.createQueue(queueMetaData);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public void createTopic(String topicName) {
	try {
	    TopicMetaData topicMetaData1 = new TopicMetaData();
	    topicMetaData1.setName(topicName);
	    adminService.createTopic(topicMetaData1);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteQueue(String queueName) {
	try {
	    adminService.deleteQueue(queueName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void deleteTopic(String topicName) {
	try {
	    adminService.deleteTopic(topicName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void deleteTopicConnectionFactory(String tcfName) {
	try {
	    adminService.deleteTopicConnectionFactory(tcfName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteQueueConnectionFactory(String qcfName) {
	try {
	    adminService.deleteQueueConnectionFactory(qcfName);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
