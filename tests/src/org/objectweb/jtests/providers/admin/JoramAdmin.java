package org.objectweb.jtests.providers.admin;

import org.objectweb.jtests.jms.admin.Admin;

import javax.naming.*;
import fr.dyade.aaa.joram.*;
import javax.jms.Queue;
import javax.jms.Topic;
import java.util.*;
import java.io.*;

public class JoramAdmin implements Admin {
  
    private String name = "JORAM";
    InitialContext ictx = null;
    fr.dyade.aaa.joram.admin.Admin admin;

    public JoramAdmin() {
	try {
	    Properties props = new Properties();
	    props.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi.NamingContextFactory");
	    props.setProperty("java.naming.factory.host", "localhost");
	    props.setProperty("java.naming.provider.url", "http://localhost:16400");
	    ictx = new InitialContext (props);
	    admin = new fr.dyade.aaa.joram.admin.Admin("localhost", 16010, 
						       "root", "root", 100);  
	    try {
		admin.getUser("anonymous");
	    } catch (fr.dyade.aaa.joram.admin.AdminException e) {
		admin.createUser("anonymous", "anonymous");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }


    public String getName() {
	return name;
    }

    public InitialContext createInitialContext() throws NamingException {
	return ictx;
    }
  
    public void createConnectionFactory(String name) {
	try {
	    fr.dyade.aaa.joram.ConnectionFactory cf = admin.createConnectionFactory();
	    ictx.rebind(name, cf);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void createQueueConnectionFactory(String name) {
	try {
	    fr.dyade.aaa.joram.QueueConnectionFactory qcf = admin.createQueueConnectionFactory();
	    ictx.rebind(name, qcf);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void createTopicConnectionFactory(String name) {
	try {
	    fr.dyade.aaa.joram.TopicConnectionFactory tcf = admin.createTopicConnectionFactory();
	    ictx.rebind(name, tcf);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public void createQueue(String name) {
	try {
	    fr.dyade.aaa.joram.Queue queue = admin.createQueue(name);
	    admin.setFreeWriting(name);
	    admin.setFreeReading(name);
	    ictx.rebind(name, queue);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
  
    public void createTopic(String name) {
	try {
	    fr.dyade.aaa.joram.Topic topic = admin.createTopic(name);
	    admin.setFreeWriting(name);
	    admin.setFreeReading(name);
	    ictx.rebind(name, topic);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteQueue(String name) {
	try {
	    admin.deleteDestination(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
 
    public void deleteTopic(String name) {
	try {
	    admin.deleteDestination(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteConnectionFactory(String name) {
	try {
	    ictx.unbind(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteTopicConnectionFactory(String name) {
	try {
	    ictx.unbind(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void deleteQueueConnectionFactory(String name) {
	try {
	    ictx.unbind(name);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

}
