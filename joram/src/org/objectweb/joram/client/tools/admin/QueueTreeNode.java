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

import org.objectweb.joram.client.jms.Queue;

class QueueTreeNode extends DestinationTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private MessageRootTreeNode msgRoot;

  private Queue queue;

  public QueueTreeNode(AdminController c, 
                       Queue dest) {
    super(c, dest);
    msgRoot = new MessageRootTreeNode();
    queue = dest;
    add(msgRoot);
  }

  public JPopupMenu getContextMenu() {
    JPopupMenu popup = new JPopupMenu("Queue");
    
    ClearQueueAction cqa = new ClearQueueAction();
    if (! c.isAdminConnected())
      cqa.setEnabled(false);
    popup.add(new JMenuItem(cqa));
    
    return popup;
  }

  public final MessageRootTreeNode getMessageRootTreeNode() {
    return msgRoot;
  }

  private class ClearQueueAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ClearQueueAction() {
      super("Clear", AdminToolConstants.trashIcon);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      try {
        Object[] options = { "OK", "CANCEL" };
        int res = JOptionPane.showOptionDialog(
          AdminTool.getInstance(), 
          "You are about to permanently remove all the messages " +
          "from this queue. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, 
          JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        if (res == 0)
          c.clearQueue(QueueTreeNode.this);
      } catch (Exception x) {
        x.printStackTrace();
        JOptionPane.showMessageDialog(null, x.getMessage());
      }
    }
  }

  public final Queue getQueue() {
    return queue;
  }
}
