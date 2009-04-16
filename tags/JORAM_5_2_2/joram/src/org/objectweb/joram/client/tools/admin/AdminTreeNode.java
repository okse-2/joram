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


/**
 * A tree node in the configuration tree.
 */

interface AdminTreeNode
{
    /**
     * Returns descriptive text about the node.
     */
    public String getDescription();

    /**
     * Returns a context menu for the node, or null if
     * no context menu should be created.
     */
    public JPopupMenu getContextMenu();

    /**
     * Gets the image icon for this node, or null to use
     * the default.
     */
    public ImageIcon getImageIcon();

    /**
     * Refreshes the node.
     * @param treeModel the model that the node is contained in.
     */
    public void refresh(DefaultTreeModel treeModel);
}
