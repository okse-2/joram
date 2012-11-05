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

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;

class DestinationTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected AdminController c;
  private Destination dest;
  
  public DestinationTreeNode(AdminController c, Destination dest)
  {
  	super(dest);
  	this.c = c;
  	this.dest = dest;
  }

  public void refresh(DefaultTreeModel treeModel)
  {
  }

  public String getDescription()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<font face=Arial><b>");
    sb.append(dest);
    sb.append("</b><br><br>");

    try {
      Queue q = (Queue) dest;
      sb.append("<b>Pending messages: </b>" + c.getPendingMessages(q) + "<br>");
      sb.append("<b>Pending requests: </b>" + c.getPendingRequests(q) + "<br>");
    }
    catch (Exception exc) {System.err.println(exc);}

    try {
      Topic t = (Topic) dest;
      sb.append("<b>Subscriptions: </b>" + c.getSubscriptions(t) + "<br>");
    }
    catch (Exception exc) {System.err.println(exc);}

    sb.append("</font>");
    return sb.toString();
  }

  public JPopupMenu getContextMenu()
  {
    JPopupMenu popup = new JPopupMenu("Destination");

    return popup;
  }

  public ImageIcon getImageIcon()
  {
    return AdminToolConstants.queueIcon;
  }

  public Destination getDestination() { return dest; }

  public ServerTreeNode getParentServerTreeNode() {
    return (ServerTreeNode) getParent().getParent();
  }
  
  public String toString() {
  	String name = c.findDestinationJndiName(dest);
  	if (name == null)
  		return dest.toString();
  	else
  		return name;
  }

  public boolean getAllowsChildren() { 
    return true; 
  }
}
