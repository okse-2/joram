package org.objectweb.jtests.providers.admin;

import org.objectweb.jtests.jms.admin.Admin;

import javax.naming.*;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.*;
import java.io.*;
import com.pramati.services.jms.spi.admin.JMSServerMBean;
import com.pramati.util.naming.BindNamesAndDefaultParamValues;

/**
 * This wrapper has been provided for Joram's open source Test suite by Pramati Technologies.
 * The wrapper is used by the Test framework to create and destroy administered objects
 * <b>queues,topics and connection factories</b><br>
 * Pramati Message Server's  Adminstration is based on JMX : Java Management Extension framework;
 * Each adminstrable object is bound in the MBean server<br>
 * 
 * In this class the main JMSServerMBean is being used for creating and deleting the Administered
 * objects.
 * 
 * @see com.pramai.services.jms.spi.admin.JMSServerMBean
 * 
 * @author Rajdeep Dua : mailto rajdeep@pramati.com
 */
public class PramatiAdmin implements Admin {
  
    private String name = "PRAMATI";
    InitialContext ictx = null;
    JMSServerMBean jmsServerMBean = null;
    /**
     * Initialises the wrapper by looking up in the Pramati Message Server's naming service for
     * the JMSServerMBean.
     */
    public PramatiAdmin() {
	try {
	    Properties props = new Properties();
	    props.setProperty("java.naming.factory.initial", 
			      "com.pramati.naming.client.PramatiClientContextFactory");
	    //Please change the IP and port according to the configuration ,2099 is the
	    //default naming service port for Standalone JMSServer,for Embedded JMSServer in
	    //Pramati's Application server it is 9191
	    props.setProperty("java.naming.provider.url", "http://localhost:2099");
	    ictx = new InitialContext (props);
	    jmsServerMBean = (JMSServerMBean)ictx.lookup(BindNamesAndDefaultParamValues.JMS_SERVER_MBEAN);
	}catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public String getName() {
	return name;
    }

    public InitialContext createInitialContext() throws NamingException {
	return ictx;
    }
  
    public void createQueueConnectionFactory(String name) {
	try {
	    jmsServerMBean.createQueueConnectionFactory(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void createTopicConnectionFactory(String name) {
	try {
	    jmsServerMBean.createTopicConnectionFactory(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public void createQueue(String name) {
	try {
	    jmsServerMBean.createQueue(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void createTopic(String name) {
	try {
	    jmsServerMBean.createTopic(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteQueue(String name) {
	try {
	    jmsServerMBean.deleteQueue(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public void deleteTopic(String name) {
	try {
	    jmsServerMBean.deleteTopic(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteTopicConnectionFactory(String name) {
	try {
	    jmsServerMBean.deleteTopicConnectionFactory(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteQueueConnectionFactory(String name) {
	try {
	    jmsServerMBean.deleteQueueConnectionFactory(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
