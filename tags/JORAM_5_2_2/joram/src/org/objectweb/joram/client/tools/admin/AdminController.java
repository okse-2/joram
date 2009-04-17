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

import java.net.ConnectException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XAQueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.XATopicTcpConnectionFactory;
import org.objectweb.util.monolog.api.BasicLevel;

public class AdminController {

  private boolean adminConnected = false;
  private String adminConnectionStr = "Not connected";

  private ControllerEventListener gui;

  private DefaultTreeModel adminTreeModel;
  private PlatformTreeNode adminRoot;

  private DefaultTreeModel jndiTreeModel;
  private MutableTreeNode jndiRoot;

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

  public AdminController() {
    adminRoot = new PlatformTreeNode(this, STR_ADMIN_DISCONNECTED);
    adminTreeModel = new DefaultTreeModel(adminRoot, true);

    jndiRoot = new DefaultMutableTreeNode(STR_JNDI_DISCONNECTED);
    jndiTreeModel = new DefaultTreeModel(jndiRoot, true);
  }

  public void setControllerEventListener(ControllerEventListener l) {
    this.gui = l;
  }

  protected DefaultTreeModel getAdminTreeModel() {
    return adminTreeModel;
  }

  protected DefaultTreeModel getJndiTreeModel() {
    return jndiTreeModel;
  }

  public void connectJndi(String host, int port, String ctxName) throws NamingException {

    Hashtable env = new Hashtable();
    env.put(PROP_JNDI_FACTORY, System.getProperty(PROP_JNDI_FACTORY) != null ?
        System.getProperty(PROP_JNDI_FACTORY) : DEFAULT_JNDI_FACTORY);
    env.put(PROP_JNDI_HOST, host);
    env.put(PROP_JNDI_PORT, Integer.toString(port));

    ctx = new InitialContext(env);
    jndiConnected = true;
    if (ctxName != null) {
      ctx = (Context) ctx.lookup(ctxName);
    }
    jndiRoot.setUserObject(ctxName == null || ctxName.length() == 0 ? "Root Context" : ctxName);
    jndiTreeModel.nodeChanged(jndiRoot);
    refreshJndiData();
  }

  public void refreshJndiData() throws NamingException {
    cleanupJndiTree();

    for (NamingEnumeration e = ctx.list(""); e.hasMore();) {
      NameClassPair pair = (NameClassPair) e.next();
      JndiTreeNode node = new JndiTreeNode(this, ctx, pair.getName());
      insertJndiNode(node);
    }
  }

  public void disconnectJndi() throws NamingException {
    ctx.close();

    jndiRoot.setUserObject(STR_JNDI_DISCONNECTED);
    jndiTreeModel.nodeChanged(jndiRoot);
    cleanupJndiTree();

    ctx = null;
    jndiConnected = false;
  }

  public JndiTreeNode findJndiNodeByName(String name) {
    int i;
    for (i = 0; i < jndiRoot.getChildCount(); i++) {
      JndiTreeNode curr = (JndiTreeNode) jndiRoot.getChildAt(i);
      if (name.equals(curr.getName()))
        return curr;
    }

    return null;
  }

  public void connectAdmin(final String host, final int port, final String user, final String passwd)
      throws Exception {
    try {
      disconnectAdmin();
    } catch (Exception exc) {
    }

    AdminModule.connect(host, port, user, passwd, 4);
    adminConnected = true;
    adminConnectionStr = "//" + host + ":" + port;
    adminRoot.setUserObject("Active Config");
    adminTreeModel.nodeChanged(adminRoot);

    AdminTool.invokeLater(new CommandWorker() {
      public void run() throws Exception {
        refreshAdminData();
        gui.adminControllerEvent(new ControllerEvent(ControllerEvent.ADMIN_CONNECTED));
      }
    });
  }

  /**
   * First refreshing step. Doesn't block.
   */
  private void refreshAdminData1(ServerTreeNode serverTreeNode) throws ConnectException, AdminException {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "AdminController.refreshAdminData(" + serverTreeNode + ')');
    String[] domainNames = AdminModule.getDomainNames(serverTreeNode.getServerId());
    TreeNode parentTreeNode = serverTreeNode.getParent();
    String parentDomainName = null;
    if (parentTreeNode instanceof DomainTreeNode) {
      DomainTreeNode dtn = (DomainTreeNode) parentTreeNode;
      parentDomainName = dtn.getDomainName();
    }
    for (int i = 0; i < domainNames.length; i++) {
      if (!domainNames[i].equals(parentDomainName)) {
        DomainTreeNode dtn = new DomainTreeNode(this, domainNames[i]);
        adminTreeModel.insertNodeInto(dtn, serverTreeNode.getDomainRoot(), serverTreeNode.getDomainRoot()
            .getChildCount());

        Server[] servers = AdminModule.getServers(domainNames[i]);
        for (int j = 0; j < servers.length; j++) {
          if (servers[j].getId() != serverTreeNode.getServerId()) {
            ServerTreeNode stn = new ServerTreeNode(this, servers[j]);
            adminTreeModel.insertNodeInto(stn, dtn, dtn.getChildCount());
            refreshAdminData1(stn);
          }
        }
      }
    }
  }

  void updateDestinations(int serverId, MutableTreeNode destinationRoot) throws ConnectException,
      AdminException {
    List destList = AdminModule.getDestinationsList(serverId);
    for (Iterator i = destList.iterator(); i.hasNext();) {
      Destination dest = (Destination) i.next();
      DestinationTreeNode destNode;
      if (dest instanceof Topic) {
        destNode = new TopicTreeNode(this, (Topic) dest);
      } else if (dest instanceof Queue) {
        destNode = new QueueTreeNode(this, (Queue) dest);
      } else if (dest instanceof TemporaryQueue) {
        destNode = new TopicTreeNode(this, (Topic) dest);
      } else if (dest instanceof TemporaryTopic) {
        destNode = new QueueTreeNode(this, (Queue) dest);
      } else {
        destNode = new DestinationTreeNode(this, dest);
      }
      adminTreeModel.insertNodeInto(destNode, destinationRoot, destinationRoot.getChildCount());
    }
  }

  void updateUsers(int serverId, MutableTreeNode userRoot) throws ConnectException, AdminException {
    List userList = AdminModule.getUsersList(serverId);
    for (Iterator i = userList.iterator(); i.hasNext();) {
      User user = (User) i.next();
      UserTreeNode userNode = new UserTreeNode(this, user);
      adminTreeModel.insertNodeInto(userNode, userRoot, userRoot.getChildCount());
    }
  }

  /**
   * Second refreshing step. May block.
   */
  private void refreshAdminData2(ServerTreeNode serverTreeNode) throws ConnectException, AdminException {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "AdminController.refreshAdminData(" + serverTreeNode + ')');
    try {
      updateDestinations(serverTreeNode.getServerId(), serverTreeNode.getDestinationRoot());
      updateUsers(serverTreeNode.getServerId(), serverTreeNode.getUserRoot());
    } catch (AdminException exc) {
      if (Log.logger.isLoggable(BasicLevel.WARN))
        Log.logger.log(BasicLevel.WARN, "", exc);
      return;
    } catch (ConnectException ce) {
      if (Log.logger.isLoggable(BasicLevel.WARN))
        Log.logger.log(BasicLevel.WARN, "", ce);
      return;
    }

    Enumeration e = serverTreeNode.getDomainRoot().children();
    while (e.hasMoreElements()) {
      DomainTreeNode dtn = (DomainTreeNode) e.nextElement();
      Enumeration e2 = dtn.children();
      while (e2.hasMoreElements()) {
        ServerTreeNode stn = (ServerTreeNode) e2.nextElement();
        refreshAdminData2(stn);
      }
    }
  }

  public void refreshAdminData() throws ConnectException, AdminException {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, "AdminController.refreshAdminData()");
    cleanupAdminTree();

    // Get the local server id
    Server localServer = AdminModule.getLocalServer();
    ServerTreeNode localServerNode = new ServerTreeNode(this, localServer);
    adminTreeModel.insertNodeInto(localServerNode, adminRoot, adminRoot.getChildCount());

    // Recursively browse the servers configuration
    refreshAdminData1(localServerNode);
    refreshAdminData2(localServerNode);
  }

  public void disconnectAdmin() throws Exception {
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

  public void stopServer(ServerTreeNode stn) throws Exception {
    AdminModule.stopServer(stn.getServerId());
  }

  public void deleteServer(ServerTreeNode stn) throws Exception {
    AdminModule.removeServer(stn.getServerId());
    adminTreeModel.removeNodeFromParent(stn);
  }

  public void deleteDomain(DomainTreeNode dtn) throws Exception {
    AdminModule.removeDomain(dtn.getDomainName());
    adminTreeModel.removeNodeFromParent(dtn);
  }

  public void createConnectionFactory(String host, int port, String name, String type) throws Exception {
    try {
      if (ctx.lookup(name) != null)
        throw new Exception("Name already bound in JNDI context");
    } catch (NameNotFoundException exc) {
    }

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

  public void createDestination(ServerTreeNode serverNode, String name, String type) throws Exception {
    try {
      if (ctx.lookup(name) != null)
        throw new Exception("Name already bound in JNDI context");
    } catch (NameNotFoundException exc) {
    }

    Destination dest = null;

    if ("Q".equals(type))
      dest = Queue.create(serverNode.getServerId(), name);
    if ("T".equals(type))
      dest = Topic.create(serverNode.getServerId(), name);
    if ("DMQ".equals(type))
      dest = DeadMQueue.create(serverNode.getServerId(), name);

    ctx.bind(name, dest);

    JndiTreeNode node = new JndiTreeNode(this, ctx, new Binding(name, dest));
    insertJndiNode(node);

    DestinationTreeNode destNode = new DestinationTreeNode(this, dest);
    adminTreeModel.insertNodeInto(destNode, serverNode.getDestinationRoot(),
        serverNode.getDestinationRoot().getChildCount());
  }

  public void deleteObject(JndiTreeNode node) throws Exception {
    String name = node.getName();
    Object obj = ctx.lookup(name);

    try {
      Destination dest = (Destination) obj;
      dest.delete();

      DestinationTreeNode dtn = findDestinationNode(adminRoot, dest);
      if (dtn != null)
        adminTreeModel.removeNodeFromParent(findDestinationNode(adminRoot, dest));
    } catch (ClassCastException cce) {
    }

    ctx.unbind(name);

    jndiTreeModel.removeNodeFromParent(node);
  }

  public void createUser(ServerTreeNode serverNode, String name, String passwd) throws Exception {
    User user = User.create(name, passwd, serverNode.getServerId());
    UserTreeNode userNode = new UserTreeNode(this, user);
    adminTreeModel.insertNodeInto(userNode, serverNode.getUserRoot(),
        serverNode.getUserRoot().getChildCount());
  }

  public void createDomain(ServerTreeNode serverNode, String domainName, int port) throws Exception {
    AdminModule.addDomain(domainName, (short) serverNode.getServerId(), port);
    DomainTreeNode dtn = new DomainTreeNode(this, domainName);
    adminTreeModel.insertNodeInto(dtn, serverNode.getDomainRoot(),
        serverNode.getDomainRoot().getChildCount());
  }

  public void updateUser(UserTreeNode userNode, String name, String passwd) throws Exception {
    userNode.getUser().update(name, passwd);
    adminTreeModel.nodeChanged(userNode);
  }

  public void deleteUser(UserTreeNode node) throws Exception {
    node.getUser().delete();
    adminTreeModel.removeNodeFromParent(node);
  }

  public void deleteMessage(MessageTreeNode msgTn) throws Exception {
    DefaultMutableTreeNode parentTn = (DefaultMutableTreeNode) msgTn.getParent();
    if (parentTn instanceof SubscriptionTreeNode) {
      SubscriptionTreeNode subTn = (SubscriptionTreeNode) parentTn;
      SubscriptionRootTreeNode subRootTn = (SubscriptionRootTreeNode) subTn.getParent();
      UserTreeNode userTn = (UserTreeNode) subRootTn.getParent();
      //      ServerTreeNode serverTn = userTn.getParentServerTreeNode();
      userTn.getUser().deleteMessage(subTn.getSubscription().getName(), msgTn.getMessageId());
    } else {
      MessageRootTreeNode msgRootTn = (MessageRootTreeNode) parentTn;
      QueueTreeNode queueTreeNode = (QueueTreeNode) msgRootTn.getParent();
      queueTreeNode.getQueue().deleteMessage(msgTn.getMessageId());
    }
    adminTreeModel.removeNodeFromParent(msgTn);
  }

  public void clearSubscription(SubscriptionTreeNode subTn) throws Exception {
    SubscriptionRootTreeNode subRootTn = (SubscriptionRootTreeNode) subTn.getParent();
    UserTreeNode userTn = (UserTreeNode) subRootTn.getParent();
    //    ServerTreeNode serverTn = userTn.getParentServerTreeNode();
    userTn.getUser().clearSubscription(subTn.getSubscription().getName());
    while (subTn.getChildCount() > 0) {
      MessageTreeNode msgTn = (MessageTreeNode) subTn.getChildAt(0);
      adminTreeModel.removeNodeFromParent(msgTn);
    }
  }

  public void clearQueue(QueueTreeNode queueTn) throws Exception {
    queueTn.getQueue().clear();
    MessageRootTreeNode msgRootTn = queueTn.getMessageRootTreeNode();
    while (msgRootTn.getChildCount() > 0) {
      MessageTreeNode msgTn = (MessageTreeNode) msgRootTn.getChildAt(0);
      adminTreeModel.removeNodeFromParent(msgTn);
    }
  }

  public int getPendingMessages(Queue q) throws Exception {
    return q.getPendingMessages();
  }

  public int getPendingRequests(Queue q) throws Exception {
    return q.getPendingRequests();
  }

  public int getSubscriptions(Topic t) throws Exception {
    return t.getSubscriptions();
  }

  public int getDefaultThreshold(int serverId) throws Exception {
    return AdminModule.getDefaultThreshold(serverId);
  }

  public void setDefaultThreshold(int serverId, int threshold) throws Exception {
    AdminModule.setDefaultThreshold(serverId, threshold);
  }

  public DeadMQueue getDefaultDMQ(int serverId) throws Exception {
    return AdminModule.getDefaultDMQ(serverId);
  }

  public void setDefaultDMQ(int serverId, DeadMQueue dmq) throws Exception {
    AdminModule.setDefaultDMQ(serverId, dmq);
  }

  public void unsetDefaultThreshold(int serverId) throws Exception {
    AdminModule.setDefaultThreshold(serverId, -1);
  }

  public void unsetDefaultDMQ(int serverId) throws Exception {
    AdminModule.setDefaultDMQ(serverId, null);
  }

  public int getUserThreshold(User user) throws Exception {
    return user.getThreshold();
  }

  public void setUserThreshold(User user, int threshold) throws Exception {
    user.setThreshold(threshold);
  }

  public DeadMQueue getUserDMQ(User user) throws Exception {
    return user.getDMQ();
  }

  public void setUserDMQ(User user, DeadMQueue dmq) throws Exception {
    user.setDMQ(dmq);
  }

  public void unsetUserThreshold(User user) throws Exception {
    user.setThreshold(-1);
  }

  public void unsetUserDMQ(User user) throws Exception {
    user.setDMQ(null);
  }

  public int getQueueThreshold(Queue queue) throws Exception {
    return queue.getThreshold();
  }

  public void setQueueThreshold(Queue queue, int threshold) throws Exception {
    queue.setThreshold(threshold);
  }

  public DeadMQueue getDestinationDMQ(Destination dest) throws Exception {
    return dest.getDMQ();
  }

  public void setDestinationDMQ(Destination dest, DeadMQueue dmq) throws Exception {
    dest.setDMQ(dmq);
  }

  public void unsetQueueThreshold(Queue queue) throws Exception {
    queue.setThreshold(-1);
  }

  public void unsetDestinationDMQ(Destination dest) throws Exception {
    dest.setDMQ(null);
  }

  public boolean isFreelyReadable(Destination dest) throws Exception {
    return dest.isFreelyReadable();
  }

  public boolean isFreelyWritable(Destination dest) throws Exception {
    return dest.isFreelyWriteable();
  }

  public void setFreeReading(Destination dest) throws Exception {
    dest.setFreeReading();
  }

  public void setFreeWriting(Destination dest) throws Exception {
    dest.setFreeWriting();
  }

  public void unsetFreeReading(Destination dest) throws Exception {
    dest.unsetFreeReading();
  }

  public void unsetFreeWriting(Destination dest) throws Exception {
    dest.unsetFreeWriting();
  }

  public List getAuthorizedReaders(Destination dest) throws Exception {
    return dest.getReaders();
  }

  public List getAuthorizedWriters(Destination dest) throws Exception {
    return dest.getWriters();
  }

  public void setReader(User user, Destination dest) throws Exception {
    dest.setReader(user);
  }

  public void setWriter(User user, Destination dest) throws Exception {
    dest.setWriter(user);
  }

  public void unsetReader(User user, Destination dest) throws Exception {
    dest.unsetReader(user);
  }

  public void unsetWriter(User user, Destination dest) throws Exception {
    dest.unsetWriter(user);
  }

  public String getAdminConnectionStatus() {
    return adminConnectionStr;
  }

  public boolean isAdminConnected() {
    return adminConnected;
  }

  public boolean isJndiConnected() {
    return jndiConnected;
  }

  private void cleanupAdminTree() {
    while (adminRoot.getChildCount() > 0)
      adminTreeModel.removeNodeFromParent((MutableTreeNode) adminRoot.getChildAt(0));
  }

  private void cleanupJndiTree() {
    while (jndiRoot.getChildCount() > 0)
      jndiTreeModel.removeNodeFromParent((MutableTreeNode) jndiRoot.getChildAt(0));
  }

  private void insertJndiNode(JndiTreeNode n) {
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
      } catch (ClassCastException exc) {
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
      if (obj instanceof Destination && obj.equals(dest))
        return current.getName();
    }

    return null;
  }

  protected void notifyListener(ControllerEvent e) {
    if (gui != null)
      gui.adminControllerEvent(e);
  }

}
