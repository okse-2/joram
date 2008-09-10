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

import javax.swing.*;
import javax.swing.tree.*;

class MessageTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AdminController c;

  private String msgId;
  
  public MessageTreeNode(AdminController c,
                         String msgId) {
    super(msgId);
    this.c = c;
    this.msgId = msgId;
  }

  /**
   * Returns descriptive text about the node.
   */
  public String getDescription() {
    return "";
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

  public JPopupMenu getContextMenu() {
    JPopupMenu popup = new JPopupMenu("Message");
    
    DeleteMessageAction dma = new DeleteMessageAction();
    if (! c.isAdminConnected())
      dma.setEnabled(false);
    popup.add(new JMenuItem(dma));
    
    return popup;
  }

  public final String getMessageId() {
    return msgId;
  }

  public boolean getAllowsChildren() {
    return false;
  }

  private class DeleteMessageAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DeleteMessageAction() {
      super("Delete", AdminToolConstants.trashIcon);
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
        Object[] options = { "OK", "CANCEL" };
        int res = JOptionPane.showOptionDialog(
          AdminTool.getInstance(), 
          "You are about to permanently remove this message " +
          "from this subscription. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, 
          JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (res == 0)
          c.deleteMessage(MessageTreeNode.this);
      } catch (Exception x) {
        x.printStackTrace();
        JOptionPane.showMessageDialog(null, x.getMessage());
      }
    }
  }
}
