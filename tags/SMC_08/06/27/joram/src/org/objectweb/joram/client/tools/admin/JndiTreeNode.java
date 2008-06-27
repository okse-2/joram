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
import javax.naming.*;


class JndiTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private AdminController c;

  private Context ctx = null;
  
  private String name = null;
  private Object obj = null;
  
  public JndiTreeNode(AdminController c, Context ctx, String name) throws NamingException
  {
  	super(name);
  	
  	this.c = c;
  	this.ctx = ctx;
  	this.name = name;
  	this.obj = ctx.lookup(name);
  }

  public JndiTreeNode(AdminController c, Context ctx, Binding binding)
  {
  	super(binding.getName());
  	
  	this.c = c;
  	this.ctx = ctx;
  	name = binding.getName();
  	obj = binding.getObject();
  }

  public void refresh(DefaultTreeModel treeModel)
  {
  }

  public String getDescription()
  {
  	if (obj == null) {
  	  try
  	  {
  	    obj = ctx.lookup(name);
  	  }
  	  catch (NamingException exc) {
  	  	obj = null;
  	  }
  	}
  	
    StringBuffer sb = new StringBuffer();
    if (obj != null) {
      sb.append("<font face=Arial><b>");
      sb.append(getType());
      sb.append("</b><br></font><hr><font face=Arial><br><b>Name:</b> ");
      sb.append(name);
      sb.append("<br><b>Class:</b> ");
      sb.append(obj.getClass().getName());
      sb.append("<br><br><b>Info:</b> ");
      sb.append(getInfo());
      sb.append("<br></font>");
    }
    else {
      sb.append("<font face=Arial><b>Object lookup failed</b></font>");
    }
    return sb.toString();
  }

  public JPopupMenu getContextMenu()
  {
    JPopupMenu popup = new JPopupMenu("Binding");

    DeleteObjectAction doa = new DeleteObjectAction();
    if (!c.isJndiConnected() || !c.isAdminConnected())
      doa.setEnabled(false);
    popup.add(new JMenuItem(doa));
    return popup;
  }

  public ImageIcon getImageIcon()
  {
    return AdminToolConstants.jndiIcon;
  }
  
  public boolean getAllowsChildren() { return false; }
  
  public String getType() {
  	if (obj == null)
  	  return null;
  	
  	String str = obj.toString();
  	int col = str.indexOf(':');
  	if (col == -1)
  	  return "Unknown";

    String t = str.substring(0, col);
    
    if (t.equals("CF"))
      return "ConnectionFactory";
    else if (t.equals("QCF"))
      return "QueueConnectionFactory";
    else if (t.equals("TCF"))
      return "TopicConnectionFactory";
      
    return t;
  }

  public String getInfo() {
  	if (obj == null)
  	  return null;
  	
  	String str = obj.toString();
  	int col = str.indexOf(':');
  	if (col == -1 || col == (str.length() - 1))
  	  return str;

    return str.substring(col + 1);
  }

  public String getName() { return name; }

	public Object getObject() { return obj; }

  private class DeleteObjectAction extends AbstractAction
  {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DeleteObjectAction()
    {
      super("Delete", AdminToolConstants.trashIcon);
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
      try
      {
        Object[] options = { "OK", "CANCEL" };
        int conf = JOptionPane.showOptionDialog(AdminTool.getInstance(), "You are about to permanently delete this object. Please click OK to proceed.",
          "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

        if (conf == 0)
          c.deleteObject(JndiTreeNode.this);
      }
      catch (Exception x) {
      	x.printStackTrace();
        JOptionPane.showMessageDialog(null, x.getMessage());
      }
    }
  }
}
