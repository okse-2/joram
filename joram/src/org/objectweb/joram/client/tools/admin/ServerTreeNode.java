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

import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.objectweb.joram.client.jms.admin.DeadMQueue;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;


class ServerTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private AdminController c;
  private Server serverDesc;
  private DestinationRootTreeNode destRoot;
  private UserRootTreeNode userRoot;
  private DomainRootTreeNode domainRoot;

  public ServerTreeNode(AdminController c, 
                        Server serverDesc) {
    super(toString(serverDesc));
    this.c = c;
    this.serverDesc = serverDesc;
    
    destRoot = new DestinationRootTreeNode(this);
    add(destRoot);
    userRoot = new UserRootTreeNode(this);
    add(userRoot);
    domainRoot = new DomainRootTreeNode(this);
    add(domainRoot);
  }

  public void refresh(DefaultTreeModel treeModel) {
  }

  public String getDescription() {
    StringBuffer sb = new StringBuffer();
    sb.append("<font face=Arial><b>Server #");
    sb.append(toString(serverDesc));
    sb.append("</b><br></font>");
    return sb.toString();
  }
  
  public JPopupMenu getContextMenu() {
    JPopupMenu popup = new JPopupMenu("Server");
    
    CreateDestinationAction cda = new CreateDestinationAction();
    if (! c.isJndiConnected() || !c.isAdminConnected())
      cda.setEnabled(false);
    popup.add(new JMenuItem(cda));
    
    CreateUserAction cua = new CreateUserAction();
    if (! c.isAdminConnected())
      cua.setEnabled(false);
    popup.add(new JMenuItem(cua));

    popup.addSeparator();

    CreateDomainAction cdoma = new CreateDomainAction();
    if (! c.isAdminConnected())
      cdoma.setEnabled(false);
    popup.add(new JMenuItem(cdoma));
    
    popup.addSeparator();
    
    StopServerAction ssa = new StopServerAction();
    if (!c.isAdminConnected())
      ssa.setEnabled(false);
    popup.add(new JMenuItem(ssa));

    DeleteServerAction dsa = new DeleteServerAction();
    if (getDomainRoot().getChildCount() != 0 ||
        ! c.isAdminConnected())
      dsa.setEnabled(false);
    popup.add(new JMenuItem(dsa));
    
    return popup;
  }
  
  public ImageIcon getImageIcon() {
    return AdminToolConstants.serverIcon;
  }
  
  public int getServerId() {
    return serverDesc.getId();
  }

  public final DestinationRootTreeNode getDestinationRoot() { 
    return destRoot; 
  }

  public final UserRootTreeNode getUserRoot() {
    return userRoot; 
  }

  public final DomainRootTreeNode getDomainRoot() {
    return domainRoot;
  }

  public String toString() {
    return toString(serverDesc);
  }

  public static String toString(Server serverDesc) {
    return "Server #" + 
      serverDesc.getId() + ": " + 
      serverDesc.getName() + " (" + 
      serverDesc.getHostName() + ')';
  }

  public List getDeadMessageQueues() {
    List rs = new Vector();

    for (int i = 0; i < destRoot.getChildCount(); i++){
      DestinationTreeNode dtn = (DestinationTreeNode) destRoot.getChildAt(i);
      try {
        DeadMQueue dmq = (DeadMQueue) dtn.getDestination();
        rs.add(dmq);
      }
      catch (ClassCastException cce) {}
    }
    return rs;
  }

  public List getUsers() {
    List rs = new Vector();

    for (int i = 0; i < userRoot.getChildCount(); i++){
      UserTreeNode utn = (UserTreeNode) userRoot.getChildAt(i);
      try {
        User user = utn.getUser();
        rs.add(user);
      }
      catch (ClassCastException cce) {}
    }

    return rs;
  }

  private class CreateDestinationAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateDestinationAction() {
      super("Create Destination...");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
        final CreateDestinationDialog cdd = CreateDestinationDialog.showDialog();
        
        if (! cdd.getActionCancelled()) {
          AdminTool.invokeLater(new CommandWorker() {
              public void run() throws Exception {
                c.createDestination(ServerTreeNode.this, 
                                    cdd.getDestinationName(), 
                                    cdd.getDestinationType());
              }
            });
        }
      }
      catch (Exception x) {
        x.printStackTrace();
        JOptionPane.showMessageDialog(null, x.getMessage());
      }
    }
  }

  private class StopServerAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StopServerAction()
      {
        super("Stop Server", AdminToolConstants.stopIcon);
      }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      Object[] options = { "OK", "CANCEL" };
      int conf = JOptionPane.showOptionDialog(
        AdminTool.getInstance(), 
        "You are about to stop this server. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, 
        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
      
      if (conf == 0) {
        AdminTool.invokeLater(new CommandWorker() {
            public void run() throws Exception {
              c.stopServer(ServerTreeNode.this);
            }
          });
      }
    }
  }

  private class CreateUserAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateUserAction() {
      super("Create User...");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      while (true) {
        final CreateUserDialog cud = CreateUserDialog.showDialog();
        
        if (cud.getActionCancelled()) {
          break;
        }
        else {
          String msg = "";
          
          if (cud.getUserName().length() == 0)
            msg = "The user name is mandatory to create a new user. Please click OK to make corrections.";
          else if (cud.getPassword().length() == 0)
            msg = "A password is mandatory to create a new user. Please click OK to make corrections.";
          else if (!cud.getPassword().equals(cud.getConfirmationPassword()))
            msg = "The two passwords that you have entered do not match. Please click OK to make corrections.";
          else {
            AdminTool.invokeLater(new CommandWorker() {
                public void run() throws Exception {
                  c.createUser(ServerTreeNode.this, cud.getUserName(), cud.getPassword());
                } 
              });
            break;
          }
          
          Object[] options = { "OK" };
          JOptionPane.showOptionDialog(AdminTool.getInstance(), msg,
                                       "Error", JOptionPane.DEFAULT_OPTION, 
                                       JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        }
      }
    }
  }

  private class CreateDomainAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateDomainAction() {
      super("Create Domain...");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      while (true) {
        final CreateDomainDialog cdd = CreateDomainDialog.showDialog();
        
        if (cdd.getActionCancelled()) {
          break;
        } else {
          AdminTool.invokeLater(new CommandWorker() {
              public void run() throws Exception {
                c.createDomain(ServerTreeNode.this, 
                               cdd.getName(), 
                               cdd.getPort());
              } 
            });
          break;
        }
      }
    }
  }

  private class DeleteServerAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DeleteServerAction() {
      super("Delete Server");
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (domainRoot.getChildCount() != 0) {
        JOptionPane.showMessageDialog(AdminTool.getInstance(), 
                                      "Can't remove a server that owns domains.",
                                      "Remove server",
                                      JOptionPane.ERROR_MESSAGE);
      } else {
        Object[] options = { "OK", "CANCEL" };
        int conf = JOptionPane.showOptionDialog(
          AdminTool.getInstance(), 
          "You are about to delete this server. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, 
          JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        
        if (conf == 0) {
          AdminTool.invokeLater(new CommandWorker() {
              public void run() throws Exception {
                c.deleteServer(ServerTreeNode.this);
              } 
            });
        }
      }
    }
  }
}
