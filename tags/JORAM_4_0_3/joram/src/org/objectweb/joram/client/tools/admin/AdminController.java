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

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.tcp.*;


public class AdminController
{
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

  	AdminModule.connect(host, port, user, passwd, 30);
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
			servers = AdminModule.getServersIds();
		}
		catch (Exception exc) {
			System.err.println("Failed to get server list: " + exc);
			return;
		}

		for (Iterator it = servers.iterator(); it.hasNext();) {
			int server = ((Short) it.next()).intValue();		

      try {
        destList = AdminModule.getDestinations(server);
        userList = AdminModule.getUsers(server);
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
      AdminModule.disconnect();
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
		AdminModule.stopServer(stn.getServerId());
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
  	  factory = TcpConnectionFactory.create(host, port);
  	if ("QCF".equals(type))
  	  factory = QueueTcpConnectionFactory.create(host, port);
  	if ("TCF".equals(type))
  	  factory = TopicTcpConnectionFactory.create(host, port);
  	if ("XCF".equals(type))
  	  factory = XATcpConnectionFactory.create(host, port);
  	if ("XQCF".equals(type))
  	  factory = XAQueueTcpConnectionFactory.create(host, port);
  	if ("XTCF".equals(type))
  	  factory = XATopicTcpConnectionFactory.create(host, port);

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
  	  dest = org.objectweb.joram.client.jms.Queue.create(serverNode.getServerId());
  	if ("T".equals(type))
  	  dest = org.objectweb.joram.client.jms.Topic.create(serverNode.getServerId());
  	if ("DMQ".equals(type))
  	  dest = org.objectweb.joram.client.jms.admin.DeadMQueue.create(serverNode.getServerId());

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
      org.objectweb.joram.client.jms.Destination dest = 
        (org.objectweb.joram.client.jms.Destination) obj;
      dest.delete();

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
    User user = User.create(name, passwd, serverNode.getServerId());
    UserTreeNode userNode = new UserTreeNode(this, user);
    adminTreeModel.insertNodeInto(userNode, serverNode.getUserRoot(),
                                  serverNode.getUserRoot().getChildCount());
  }

  public void updateUser(UserTreeNode userNode, String name, String passwd) throws Exception
  {
    userNode.getUser().update(name, passwd);
    adminTreeModel.nodeChanged(userNode);
  }

  public void deleteUser(UserTreeNode node) throws Exception
  {
    node.getUser().delete();

    adminTreeModel.removeNodeFromParent(node);
  }

  public int getPendingMessages(Queue q) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Queue) q).getPendingMessages();
  }

  public int getPendingRequests(Queue q) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Queue) q).getPendingRequests();
  }

  public int getSubscriptions(Topic t) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Topic) t).getSubscriptions();
  }

  public int getDefaultThreshold(int serverId) throws Exception
  {
    return AdminModule.getDefaultThreshold(serverId);
  }

  public void setDefaultThreshold(int serverId, int threshold) throws Exception
  {
    AdminModule.setDefaultThreshold(serverId, threshold);
  }

  public DeadMQueue getDefaultDMQ(int serverId) throws Exception
  {
    return AdminModule.getDefaultDMQ(serverId);
  }

  public void setDefaultDMQ(int serverId, DeadMQueue dmq) throws Exception
  {
    AdminModule.setDefaultDMQ(serverId, dmq);
  }

  public void unsetDefaultThreshold(int serverId) throws Exception
  {
    AdminModule.setDefaultThreshold(serverId, -1);
  }

  public void unsetDefaultDMQ(int serverId) throws Exception
  {
    AdminModule.setDefaultDMQ(serverId, null);
  }

  public int getUserThreshold(User user) throws Exception
  {
    return user.getThreshold();
  }

  public void setUserThreshold(User user, int threshold) throws Exception
  {
    user.setThreshold(threshold);
  }

  public DeadMQueue getUserDMQ(User user) throws Exception
  {
    return user.getDMQ();
  }

  public void setUserDMQ(User user, DeadMQueue dmq) throws Exception
  {
    user.setDMQ(dmq);
  }

  public void unsetUserThreshold(User user) throws Exception
  {
    user.setThreshold(-1);
  }

  public void unsetUserDMQ(User user) throws Exception
  {
    user.setDMQ(null);
  }

  public int getQueueThreshold(Queue queue) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Queue) queue).getThreshold();
  }

  public void setQueueThreshold(Queue queue, int threshold) throws Exception
  {
    ((org.objectweb.joram.client.jms.Queue) queue).setThreshold(threshold);
  }

  public DeadMQueue getDestinationDMQ(Destination dest) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Destination) dest).getDMQ();
  }

  public void setDestinationDMQ(Destination dest, DeadMQueue dmq) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).setDMQ(dmq);
  }

  public void unsetQueueThreshold(Queue queue) throws Exception
  {
    ((org.objectweb.joram.client.jms.Queue) queue).setThreshold(-1);
  }

  public void unsetDestinationDMQ(Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).setDMQ(null);
  }

  public boolean isFreelyReadable(Destination dest) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Destination) dest).isFreelyReadable();
  }

  public boolean isFreelyWritable(Destination dest) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Destination) dest).isFreelyWriteable();
  }

  public void setFreeReading(Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).setFreeReading();
  }

  public void setFreeWriting(Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).setFreeWriting();
  }

  public void unsetFreeReading(Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).unsetFreeReading();
  }

  public void unsetFreeWriting(Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).unsetFreeWriting();
  }

  public List getAuthorizedReaders(Destination dest) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Destination) dest).getReaders();
  }

  public List getAuthorizedWriters(Destination dest) throws Exception
  {
    return ((org.objectweb.joram.client.jms.Destination) dest).getWriters();
  }

  public void setReader(User user, Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).setReader(user);
  }

  public void setWriter(User user, Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).setWriter(user);
  }

  public void unsetReader(User user, Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).unsetReader(user);
  }

  public void unsetWriter(User user, Destination dest) throws Exception
  {
    ((org.objectweb.joram.client.jms.Destination) dest).unsetWriter(user);
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