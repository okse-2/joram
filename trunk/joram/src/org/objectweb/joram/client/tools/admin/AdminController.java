/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): Alexander Fedorowicz
 * Contributor(s):
 */
package org.objectweb.joram.client.tools.admin;

import java.util.*;
import java.net.ConnectException;
import javax.naming.*;
import javax.swing.tree.*;
import javax.jms.*;

import fr.dyade.aaa.joram.admin.*;

public class AdminController
{
  private final AdminItf admin;
  private final MonitorItf monitor;
  private boolean adminConnected = false;
  private String adminConnectionStr = "Not connected";

  private ControllerEventListener gui;

  private DefaultTreeModel adminTreeModel;
  private MutableTreeNode adminRoot;
  
  private DefaultTreeModel jndiTreeModel;
  private MutableTreeNode jndiRoot;

  private String namedCtx = "";
  private Context ctx = null;
  private boolean jndiConnected = false;

  public static final String DEFAULT_ADMIN_HOST = "localhost";
  public static final String DEFAULT_ADMIN_PORT = "16010";
  
  public static final String PROP_JNDI_FACTORY = "java.naming.factory.initial";
  public static final String PROP_JNDI_HOST = "java.naming.factory.host";
  public static final String PROP_JNDI_PORT = "java.naming.factory.port";
  
  public static final String DEFAULT_JNDI_FACTORY = "fr.dyade.aaa.jndi2.client.NamingContextFactory";
  public static final String DEFAULT_JNDI_HOST = "localhost";
  public static final String DEFAULT_JNDI_PORT = "16400";

  private static final String STR_ADMIN_DISCONNECTED = "Disconnected";
  private static final String STR_JNDI_DISCONNECTED = "Disconnected";

  public AdminController()
  {
    admin = new AdminImpl();
    monitor = new MonitorImpl();
    
    adminRoot = new DefaultMutableTreeNode(STR_ADMIN_DISCONNECTED);
    adminTreeModel = new DefaultTreeModel(adminRoot, true);

    jndiRoot = new DefaultMutableTreeNode(STR_JNDI_DISCONNECTED);
    jndiTreeModel = new DefaultTreeModel(jndiRoot, true);
  }

  public void setControllerEventListener(ControllerEventListener l)
  {
  	this.gui = l;
  }

  protected DefaultTreeModel getAdminTreeModel() { return adminTreeModel; }
  
  protected DefaultTreeModel getJndiTreeModel() { return jndiTreeModel; }

  public void connectJndi(String host, int port, String ctxName) throws NamingException
  {
  	namedCtx = ctxName;
  	
  	Hashtable env = new Hashtable();
  	env.put(PROP_JNDI_FACTORY,
  	  System.getProperty(PROP_JNDI_FACTORY) != null ? System.getProperty(PROP_JNDI_FACTORY) : DEFAULT_JNDI_FACTORY);
  	env.put(PROP_JNDI_HOST, host);
  	env.put(PROP_JNDI_PORT, Integer.toString(port));
  	
  	ctx = new InitialContext(env);
    jndiConnected = true;

    jndiRoot.setUserObject((ctxName == null || ctxName.length() == 0) ? "Root Context" : ctxName);
    jndiTreeModel.nodeChanged(jndiRoot);
  	refreshJndiData();
  }

  public void refreshJndiData() throws NamingException
  {
    cleanupJndiTree();
 
  	for (NamingEnumeration e = ctx.list(namedCtx); e.hasMore();) {
  	  NameClassPair pair = (NameClassPair) e.next();
      JndiTreeNode node = new JndiTreeNode(this, ctx, pair.getName());
  	  insertJndiNode(node);
  	}
  }

  public void disconnectJndi() throws NamingException
  {
    ctx.close();

    jndiRoot.setUserObject(STR_JNDI_DISCONNECTED);
    jndiTreeModel.nodeChanged(jndiRoot);
    cleanupJndiTree();

    ctx = null;
    jndiConnected = false;
  }

  public JndiTreeNode findJndiNodeByName(String name)
  {
    int i;
    for (i = 0; i < jndiRoot.getChildCount(); i++) {
      JndiTreeNode curr = (JndiTreeNode) jndiRoot.getChildAt(i);
      if (name.equals(curr.getName()))
        return curr;
    }

    return null;
  }

  public void connectAdmin(String host, int port, String user, String passwd) throws Exception
  {
  	try
  	{
  	  disconnectAdmin();
  	}
  	catch (Exception exc) {}

  	admin.connect(host, port, user, passwd, 30);
  	monitor.connect(host, port, user, passwd, 30);
  	adminConnected = true;
  	adminConnectionStr = "//" + host + ":" + port;

    adminRoot.setUserObject("Active Config");
    adminTreeModel.nodeChanged(adminRoot);
    refreshAdminData();
  	gui.adminControllerEvent(new ControllerEvent(ControllerEvent.ADMIN_CONNECTED));
  }

  public void refreshAdminData() throws ConnectException
  {
    cleanupAdminTree();

    List destList = null;
    List userList = null;

		List servers = null;
		try {
			servers = monitor.getServersIds();
		}
		catch (Exception exc) {
			System.err.println("Failed to get server list: " + exc);
			return;
		}

		for (Iterator it = servers.iterator(); it.hasNext();) {
			int server = ((Short) it.next()).intValue();		

      try {
        destList = monitor.getDestinations(server);
        userList = monitor.getUsers(server);
      }
      catch (AdminException exc) {
        System.err.println("Failed to get user and destination lists for server #" +
        	server + ": " + exc);
        break;
      }

      ServerTreeNode serverNode = new ServerTreeNode(this, server);
      adminTreeModel.insertNodeInto(serverNode, adminRoot, adminRoot.getChildCount());

      for (Iterator i = destList.iterator(); i.hasNext();) {
        Destination dest = (Destination) i.next();
        DestinationTreeNode destNode = new DestinationTreeNode(this, dest);
        adminTreeModel.insertNodeInto(destNode, serverNode.getDestinationRoot(),
                                      serverNode.getDestinationRoot().getChildCount());
      }

      for (Iterator i = userList.iterator(); i.hasNext();) {
        User user = (User) i.next();
        UserTreeNode userNode = new UserTreeNode(this, user);
        adminTreeModel.insertNodeInto(userNode, serverNode.getUserRoot(),
                                      serverNode.getUserRoot().getChildCount());
      }
    }
}

  public void disconnectAdmin() throws Exception
  {
  	if (adminConnected) {
      admin.disconnect();
      monitor.disconnect();
  	}

    adminRoot.setUserObject(STR_ADMIN_DISCONNECTED);
    adminTreeModel.nodeChanged(adminRoot);
    cleanupAdminTree();

    adminConnected = false;
    adminConnectionStr = "Not connected";
  	gui.adminControllerEvent(new ControllerEvent(ControllerEvent.ADMIN_DISCONNECTED));
  }

	public void stopServer(ServerTreeNode stn) throws Exception
	{
		admin.stopServer(stn.getServerId());
		adminTreeModel.removeNodeFromParent(stn);
	}

  public void createConnectionFactory(String host, int port,
    String name, String type) throws Exception
  {
    try
    {
      if (ctx.lookup(name) != null)
        throw new Exception("Name already bound in JNDI context");
    }
    catch (NameNotFoundException exc) {}

  	Object factory = null;

  	if ("CF".equals(type))
  	  factory = admin.createConnectionFactory(host, port);
  	if ("QCF".equals(type))
  	  factory = admin.createQueueConnectionFactory(host, port);
  	if ("TCF".equals(type))
  	  factory = admin.createTopicConnectionFactory(host, port);
  	if ("XCF".equals(type))
  	  factory = admin.createXAConnectionFactory(host, port);
  	if ("XQCF".equals(type))
  	  factory = admin.createXAQueueConnectionFactory(host, port);
  	if ("XTCF".equals(type))
  	  factory = admin.createXATopicConnectionFactory(host, port);

    ctx.bind(name, factory);

    JndiTreeNode node = new JndiTreeNode(this, ctx, new Binding(name, factory));
    insertJndiNode(node);
  }

  public void createDestination(ServerTreeNode serverNode, String name, String type) throws Exception
  {
    try
    {
      if (ctx.lookup(name) != null)
        throw new Exception("Name already bound in JNDI context");
    }
    catch (NameNotFoundException exc) {}

  	Destination dest = null;

  	if ("Q".equals(type))
  	  dest = admin.createQueue(serverNode.getServerId());
  	if ("T".equals(type))
  	  dest = admin.createTopic(serverNode.getServerId());
  	if ("DMQ".equals(type))
  	  dest = admin.createDeadMQueue(serverNode.getServerId());

    ctx.bind(name, dest);

    JndiTreeNode node = new JndiTreeNode(this, ctx, new Binding(name, dest));
    insertJndiNode(node);

    DestinationTreeNode destNode = new DestinationTreeNode(this, dest);
    adminTreeModel.insertNodeInto(destNode, serverNode.getDestinationRoot(),
                                  serverNode.getDestinationRoot().getChildCount());
  }

  public void deleteObject(JndiTreeNode node) throws Exception
  {
  	String name = node.getName();
  	Object obj = ctx.lookup(name);

    try
    {
      Destination dest = (Destination) obj;
      admin.deleteDestination(dest);

      DestinationTreeNode dtn = findDestinationNode(adminRoot, dest);
      if (dtn != null)
      	adminTreeModel.removeNodeFromParent(findDestinationNode(adminRoot, dest));
    }
    catch (ClassCastException cce) {}

    ctx.unbind(name);

    jndiTreeModel.removeNodeFromParent(node);
  }

  public void createUser(ServerTreeNode serverNode, String name, String passwd) throws Exception
  {
    User user = admin.createUser(name, passwd, serverNode.getServerId());
    UserTreeNode userNode = new UserTreeNode(this, user);
    adminTreeModel.insertNodeInto(userNode, serverNode.getUserRoot(),
                                  serverNode.getUserRoot().getChildCount());
  }

  public void updateUser(UserTreeNode userNode, String name, String passwd) throws Exception
  {
    admin.updateUser(userNode.getUser(), name, passwd);
    adminTreeModel.nodeChanged(userNode);
  }

  public void deleteUser(UserTreeNode node) throws Exception
  {
    User user = node.getUser();
    admin.deleteUser(user);

    adminTreeModel.removeNodeFromParent(node);
  }

  public int getPendingMessages(Queue q) throws Exception
  {
  	return monitor.getPendingMessages(q);
  }

  public int getPendingRequests(Queue q) throws Exception
  {
    return monitor.getPendingRequests(q);
  }

  public int getSubscriptions(Topic t) throws Exception
  {
    return monitor.getSubscriptions(t);
  }

  public int getDefaultThreshold(int serverId) throws Exception
  {
    return monitor.getDefaultThreshold(serverId);
  }

  public void setDefaultThreshold(int serverId, int threshold) throws Exception
  {
    admin.setDefaultThreshold(serverId, threshold);
  }

  public DeadMQueue getDefaultDMQ(int serverId) throws Exception
  {
    return monitor.getDefaultDMQ(serverId);
  }

  public void setDefaultDMQ(int serverId, DeadMQueue dmq) throws Exception
  {
    admin.setDefaultDMQ(serverId, dmq);
  }

  public void unsetDefaultThreshold(int serverId) throws Exception
  {
    admin.unsetDefaultThreshold(serverId);
  }

  public void unsetDefaultDMQ(int serverId) throws Exception
  {
    admin.unsetDefaultDMQ(serverId);
  }

  public int getUserThreshold(User user) throws Exception
  {
    return monitor.getThreshold(user);
  }

  public void setUserThreshold(User user, int threshold) throws Exception
  {
    admin.setUserThreshold(user, threshold);
  }

  public DeadMQueue getUserDMQ(User user) throws Exception
  {
    return monitor.getDMQ(user);
  }

  public void setUserDMQ(User user, DeadMQueue dmq) throws Exception
  {
    admin.setUserDMQ(user, dmq);
  }

  public void unsetUserThreshold(User user) throws Exception
  {
    admin.unsetUserThreshold(user);
  }

  public void unsetUserDMQ(User user) throws Exception
  {
    admin.unsetUserDMQ(user);
  }

  public int getQueueThreshold(Queue queue) throws Exception
  {
    return monitor.getThreshold(queue);
  }

  public void setQueueThreshold(Queue queue, int threshold) throws Exception
  {
    admin.setQueueThreshold(queue, threshold);
  }

  public DeadMQueue getDestinationDMQ(Destination dest) throws Exception
  {
    return monitor.getDMQ(dest);
  }

  public void setDestinationDMQ(Destination dest, DeadMQueue dmq) throws Exception
  {
    admin.setDestinationDMQ(dest, dmq);
  }

  public void unsetQueueThreshold(Queue queue) throws Exception
  {
    admin.unsetQueueThreshold(queue);
  }

  public void unsetDestinationDMQ(Destination dest) throws Exception
  {
    admin.unsetDestinationDMQ(dest);
  }

  public boolean isFreelyReadable(Destination dest) throws Exception
  {
    return monitor.freelyReadable(dest);
  }

  public boolean isFreelyWritable(Destination dest) throws Exception
  {
    return monitor.freelyWriteable(dest);
  }

  public void setFreeReading(Destination dest) throws Exception
  {
    admin.setFreeReading(dest);
  }

  public void setFreeWriting(Destination dest) throws Exception
  {
    admin.setFreeWriting(dest);
  }

  public void unsetFreeReading(Destination dest) throws Exception
  {
    admin.unsetFreeReading(dest);
  }

  public void unsetFreeWriting(Destination dest) throws Exception
  {
    admin.unsetFreeWriting(dest);
  }

	public List getAuthorizedReaders(Destination dest) throws Exception
	{
		return monitor.getReaders(dest);
	}

	public List getAuthorizedWriters(Destination dest) throws Exception
	{
		return monitor.getWriters(dest);
	}

	public void setReader(User user, Destination dest) throws Exception
	{
		admin.setReader(user, dest);
	}

	public void setWriter(User user, Destination dest) throws Exception
	{
		admin.setWriter(user, dest);
	}

	public void unsetReader(User user, Destination dest) throws Exception
	{
		admin.unsetReader(user, dest);
	}

	public void unsetWriter(User user, Destination dest) throws Exception
	{
		admin.unsetWriter(user, dest);
	}

  public String getAdminConnectionStatus() { return adminConnectionStr; }

  public boolean isAdminConnected() { return adminConnected; }

  public boolean isJndiConnected() { return jndiConnected; }

  private void cleanupAdminTree()
  {
  	while (adminRoot.getChildCount() > 0)
  	  adminTreeModel.removeNodeFromParent((MutableTreeNode) adminRoot.getChildAt(0));
  }
  
  private void cleanupJndiTree()
  {
  	while (jndiRoot.getChildCount() > 0)
  	  jndiTreeModel.removeNodeFromParent((MutableTreeNode) jndiRoot.getChildAt(0));
  }

  private void insertJndiNode(JndiTreeNode n)
  {
  	int i;
  	for (i = 0; i < jndiRoot.getChildCount(); i++) {
  	  JndiTreeNode curr = (JndiTreeNode) jndiRoot.getChildAt(i);
  	  if (n.getName().compareTo(curr.getName()) < 0)
  	    break;
  	}

    jndiTreeModel.insertNodeInto(n, jndiRoot, i);
  }

	private DestinationTreeNode findDestinationNode(TreeNode from, Destination dest) {
		for (int i = 0; i < from.getChildCount(); i++) {
			TreeNode current = from.getChildAt(i);
			try {
				DestinationTreeNode dtn = (DestinationTreeNode) current;
				if (dtn.getDestination().equals(dest))
					return dtn;
			}
			catch (ClassCastException exc) {
				if (current.getChildCount() > 0)
					return findDestinationNode(current, dest);
			}
		}

		return null;
	}

	protected String findDestinationJndiName(Destination dest) {
		for (int i = 0; i < jndiRoot.getChildCount(); i++) {
			JndiTreeNode current = (JndiTreeNode) jndiRoot.getChildAt(i);
			Object obj = current.getObject();
			if (obj instanceof Destination && ((Destination) obj).equals(dest))
				return current.getName();
		}

		return null;
	}

  protected void notifyListener(ControllerEvent e)
  {
  	if (gui != null)
  	  gui.adminControllerEvent(e);
  }
}
