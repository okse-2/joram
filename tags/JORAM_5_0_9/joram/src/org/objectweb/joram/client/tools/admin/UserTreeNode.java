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

import javax.swing.*;
import javax.swing.tree.*;

import org.objectweb.joram.client.jms.admin.User;


class UserTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private AdminController c;
  private User user;

  private SubscriptionRootTreeNode subscriptionRoot;
  
  public UserTreeNode(AdminController c, User user) {
    super(user.getName() + ':' + 
          user.getProxyId());
    this.c = c;
    this.user = user;
    
    subscriptionRoot = new SubscriptionRootTreeNode();
    add(subscriptionRoot);
  }

  public void refresh(DefaultTreeModel treeModel)
    {
    }

  public String getDescription()
    {
      StringBuffer sb = new StringBuffer();
      sb.append("<font face=Arial><b>User: </b>");
      sb.append(user);
      sb.append("<br><br>");

      sb.append("</font>");
      return sb.toString();
    }

  public JPopupMenu getContextMenu()
    {
      JPopupMenu popup = new JPopupMenu("User");

      ChangePasswordAction cpa = new ChangePasswordAction();
      if (!c.isAdminConnected())
        cpa.setEnabled(false);
      popup.add(new JMenuItem(cpa));

      DeleteUserAction dua = new DeleteUserAction();
      if (!c.isAdminConnected())
        dua.setEnabled(false);
      popup.add(new JMenuItem(dua));

      return popup;
    }

  public ImageIcon getImageIcon()
    {
      return AdminToolConstants.userIcon;
    }

  public User getUser() { return user; }
  
  // TODO: This is a hack because the User object doesn't provide the user name
  public String getUserName() {
    String name = null;

    try {
      name =  (String) user.code().get("name");
    }
    catch (Exception exc) {}
    
    return name;
  }

  public ServerTreeNode getParentServerTreeNode() {
    return (ServerTreeNode) getParent().getParent();
  }

  public boolean getAllowsChildren() { 
    return true; 
  }

  private class DeleteUserAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DeleteUserAction()
      {
        super("Delete", AdminToolConstants.trashIcon);
      }

    public void actionPerformed(java.awt.event.ActionEvent e)
      {
        try
          {
            Object[] options = { "OK", "CANCEL" };
            int conf = JOptionPane.showOptionDialog(AdminTool.getInstance(), "You are about to permanently delete this user. Please click OK to proceed.",
                                                    "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

            if (conf == 0)
              c.deleteUser(UserTreeNode.this);
          }
        catch (Exception x) {
          x.printStackTrace();
          JOptionPane.showMessageDialog(null, x.getMessage());
        }
      }
  }

  private class ChangePasswordAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ChangePasswordAction()
      {
        super("Change Password...");
      }

    public void actionPerformed(java.awt.event.ActionEvent e)
      {
        try
          {
            while (true) {
              CreateUserDialog cud = CreateUserDialog.showDialog(UserTreeNode.this.getUserName(), true);

              if (cud.getActionCancelled()) {
                break;
              }
              else {
                String msg = "";

                if (cud.getPassword().length() == 0)
                  msg = "You have to enter a new password for the user. Please click OK to make corrections.";
                else if (!cud.getPassword().equals(cud.getConfirmationPassword()))
                  msg = "The two passwords that you have entered do not match. Please click OK to make corrections.";
                else {
                  c.updateUser(UserTreeNode.this, cud.getUserName(), cud.getPassword());
                  break;
                }

                Object[] options = { "OK" };
                JOptionPane.showOptionDialog(AdminTool.getInstance(), msg,
                                             "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
              }
            }
          }
        catch (Exception x) {
          x.printStackTrace();
          JOptionPane.showMessageDialog(null, x.getMessage());
        }
      }
  }
}
