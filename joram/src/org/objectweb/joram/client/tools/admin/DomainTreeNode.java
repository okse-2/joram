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
import java.awt.event.*;

import org.objectweb.joram.client.jms.admin.*;

import org.objectweb.util.monolog.api.*;

class DomainTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AdminController c;

  private String domainName;

  public DomainTreeNode(AdminController c,
                        String domainName) {
    super(domainName);
    this.domainName = domainName;
    this.c = c;
  }
  
  public final String getDomainName() {
    return domainName;
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
    JPopupMenu popup = new JPopupMenu("Domain " + domainName);

    CreateServerAction create = new CreateServerAction();
    if (! c.isAdminConnected())
      create.setEnabled(false);
    popup.add(new JMenuItem(create));
    
    popup.addSeparator();

    DeleteDomainAction dda = new DeleteDomainAction();
    if (getChildCount() != 0 ||
        ! c.isAdminConnected())
      dda.setEnabled(false);
    popup.add(new JMenuItem(dda));

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

  private class CreateServerAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public CreateServerAction() {
      super("Create server..."); 
    }
    
    public void actionPerformed(ActionEvent e) {
      CreateServerDialog csd = CreateServerDialog.showDialog();
      
      if (! csd.getActionCancelled()) {
        try {
          AdminModule.addServer(
            csd.getServerId(), 
            csd.getHostName(), 
            domainName, 
            csd.getPort(),
            csd.getName());
        } catch (Exception exc) {
          if (Log.logger.isLoggable(BasicLevel.DEBUG))
            Log.logger.log(BasicLevel.DEBUG, "", exc);
          JOptionPane.showMessageDialog(AdminTool.getInstance(), 
                                        exc.getMessage());
          return;
        }
        ServerTreeNode serverNode = 
          new ServerTreeNode(
            c, 
            new Server(
              (int)csd.getServerId(),
              csd.getName(),
              csd.getHostName()));
        c.getAdminTreeModel().insertNodeInto(
          serverNode, DomainTreeNode.this, 
          DomainTreeNode.this.getChildCount());
      }
    }
  }

  private class DeleteDomainAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DeleteDomainAction() {
      super("Delete domain", AdminToolConstants.trashIcon);
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
      if (getChildCount() != 0) {
        JOptionPane.showMessageDialog(AdminTool.getInstance(), 
                                      "Can't remove a domain that owns servers.",
                                      "Remove domain",
                                      JOptionPane.ERROR_MESSAGE);
      } else {
        Object[] options = { "OK", "CANCEL" };
        int conf = JOptionPane.showOptionDialog(
          AdminTool.getInstance(), 
          "You are about to delete this domain. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, 
          JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        
        if (conf == 0) {
          AdminTool.invokeLater(new CommandWorker() {
              public void run() throws Exception {
                c.deleteDomain(DomainTreeNode.this);
              } 
            });
        }
      }
    }
  }
}
