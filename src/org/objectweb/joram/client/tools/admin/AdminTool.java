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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ConnectException;

import javax.jms.Message;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.util.monolog.api.BasicLevel;

public class AdminTool extends JFrame
    implements ControllerEventListener
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static AdminTool adminTool = null;

  private final AdminController c;

  private final JTree configTree;
  private final JTree jndiTree;
  private final JTabbedPane tabbedPane;
  private final JEditorPane msgPane;
  private final ServerPanel serverPanel;
  private final UserPanel userPanel;
  private final DestinationPanel destPanel;
  private final SubscriptionPanel subscriptionPanel;
  private final MessagePanel messagePanel;
  private final JPanel editPanel;
  private final JSplitPane splitter;
  private final JLabel connStatus;

  private final Action exitAction, adminConnectAction, adminDisconnectAction, adminRefreshAction,
    jndiConnectAction, jndiDisconnectAction, jndiRefreshAction, jndiCreateFactoryAction;

  public AdminTool(final AdminController c)
    {
      super("JORAM Administration Tool");
      this.c = c;

      ConnectAdminDialog.initialize(this);
      ConnectJndiDialog.initialize(this);
      CreateFactoryDialog.initialize(this);
      CreateUserDialog.initialize(this);
      CreateDestinationDialog.initialize(this);
      CreateServerDialog.initialize(this);
      CreateDomainDialog.initialize(this);

      jndiConnectAction = new JndiConnectAction();
      jndiDisconnectAction = new JndiDisconnectAction();
      jndiDisconnectAction.setEnabled(false);
      jndiRefreshAction = new JndiRefreshAction();
      jndiRefreshAction.setEnabled(false);
      jndiCreateFactoryAction = new JndiCreateFactoryAction();
      jndiCreateFactoryAction.setEnabled(false);

      adminConnectAction = new AdminConnectAction();
      adminDisconnectAction = new AdminDisconnectAction();
      adminDisconnectAction.setEnabled(false);
      adminRefreshAction = new AdminRefreshAction();
      adminRefreshAction.setEnabled(false);
      exitAction = new ExitAction();
    
      // Build the configuration tree
      configTree = new JTree(c.getAdminTreeModel());
      configTree.expandRow(0);
      configTree.setScrollsOnExpand(true);
      configTree.setCellRenderer(new ConfigTreeCellRenderer());

      final TreeSelectionModel configTsm = new DefaultTreeSelectionModel();
      configTsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      configTree.setSelectionModel(configTsm);

      // Build the JNDI tree
      jndiTree = new JTree(c.getJndiTreeModel());
      jndiTree.expandRow(0);
      jndiTree.setScrollsOnExpand(true);
      jndiTree.setCellRenderer(new ConfigTreeCellRenderer());

      final TreeSelectionModel jndiTsm = new DefaultTreeSelectionModel();
      jndiTsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      jndiTree.setSelectionModel(jndiTsm);

      tabbedPane = new JTabbedPane();
      tabbedPane.add("Configuration", new JScrollPane(configTree));
      tabbedPane.add("JNDI", new JScrollPane(jndiTree));

      JPanel statusPanel = new JPanel();
      statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
      JLabel statusTitle = new JLabel("Admin connection: ");
      statusPanel.add(statusTitle);
      connStatus = new JLabel("Not connected");
      connStatus.setForeground(AdminToolConstants.COLOR_DISCONNECTED);
      statusPanel.add(connStatus);
      statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      msgPane = new JEditorPane("text/html", "");
      msgPane.setEditable(false);

      serverPanel = new ServerPanel(c);
      userPanel = new UserPanel(c);
      destPanel = new DestinationPanel(c);
      subscriptionPanel = new SubscriptionPanel(c);
      messagePanel = new MessagePanel();

      editPanel = new JPanel(new CardLayout());
      editPanel.add(msgPane, "html");
      editPanel.add(serverPanel, "server");
      editPanel.add(userPanel, "user");
      editPanel.add(destPanel, "destination");
      editPanel.add(subscriptionPanel, "subscription");
      editPanel.add(messagePanel, "message");

      Component rightPane = new JScrollPane(editPanel);

      JPanel rightPanel = new JPanel(new BorderLayout());
      rightPanel.add(statusPanel, BorderLayout.NORTH);
      rightPanel.add(rightPane, BorderLayout.CENTER);

      splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                tabbedPane,
                                rightPanel);

      getContentPane().add(splitter, BorderLayout.CENTER);
    
      setJMenuBar(buildMenuBar());

      // whenever the tree model gets a new node, we expand the
      // view so the user can see the new node.
      c.getAdminTreeModel().addTreeModelListener(new TreeModelListener() {
          public void treeNodesRemoved(TreeModelEvent e) {}
      
          public void treeNodesChanged(TreeModelEvent e) {}
      
          public void treeStructureChanged(final TreeModelEvent e) {}
      
          public void treeNodesInserted(final TreeModelEvent e)
            {
              // make sure the new node is visible.
              SwingUtilities.invokeLater(new Runnable() {
                  public void run()
                    {
                      configTree.expandPath(e.getTreePath());
                    }
                });
            }
        });

      c.getJndiTreeModel().addTreeModelListener(new TreeModelListener() {
          public void treeNodesRemoved(TreeModelEvent e) {}
      
          public void treeNodesChanged(TreeModelEvent e) {}
      
          public void treeStructureChanged(final TreeModelEvent e) {}
      
          public void treeNodesInserted(final TreeModelEvent e)
            {
              // make sure the new node is visible.
              SwingUtilities.invokeLater(new Runnable() {
                  public void run()
                    {
                      jndiTree.expandPath(e.getTreePath());
                    }
                });
            }
        });

      // when you select something in the tree,
      // the controller's populateList method is called to
      // fill the list with messages from the given node.
      configTree.addTreeSelectionListener(new TreeSelectionListener() {
          public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
            if (Log.logger.isLoggable(BasicLevel.DEBUG))
              Log.logger.log(BasicLevel.DEBUG, 
                             "TreeSelectionListener[configTree].valueChanged(" + e + ')');
            if (!configTsm.isSelectionEmpty()) {
              TreeNode selection = (TreeNode) configTsm.getSelectionPath().getLastPathComponent();
              updateMessagePaneFromNode(selection);
            }
          }
        });

      jndiTree.addTreeSelectionListener(new TreeSelectionListener() {
          public void valueChanged(javax.swing.event.TreeSelectionEvent e) {
            if (Log.logger.isLoggable(BasicLevel.DEBUG))
              Log.logger.log(BasicLevel.DEBUG, 
                             "TreeSelectionListener[jndiTree].valueChanged(" + e + ')');
            if (!jndiTsm.isSelectionEmpty()) {
              TreeNode selection = (TreeNode) jndiTsm.getSelectionPath().getLastPathComponent();
              updateMessagePaneFromNode(selection);
            }
          }
        });

      // add popup menu listener
      MouseListener popupListener = new PopupListener();
      configTree.addMouseListener(popupListener);
      jndiTree.addMouseListener(popupListener);
    }

  private void displayError(String errorMsg) {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, 
                     "AdminTool.displayError(" + errorMsg + ')');
    msgPane.setText("<font face=Arial><br><br><br><br><br><center><b>" + 
                    errorMsg + 
                    "</b></center></font>");
    ((CardLayout) editPanel.getLayout()).show(editPanel, "html");
  }

  private JMenuBar buildMenuBar()
    {
      JMenuBar menuBar = new JMenuBar();

      // Configuration menu
      JMenu adminMenu = new JMenu("Admin");
      adminMenu.add(adminConnectAction);
      adminMenu.add(adminDisconnectAction);
      adminMenu.addSeparator();
      adminMenu.add(adminRefreshAction);
      adminMenu.addSeparator();
      adminMenu.add(exitAction);
    
      // JNDI menu
      JMenu jndiMenu = new JMenu("JNDI");
      jndiMenu.add(jndiConnectAction);
      jndiMenu.add(jndiDisconnectAction);
      jndiMenu.addSeparator();
      jndiMenu.add(jndiRefreshAction);
      jndiMenu.addSeparator();
      jndiMenu.add(jndiCreateFactoryAction);

      // add menus
      menuBar.add(adminMenu);
      menuBar.add(jndiMenu);
    
      return menuBar;
    }

  public void adminControllerEvent(ControllerEvent e)
    {
      int id = e.getId();
      if (id == ControllerEvent.ADMIN_CONNECTED) {
        connStatus.setText(c.getAdminConnectionStatus());
        connStatus.setForeground(AdminToolConstants.COLOR_CONNECTED);
      }
      else if (id == ControllerEvent.ADMIN_DISCONNECTED) {
        connStatus.setText(c.getAdminConnectionStatus());
        connStatus.setForeground(AdminToolConstants.COLOR_DISCONNECTED);
      }
    }

  // class AdminCommandCaller implements Runnable {
//     private TreeNode selection;
//     private AdminCommandDialog dlg;

//     public AdminCommandCaller(TreeNode selection,
//                               AdminCommandDialog dlg) {
//       this.selection = selection;
//       this.dlg = dlg;
//     }

//     public void run() {
//       deferredMessagePaneUpdate(selection);
//       dlg.close();
//     }
//   }

  private void updateMessagePaneFromNode(final TreeNode selection) {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, 
                     "AdminTool.updateMessagePaneFromNode(" + selection + ')');
    if (selection instanceof AdminTreeNode) {
      invokeLater(new CommandWorker() {
          public void run() {
            deferredMessagePaneUpdate(selection);
          }
        });
    }
    else if (selection instanceof AdminTreeNode) {
      msgPane.setText(((AdminTreeNode) selection).getDescription());
      ((CardLayout) editPanel.getLayout()).show(editPanel, "html");
    }
    else {    
      msgPane.setText("");
      ((CardLayout) editPanel.getLayout()).show(editPanel, "html");
    }
  }

  public static void invokeLater(final CommandWorker worker) {
    final AdminCommandDialog dlg = new AdminCommandDialog(
      getInstance(), getInstance().c);
    new Thread(new Runnable() {
        public void run() {
          try {
            worker.run();
            dlg.close();
          } catch (Exception exc) {
            if (Log.logger.isLoggable(BasicLevel.ERROR))
              Log.logger.log(BasicLevel.ERROR, "", exc);
            dlg.close();
            JOptionPane.showMessageDialog(AdminTool.getInstance(), 
                                          exc.getMessage());
          }
        }
      }).start();
    dlg.showDialog();
  }

  void updateSubscriptions(User user,
                           MutableTreeNode subRoot) 
    throws ConnectException, AdminException {
    Subscription[] subscriptions = user.getSubscriptions();
    for (int i = 0; i < subscriptions.length; i++) {
      SubscriptionTreeNode subNode = new SubscriptionTreeNode(
        c, subscriptions[i]);
      c.getAdminTreeModel().insertNodeInto(
        subNode, 
        subRoot,
        subRoot.getChildCount());
    }
  }

  private void deferredMessagePaneUpdate(TreeNode selection) {
    if (Log.logger.isLoggable(BasicLevel.DEBUG))
      Log.logger.log(BasicLevel.DEBUG, 
                     "AdminTool.deferredMessagepaneUpdate(" + selection + ')');
    try {
      if (selection instanceof ServerTreeNode) {
        ServerTreeNode stn = (ServerTreeNode) selection;
        serverPanel.setServerId(stn.getServerId());
        int threshold = c.getDefaultThreshold(stn.getServerId());
        serverPanel.setDefaultThreshold((threshold < 0 ? "" : Integer.toString(threshold)));
        serverPanel.setDMQList(stn.getDeadMessageQueues(), c.getDefaultDMQ(stn.getServerId()));
        ((CardLayout) editPanel.getLayout()).show(editPanel, "server");
        
        try {
          if (stn.getDestinationRoot().getChildCount() == 0) {
            // Means that the destinations may not have been initialized
            c.updateDestinations(stn.getServerId(),
                                 stn.getDestinationRoot());
          }
          
          if (stn.getUserRoot().getChildCount() == 0) {
            // Means that the users may not have been initialized
            c.updateUsers(stn.getServerId(),
                          stn.getUserRoot());
          }
        } catch (AdminException exc) {
          if (Log.logger.isLoggable(BasicLevel.WARN))
            Log.logger.log(BasicLevel.WARN, "", exc);
        } catch (ConnectException ce) {
          if (Log.logger.isLoggable(BasicLevel.WARN))
            Log.logger.log(BasicLevel.WARN, "", ce);
        }
      } else if (selection instanceof UserTreeNode) {
        UserTreeNode utn = (UserTreeNode) selection;
        userPanel.setUser(utn.getUser());
        int threshold = c.getUserThreshold(utn.getUser());
        userPanel.setThreshold((threshold < 0 ? "" : Integer.toString(threshold)));
        userPanel.setDMQList(utn.getParentServerTreeNode().getDeadMessageQueues(),
                             c.getUserDMQ(utn.getUser()));
        
        ((CardLayout) editPanel.getLayout()).show(editPanel, "user");
      } else if (selection instanceof DestinationTreeNode) {
        DestinationTreeNode dtn = (DestinationTreeNode) selection;
        destPanel.setDestination(dtn.getDestination());
        int threshold = -1;
        try {
          Queue q = (Queue) dtn.getDestination();
          threshold = c.getQueueThreshold(q);
          destPanel.setThresholdActive(true);
          destPanel.setPendingMessages(c.getPendingMessages(q));
          destPanel.setPendingRequests(c.getPendingRequests(q));
        }
        catch (ClassCastException cce) {
          destPanel.setThresholdActive(false);
          destPanel.setPendingMessages(-1);
          destPanel.setPendingRequests(-1);
        }
        finally {
          destPanel.setThreshold((threshold < 0 ? "" : Integer.toString(threshold)));
        }

        destPanel.setDMQList(dtn.getParentServerTreeNode().getDeadMessageQueues(),
                             c.getDestinationDMQ(dtn.getDestination()));

        destPanel.setFreeReading(c.isFreelyReadable(dtn.getDestination()));
        destPanel.setFreeWriting(c.isFreelyWritable(dtn.getDestination()));
                                
        java.util.List users = dtn.getParentServerTreeNode().getUsers();
        destPanel.setReadingACL(users, c.getAuthorizedReaders(dtn.getDestination()));
        destPanel.setWritingACL(users, c.getAuthorizedWriters(dtn.getDestination()));

        ((CardLayout) editPanel.getLayout()).show(editPanel, "destination");
      } else if (selection instanceof DestinationRootTreeNode) {
        if (selection.getChildCount() == 0) {
          // Means that the destinations may not have been initialized
          DestinationRootTreeNode drtn = (DestinationRootTreeNode)selection;
          ServerTreeNode stn = drtn.getParentServerTreeNode();
          c.updateDestinations(stn.getServerId(),
                               stn.getDestinationRoot());
        }
      } else if (selection instanceof UserRootTreeNode) {
        if (selection.getChildCount() == 0) {
          // Means that the users may not have been initialized
          UserRootTreeNode urtn = (UserRootTreeNode)selection;
          ServerTreeNode stn = urtn.getParentServerTreeNode();
          c.updateUsers(stn.getServerId(),
                        stn.getUserRoot());
        }
      } else if (selection instanceof SubscriptionRootTreeNode) {
        if (selection.getChildCount() == 0) {
          // Means that the subscriptions may not have been initialized
          SubscriptionRootTreeNode subRootTn = (SubscriptionRootTreeNode)selection;
          UserTreeNode userTn = (UserTreeNode)subRootTn.getParent();
          updateSubscriptions(userTn.getUser(),
                              subRootTn);
        }
      } else if (selection instanceof SubscriptionTreeNode) {
        SubscriptionTreeNode subTn = (SubscriptionTreeNode) selection;
        SubscriptionRootTreeNode subRootTn = 
          (SubscriptionRootTreeNode)subTn.getParent();
        UserTreeNode userTn = (UserTreeNode)subRootTn.getParent();
//         ServerTreeNode serverTn = userTn.getParentServerTreeNode();
//         subscriptionPanel.setSubscription(subtn.getSubscription());
//         subscriptionPanel.setServerId(serverTn.getServerId());
//         subscriptionPanel.setUserName(userTn.getUserName());
//         subscriptionPanel.loadMessageIds();
//         ((CardLayout) editPanel.getLayout()).show(editPanel, "subscription");
        if (subTn.getChildCount() == 0) {
          String[] ids = userTn.getUser().getMessageIds(subTn.getSubscription().getName());
          for (int i = 0; i < ids.length; i++) {
            MessageTreeNode msgNode = new MessageTreeNode(c, ids[i]);
            c.getAdminTreeModel().insertNodeInto(msgNode, subTn, subTn.getChildCount());
          }
        }
      } else if (selection instanceof MessageTreeNode) {
        Message msg;
        MessageTreeNode msgTn = (MessageTreeNode)selection;
        DefaultMutableTreeNode parentTn = 
          (DefaultMutableTreeNode)msgTn.getParent();
        if (parentTn instanceof SubscriptionTreeNode) {
          SubscriptionTreeNode subTn = 
            (SubscriptionTreeNode)parentTn;
          SubscriptionRootTreeNode subRootTn = 
            (SubscriptionRootTreeNode)subTn.getParent();
          UserTreeNode userTn = (UserTreeNode)subRootTn.getParent();
//        ServerTreeNode serverTn = userTn.getParentServerTreeNode();
          msg = userTn.getUser().getMessage(subTn.getSubscription().getName(), msgTn.getMessageId());
        } else {
          MessageRootTreeNode msgRootTn = 
            (MessageRootTreeNode)parentTn;
          QueueTreeNode queueTn = 
            (QueueTreeNode)msgRootTn.getParent();
          msg = queueTn.getQueue().getMessage(msgTn.getMessageId());
        }
        messagePanel.setMessage(msg);
        ((CardLayout) editPanel.getLayout()).show(editPanel, "message");
      } else if (selection instanceof SubscriberRootTreeNode) {
        SubscriberRootTreeNode subRootTn = 
          (SubscriberRootTreeNode)selection;
        if (subRootTn.getChildCount() == 0) {
          TopicTreeNode topicTn = (TopicTreeNode)subRootTn.getParent();
          String[] subscriberIds = 
            topicTn.getTopic().getSubscriberIds();
          for (int i = 0; i < subscriberIds.length; i++) {
            SubscriberTreeNode subTn = new SubscriberTreeNode(
              subscriberIds[i]);
            c.getAdminTreeModel().insertNodeInto(
              subTn,
              subRootTn,
              subRootTn.getChildCount());
          }
        }
      } else if (selection instanceof MessageRootTreeNode) {
        MessageRootTreeNode msgRootTn = 
          (MessageRootTreeNode)selection;
        if (msgRootTn.getChildCount() == 0) {
          QueueTreeNode queueTn = 
            (QueueTreeNode)msgRootTn.getParent();
          String[] msgIds = queueTn.getQueue().getMessageIds();
          for (int i = 0; i < msgIds.length; i++) {
            MessageTreeNode msgTn = new MessageTreeNode(
              c, msgIds[i]);
            c.getAdminTreeModel().insertNodeInto(
              msgTn,
              msgRootTn,
              msgRootTn.getChildCount());
          }
        }
      }
    } catch (Exception exc) {
      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG, "", exc);
      displayError(exc.getMessage());
    }
  }

  private class JndiConnectAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JndiConnectAction()
      {
        super("Connect...");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, KeyEvent.ALT_DOWN_MASK));
      }

    public void actionPerformed(ActionEvent e)
      {
        ConnectJndiDialog dialog;
        try
          {
   	    dialog = ConnectJndiDialog.showDialog();
          }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(AdminTool.this, exc.getMessage());
          return;
        }
                     
        if (dialog.getActionCancelled())
          return;

        try
          {
            c.connectJndi(dialog.getJndiHost(), dialog.getJndiPort(), dialog.getNamedContext());
            jndiConnectAction.setEnabled(false);
            jndiDisconnectAction.setEnabled(true);
            jndiRefreshAction.setEnabled(true);
            if (c.isAdminConnected())
              jndiCreateFactoryAction.setEnabled(true);
          }
        catch (Exception x) {
          JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
        }
      }
  }

  private class JndiDisconnectAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JndiDisconnectAction()
      {
        super("Disconnect");
      }

    public void actionPerformed(ActionEvent e)
      {
        try
          {
            c.disconnectJndi();
          }
        catch (Exception x) {
          JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
        }
        finally {
          jndiDisconnectAction.setEnabled(false);
          jndiConnectAction.setEnabled(true);
          jndiRefreshAction.setEnabled(false);
          jndiCreateFactoryAction.setEnabled(false);

          if (tabbedPane.getSelectedIndex() == 0) {
            msgPane.setText("");
            ((CardLayout) editPanel.getLayout()).show(editPanel, "html");
          }
        }
      }
  }

  private class JndiRefreshAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JndiRefreshAction()
      {
        super("Refresh", AdminToolConstants.refreshIcon);
      }

    public void actionPerformed(ActionEvent e)
      {
        try
          {
            c.refreshJndiData();
          }
        catch (Exception x) {
          JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
        }
      }
  }

  private class JndiCreateFactoryAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JndiCreateFactoryAction()
      {
        super("Create Connection Factory...");
      }

    public void actionPerformed(ActionEvent e)
      {
        try
          {
            CreateFactoryDialog dialog = CreateFactoryDialog.showDialog();
            if (!dialog.getActionCancelled())
              c.createConnectionFactory(dialog.getHost(), dialog.getPort(), dialog.getFactoryName(),
                                        dialog.getFactoryType());
          }
        catch (Exception x) {
          JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
        }
      }
  }

  private class AdminConnectAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AdminConnectAction()
      {
        super("Connect...");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK));
      }

    public void actionPerformed(ActionEvent e)
      {
        ConnectAdminDialog dialog;
        try
	  {
            dialog = ConnectAdminDialog.showDialog();
	  }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(AdminTool.this, exc.getMessage());
          return;
        }
                   
        if (dialog.getActionCancelled())
          return;

        try
	  {
            c.connectAdmin(dialog.getAdminHost(), dialog.getAdminPort(),
                           dialog.getAdminUser(), dialog.getAdminPassword());
            adminConnectAction.setEnabled(false);
            adminDisconnectAction.setEnabled(true);
            adminRefreshAction.setEnabled(true);
            if (c.isJndiConnected())
              jndiCreateFactoryAction.setEnabled(true);
	  }
        catch (Exception x) {
          if (Log.logger.isLoggable(BasicLevel.DEBUG))
            Log.logger.log(
              BasicLevel.DEBUG, "", x);
          JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
        }
      }
  }

  private class AdminDisconnectAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AdminDisconnectAction()
      {
        super("Disconnect");
      }

    public void actionPerformed(ActionEvent e)
      {
        try
	  {
            c.disconnectAdmin();
	  }
        catch (Exception x) {
          JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
        }
        finally {
          adminDisconnectAction.setEnabled(false);
          adminConnectAction.setEnabled(true);
          adminRefreshAction.setEnabled(false);
          jndiCreateFactoryAction.setEnabled(false);

          if (tabbedPane.getSelectedIndex() == 0) {
            msgPane.setText("");
            ((CardLayout) editPanel.getLayout()).show(editPanel, "html");
          }
        }
      }
  }

  private class AdminRefreshAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AdminRefreshAction()
      {
        super("Refresh", AdminToolConstants.refreshIcon);
      }

    public void actionPerformed(ActionEvent e)
      {
        AdminTool.invokeLater(new CommandWorker() {
            public void run() throws Exception {
              c.refreshAdminData();
            }
          });
      }
  }

  private class ExitAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExitAction() { super("Exit"); }
    
    public void actionPerformed(ActionEvent e)
      {
        System.exit(0);
      }
  }

  private class ConfigTreeCellRenderer extends DefaultTreeCellRenderer
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ConfigTreeCellRenderer() {
      super();
      setClosedIcon(AdminToolConstants.collapsedIcon);
      setOpenIcon(AdminToolConstants.expandedIcon);
    }

    public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus)
      {
        super.getTreeCellRendererComponent(
          tree, value, sel,
          expanded, leaf, row,
          hasFocus);

        ImageIcon icon = null;
        if (value instanceof AdminTreeNode) {
          icon = ((AdminTreeNode) value).getImageIcon();
          setText(value.toString());
        }
        else if (value == c.getAdminTreeModel().getRoot())
          icon = AdminToolConstants.homeIcon;

        if (icon != null)
          setIcon(icon);

        return this;
      }
  }

  private class PopupListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        TreePath tp;
        if (tabbedPane.getSelectedIndex() == 0) {
          tp = configTree.getPathForLocation(
            e.getPoint().x, e.getPoint().y);
        } else {
          tp = jndiTree.getPathForLocation(
            e.getPoint().x, e.getPoint().y);
        }
        if (tp != null) {
          Object o = tp.getLastPathComponent();
          if (o instanceof AdminTreeNode) {
            JPopupMenu popup = ((AdminTreeNode) o).getContextMenu();
            if (popup != null) {
              popup.show(e.getComponent(), e.getX(), e.getY());
            }
          }
        }
      }
    }
  }

  public static AdminTool getInstance()
    {
      if (adminTool == null) {
        AdminController c = new AdminController();
        adminTool = new AdminTool(c);
        c.setControllerEventListener(adminTool);

        adminTool.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { System.exit(0); }
          });
      }

      return adminTool;
    }

  /**
   * Start the admin tool by creating the controller and the main window.
   */
  public static void main(String[] args)
    {
      AdminTool frame = getInstance();

      frame.setSize(AdminToolConstants.STARTUP_SIZE);
      frame.setVisible(true);
      frame.splitter.setDividerLocation(AdminToolConstants.DIVIDER_PROPORTION);
      frame.repaint();
    }
}
