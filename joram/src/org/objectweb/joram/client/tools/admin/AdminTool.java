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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.jms.*;



public class AdminTool extends JFrame
  implements ControllerEventListener
{
  private static AdminTool adminTool = null;

  private final AdminController c;

  private final JTree configTree;
  private final JTree jndiTree;
  private final JTabbedPane tabbedPane;
  private final JEditorPane msgPane;
  private final ServerPanel serverPanel;
  private final UserPanel userPanel;
  private final DestinationPanel destPanel;
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

    final TreeSelectionModel tsm = new DefaultTreeSelectionModel();
    tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    configTree.setSelectionModel(tsm);

    // Build the JNDI tree
    jndiTree = new JTree(c.getJndiTreeModel());
    jndiTree.expandRow(0);
    jndiTree.setScrollsOnExpand(true);
    jndiTree.setCellRenderer(new ConfigTreeCellRenderer());

    jndiTree.setSelectionModel(tsm);

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

    editPanel = new JPanel(new CardLayout());
    editPanel.add(msgPane, "html");
    editPanel.add(serverPanel, "server");
    editPanel.add(userPanel, "user");
    editPanel.add(destPanel, "destination");

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
      public void valueChanged(javax.swing.event.TreeSelectionEvent e)
      {
        if (!tsm.isSelectionEmpty()) {
          TreeNode selection = (TreeNode) tsm.getSelectionPath().getLastPathComponent();
					updateMessagePaneFromNode(selection);
        }
      }
    });

    jndiTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent e)
      {
        if (!tsm.isSelectionEmpty()) {
          TreeNode selection = (TreeNode) tsm.getSelectionPath().getLastPathComponent();
          updateMessagePaneFromNode(selection);
        }
      }
    });

    // add popup menu listener
    MouseListener popupListener = new PopupListener();
    configTree.addMouseListener(popupListener);
    jndiTree.addMouseListener(popupListener);
  }

	private void showWaitMessage() {
		msgPane.setText("<font face=Arial><br><br><br><br><br><center><b>Loading: Please Wait...</b></center></font>");
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

  private void updateMessagePaneFromNode(final TreeNode selection)
  {
    if (selection instanceof ServerTreeNode ||
    	selection instanceof UserTreeNode ||
			selection instanceof DestinationTreeNode)
		{
			showWaitMessage();
			SwingUtilities.invokeLater(new Runnable() {
				public void run()
				{
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

	private void deferredMessagePaneUpdate(TreeNode selection)
	{
		if (selection instanceof ServerTreeNode) {
			try {
				ServerTreeNode stn = (ServerTreeNode) selection;
				serverPanel.setServerId(stn.getServerId());
				int threshold = c.getDefaultThreshold(stn.getServerId());
				serverPanel.setDefaultThreshold((threshold < 0 ? "" : Integer.toString(threshold)));
				serverPanel.setDMQList(stn.getDeadMessageQueues(), c.getDefaultDMQ(stn.getServerId()));

				((CardLayout) editPanel.getLayout()).show(editPanel, "server");
			}
			catch (Exception exc) {
				JOptionPane.showMessageDialog(AdminTool.this, exc.getMessage());
				return;
			}
		}
		else if (selection instanceof UserTreeNode) {
			try {
				UserTreeNode utn = (UserTreeNode) selection;
				userPanel.setUser(utn.getUser());
				int threshold = c.getUserThreshold(utn.getUser());
				userPanel.setThreshold((threshold < 0 ? "" : Integer.toString(threshold)));
				userPanel.setDMQList(utn.getParentServerTreeNode().getDeadMessageQueues(),
														 c.getUserDMQ(utn.getUser()));

				((CardLayout) editPanel.getLayout()).show(editPanel, "user");
			}
			catch (Exception exc) {
				JOptionPane.showMessageDialog(AdminTool.this, exc.getMessage());
				return;
			}
		}
		else if (selection instanceof DestinationTreeNode) {
			try {
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
			}
			catch (Exception exc) {
				JOptionPane.showMessageDialog(AdminTool.this, exc.getMessage());
				return;
			}
		}
	}

  private class JndiConnectAction extends AbstractAction
  {
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
		JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
	  }
	}
  }

  private class AdminDisconnectAction extends AbstractAction
  {
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
	public AdminRefreshAction()
	{
	  super("Refresh", AdminToolConstants.refreshIcon);
	}

	public void actionPerformed(ActionEvent e)
	{
	  try
	  {
		c.refreshAdminData();
	  }
	  catch (Exception x) {
		JOptionPane.showMessageDialog(AdminTool.this, x.getMessage());
	  }
	}
  }

  private class ExitAction extends AbstractAction
  {
    public ExitAction() { super("Exit"); }
    
    public void actionPerformed(ActionEvent e)
    {
      System.exit(0);
    }
  }

  private class ConfigTreeCellRenderer extends DefaultTreeCellRenderer
  {
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
        if (tabbedPane.getSelectedIndex() == 0)
          tp = configTree.getPathForLocation(e.getPoint().x, e.getPoint().y);
        else
          tp = jndiTree.getPathForLocation(e.getPoint().x, e.getPoint().y);

        Object o = tp.getLastPathComponent();
        
        if (o instanceof AdminTreeNode)
        {
          JPopupMenu popup = ((AdminTreeNode) o).getContextMenu();
          
          if (popup != null)
            popup.show(e.getComponent(), e.getX(), e.getY());
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
