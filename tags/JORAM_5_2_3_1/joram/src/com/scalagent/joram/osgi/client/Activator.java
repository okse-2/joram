package com.scalagent.joram.osgi.client;

import java.io.Reader;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.scalagent.joram.osgi.client.service.JoramClient;

/**
 * Activator class for Joram client bundle.
 * This bundle allows the use of the Joram's JMS service to the other bundle of
 * the platform. The Joram server can be either remote or colocated.
 */
public class Activator implements BundleActivator {

  private static BundleContext bcontext;

  public static BundleContext getBundleContext() {
    return bcontext;
  }

  /**
   * Implements BundleActivator.start().
   *
   * @param context the framework context for the bundle.
   * @see BundleActivator#start(BundleContext)
   */
  public void start(BundleContext context) throws Exception {
    bcontext = context;

    Properties props = new Properties();
    context.registerService(JoramClient.class.getName(), new JoramClientImpl(), props);
  }

  /**
   * Implements BundleActivator.stop().
   *
   * @param context the framework context for the bundle.
   * @see BundleActivator#stop(BundleContext)
   */
  public void stop(BundleContext context) {
  }

  public static class JoramClientImpl implements com.scalagent.joram.osgi.client.service.JoramClient {
    /**
     *
     */
    public void connect(String host, int port,
                        String name, String password,
                        int cnxTimer) throws Exception {
      AdminModule.connect(host, port, name, password, cnxTimer);
    }
    
    /**
     *
     */
    public void disconnect() {
      AdminModule.disconnect();
    }

    /**
     *
     */
    public ConnectionFactory getTcpConnectionFactory(String hostname, int port) throws Exception {
      return TcpConnectionFactory.create(hostname, port);
    }

    /**
     *
     */
    public Context getInitialContext() throws Exception {
      Properties prop = new Properties();
      prop.setProperty("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      prop.setProperty("java.naming.factory.host", "localhost");
      prop.setProperty("java.naming.factory.port", "16400");
      return getInitialContext(prop);
    }

    /**
     *
     */
    public Context getInitialContext(Hashtable prop) throws Exception {
      Thread ct = Thread.currentThread();
      ClassLoader cl = ct.getContextClassLoader();
      ct.setContextClassLoader(JoramClientImpl.class.getClassLoader());
      
      Context ctx = new InitialContext(prop);
      
      ct.setContextClassLoader(cl);
      return ctx;
    }
    
    Context ictx;
    
    /**
     * Retrieves the named object.
     * 
     * @param name  the name of the object to look up
     * @return  the object bound to name
     * 
     * @throws NamingException  if a naming exception is encountered
     * 
     * @see Context#lookup(String)
     */
    public Object lookup(String name) throws NamingException {
      Thread ct = Thread.currentThread();
      ClassLoader cl = ct.getContextClassLoader();
      ct.setContextClassLoader(JoramClientImpl.class.getClassLoader());
      
      Object obj = ictx.lookup(name);
      
      ct.setContextClassLoader(cl);
      
      return obj;      
    }

    /**
     *
     */
    public boolean executeAdminXML(Reader reader) throws Exception {
      return AdminModule.executeAdmin(reader);
    }

    /**
     * Creates or retrieves a user on the underlying JORAM server.
     *
     * @exception Exception   If the creation fails.
     */
    public User createUser(String name, String password) throws Exception {
      return User.create(name, password);
    }

    /**
     * Creates or retrieves a queue destination on the underlying JORAM server,
     * (re)binds the corresponding <code>Queue</code> instance.
     *
     * @param name       The name of the queue.
     *
     * @exception Exception   If the creation fails.
     */
    public Queue createQueue(String name) throws Exception {
      Queue queue = Queue.create(name);
      queue.setFreelyWriteable(true);
      queue.setFreelyReadable(true);

      return queue;
    }

    /**
     * Creates or retrieves a topic destination on the underlying JORAM server,
     * (re)binds the corresponding <code>Topic</code> instance.
     *
     * @exception Exception   If the creation fails.
     */
    public Topic createTopic(String name) throws Exception {
      Topic topic = Topic.create(name);
      topic.setFreelyWriteable(true);
      topic.setFreelyReadable(true);

      return topic;
    }

    /**
     * Returns the list of all destinations that exist on the server.
     */
    public Destination[] getDestinations() throws Exception {
      return AdminModule.getDestinations();
    }

    /**
     * Returns the list of all users that exist on a given server.
     */
    public User[] getUsers() throws Exception {
      return AdminModule.getUsers();
    }
  }
}
