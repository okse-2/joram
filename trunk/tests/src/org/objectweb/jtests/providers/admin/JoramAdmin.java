package org.objectweb.jtests.providers.admin;

import org.objectweb.jtests.jms.admin.Admin;

import javax.naming.*;
import fr.dyade.aaa.joram.admin.AdminItf;
import fr.dyade.aaa.joram.admin.AdminImpl;
import javax.jms.*;
import java.util.*;
import java.io.*;

public class JoramAdmin implements Admin
{
  private String name = "JORAM";
  InitialContext ictx = null;
  AdminItf admin;

  public JoramAdmin()
  {
    try {
      Properties props = new Properties();
      props.setProperty("java.naming.factory.initial",
                        "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      props.setProperty("java.naming.factory.host", "localhost");
      props.setProperty("java.naming.provider.url", "http://localhost:16400");

      ictx = new InitialContext (props);
      admin = new AdminImpl();
      admin.connect("root", "root", 100);  

      admin.createUser("anonymous", "anonymous", 0);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  public String getName()
  {
    return name;
  }

  public InitialContext createInitialContext() throws NamingException
  {
    return ictx;
  }
  
  public void createConnectionFactory(String name)
  {
    try {
      ConnectionFactory cf = admin.createConnectionFactory();
      ictx.rebind(name, cf);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void createQueueConnectionFactory(String name)
  {
    try {
      QueueConnectionFactory cf = admin.createQueueConnectionFactory();
      ictx.rebind(name, cf);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void createTopicConnectionFactory(String name)
  {
    try {
      TopicConnectionFactory cf = admin.createTopicConnectionFactory();
      ictx.rebind(name, cf);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
 
  public void createQueue(String name)
  {
    try {
      Queue queue = admin.createQueue(0);
      ictx.rebind(name, queue);
      admin.setFreeWriting(queue);
      admin.setFreeReading(queue);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public void createTopic(String name)
  {
    try {
      Topic topic = admin.createTopic(0);
      ictx.rebind(name, topic);
      admin.setFreeWriting(topic);
      admin.setFreeReading(topic);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteQueue(String name)
  {
    try {
      Queue queue = (Queue) ictx.lookup(name);
      admin.deleteDestination(queue);
      ictx.unbind(name);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteTopic(String name)
  {
    try {
      Topic topic = (Topic) ictx.lookup(name);
      admin.deleteDestination(topic);
      ictx.unbind(name);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
 
  public void deleteConnectionFactory(String name)
  {
    try {
      ictx.unbind(name);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteTopicConnectionFactory(String name)
  {
    try {
      ictx.unbind(name);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void deleteQueueConnectionFactory(String name)
  {
    try {
      ictx.unbind(name);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
