package org.objectweb.jtests.providers.admin;

import org.objectweb.jtests.jms.admin.Admin;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.JMSException;
import java.util.Properties;

import com.swiftmq.admin.cli.CLI;
import com.swiftmq.admin.cli.CLIException;

public class SwiftMQAdmin implements Admin
{
  InitialContext ctx = null;
  CLI cli = null;
  QueueConnection qc = null;

  public SwiftMQAdmin()
  {
    try
    {
      Properties props = new Properties();
      props.setProperty("java.naming.factory.initial", "com.swiftmq.jndi.InitialContextFactoryImpl");
      props.setProperty("java.naming.provider.url", "smqp://localhost:4001/timeout=10000");
      ctx = new InitialContext(props);
      QueueConnectionFactory qcf = (QueueConnectionFactory)ctx.lookup("plainsocket@router1");
      qc = qcf.createQueueConnection();
      cli = new CLI(qc);
      cli.waitForRouter("router1");
      cli.executeCommand("sr router1");
      try
      {
        cli.executeCommand("cc /sys$jndi/aliases");
        cli.executeCommand("delete QueueConnectionFactory");
        cli.executeCommand("delete TopicConnectionFactory");
        cli.executeCommand("delete testqueue");
      } catch (CLIException e)
      {
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public String getName()
  {
    return "SwiftMQ";
  }

  public InitialContext createInitialContext()
      throws NamingException
  {
    return ctx;
  }

  public void createQueueConnectionFactory(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$jndi/aliases");
      cli.executeCommand("new "+name+" map-to plainsocket@router1");
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void createTopicConnectionFactory(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$jndi/aliases");
      cli.executeCommand("new "+name+" map-to plainsocket@router1");
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void createQueue(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$queuemanager/queues");
      cli.executeCommand("new "+name);
      cli.executeCommand("cc /sys$jndi/aliases");
      cli.executeCommand("new "+name+" map-to "+name+"@router1");
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void createTopic(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$topicmanager/topics");
      String [] s = cli.getContextEntities();
      boolean found = false;
      if (s != null)
      {
        for (int i=0;i<s.length;i++)
        {
          if (s[i].equals(name))
          {
            found = true;
            break;
          }
        }
      }
      if (!found)
        cli.executeCommand("new "+name);
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void deleteQueue(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$queuemanager/queues");
      cli.executeCommand("delete "+name);
      cli.executeCommand("cc /sys$jndi/aliases");
      cli.executeCommand("delete "+name);
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void deleteTopic(String name)
  {
    // do nothing
  }

  public void deleteQueueConnectionFactory(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$jndi/aliases");
      cli.executeCommand("delete "+name);
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public void deleteTopicConnectionFactory(String name)
  {
    try
    {
      cli.executeCommand("cc /sys$jndi/aliases");
      cli.executeCommand("delete "+name);
    } catch (CLIException e)
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
