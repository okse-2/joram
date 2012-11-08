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
 * Initial developer(s): ScalAgent DT
 * Contributor(s):
 */
package org.objectweb.joram.client.tools.admin;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.objectweb.joram.client.jms.admin.Subscription;

class SubscriptionTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AdminController c;
  
  private Subscription sub;
  
  public SubscriptionTreeNode(AdminController c,
                              Subscription sub) {
    this.c = c;
    this.sub = sub;
    setNodeTitle();
  }

  private void setNodeTitle() {
    String title = 
      sub.getName() + " (" + 
      sub.getTopicId() + ", " + 
      sub.getMessageCount();
    if (sub.isDurable()) {
      title = title + ", durable";
    }
    title = title + ')';
    setUserObject(title);
  }

  /**
   * Returns descriptive text about the node.
   */
  public String getDescription() {
    return "";
  }
  
  /**
   * Returns a context menu for the node, or null if
   * no context menu should be created.
   */
  public JPopupMenu getContextMenu() {
    JPopupMenu popup = new JPopupMenu("Subscription");
    
    ClearSubscriptionAction csa = new ClearSubscriptionAction();
    if (! c.isAdminConnected())
      csa.setEnabled(false);
    popup.add(new JMenuItem(csa));
    
    return popup;
  }
  
  /**
   * Gets the image icon for this node, or null to use
   * the default.
   */
  public ImageIcon getImageIcon() {
    return null;
  }
  
  /**
   * Refreshes the node.
   * @param treeModel the model that the node is contained in.
   */
  public void refresh(DefaultTreeModel treeModel) {
    
  }

  public final Subscription getSubscription() {
    return sub;
  }

  
  private class ClearSubscriptionAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ClearSubscriptionAction() {
      super("Clear", AdminToolConstants.trashIcon);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
        Object[] options = { "OK", "CANCEL" };
        int res = JOptionPane.showOptionDialog(
          AdminTool.getInstance(), 
          "You are about to permanently remove all the messages " +
          "from this subscription. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, 
          JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (res == 0)
          c.clearSubscription(SubscriptionTreeNode.this);
      } catch (Exception x) {
        x.printStackTrace();
        JOptionPane.showMessageDialog(null, x.getMessage());
      }
    }
  }
}
