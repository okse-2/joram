package com.scalagent.joram.osgi.server;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.local.LocalConnectionFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.scalagent.joram.osgi.server.service.JoramAdmin;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.Debug;

public class Activator implements BundleActivator {

  private static BundleContext bcontext;

  public static BundleContext getBundleContext() {
    return bcontext;
  }

  /**
   * Implements BundleActivator.start(). Initialize and starts the
   * AgentServer runtime.
   *
   * @param context the framework context for the bundle.
   */
  public void start(BundleContext context) throws Exception {
    bcontext = context;

    startAgentServer();

    Properties props = new Properties();
    context.registerService(JoramAdmin.class.getName(), 
                            new JoramAdminImpl(),
                            props);
  }

  private void startAgentServer() throws Exception {
    //search a3server.prop in path used to load classes.
    ClassLoader classLoader = getClass().getClassLoader();
    InputStream is = classLoader.getResourceAsStream("a3server.prop");
    Properties a3prop = new Properties();
    a3prop.load(is);

    String serverId = a3prop.getProperty("sid");
    String path = a3prop.getProperty("path");
    String relativePath = a3prop.getProperty("relativePath");
    String storageDirName = a3prop.getProperty("storageDirName");
    String logicalServerName = a3prop.getProperty("logicalServerName");

    File storageDir = new File(new File(path), storageDirName);
    File lockFile = new File(storageDir, "lock");
    lockFile.delete();

    // find the most precise debug file
    if (logicalServerName != null) try {
      String debugDir = System.getProperty(
        Debug.DEBUG_DIR_PROPERTY, Debug.DEFAULT_DEBUG_DIR);
      File debugFile = new File(debugDir, logicalServerName);
      if ((debugFile != null) &&
          debugFile.exists() &&
          debugFile.isDirectory()) {
        debugDir = debugFile.getPath();
        String debugFileName = System.getProperty(
          Debug.DEBUG_FILE_PROPERTY, Debug.DEFAULT_DEBUG_FILE);
        debugFile = new File(debugFile, debugFileName);
        if ((debugFile != null) &&
            debugFile.exists() &&
            debugFile.isFile() &&
            (debugFile.length() != 0)) {
          Debug.setDebugDir(debugDir);
          Debug.setDebugFileName(debugFileName);
        }
      }
    } catch (Exception exc) {
    }

    AgentServer.init(new String[] {
      serverId, 
      storageDir.getAbsolutePath()});
    AgentServer.start();

  }

  /**
   * Implements BundleActivator.stop(). Stops the AgentServer runtime.
   *
   * @param context the framework context for the bundle.
   */
  public void stop(BundleContext context) {
    stopAgentServer();
  }

  private void stopAgentServer()  {
    AgentServer.stop();
    AgentServer.reset();
  }

  public static class JoramAdminImpl implements JoramAdmin {

    private String rootUserName= "root";

    public void setRootUserName(String root) {
      this.rootUserName = root;
    }

    private String rootPassword = "root";

    public void setRootPassword(String password) {
      this.rootPassword = password;
    }

    /**
     *
     */
    public ConnectionFactory getLocalConnectionFactory() throws Exception {
      return (ConnectionFactory) LocalConnectionFactory.create();
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
    public void createUser(String name, String password) throws Exception {
      try {
        AdminModule.collocatedConnect(rootUserName, rootPassword);
        User.create(name, password);
      } finally {
        AdminModule.disconnect();
      }
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
      Queue queue = null;
      try {
        AdminModule.collocatedConnect(rootUserName, rootPassword);
        queue = Queue.create(name);
        queue.setFreelyWriteable(true);
        queue.setFreelyReadable(true);
      } finally {
        AdminModule.disconnect();
      }
      return queue;
    }

    /**
     * Creates or retrieves a topic destination on the underlying JORAM server,
     * (re)binds the corresponding <code>Topic</code> instance.
     *
     * @exception Exception   If the creation fails.
     */
    public Topic createTopic(String name) throws Exception {
      Topic topic = null;
      try {
        AdminModule.collocatedConnect(rootUserName, rootPassword);
        topic = Topic.create(name);
        topic.setFreelyWriteable(true);
        topic.setFreelyReadable(true);
      } finally {
        AdminModule.disconnect();
      }
      return topic;
    }

    /**
     * Returns the list of all destinations that exist on the server.
     */
    public Destination[] getDestinations() throws Exception {
      Destination[] destinations = null;
      try {
        AdminModule.collocatedConnect(rootUserName, rootPassword);
        destinations = AdminModule.getDestinations();
      } finally {
        AdminModule.disconnect();
      }
      return destinations;
    }

    /**
     * Returns the list of all users that exist on a given server.
     */
    public User[] getUsers() throws Exception {
      User[] users = null;
      try {
        AdminModule.collocatedConnect(rootUserName, rootPassword);
        users = AdminModule.getUsers();
      } finally {
        AdminModule.disconnect();
      }
      return users;
    }
  }
}
