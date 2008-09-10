package org.objectweb.joram.client.tools.admin;

import javax.swing.*;
import javax.swing.tree.*;

class MessageRootTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public MessageRootTreeNode() {
    super("Messages");
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
    return null;
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
}
