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

import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

import fr.dyade.aaa.joram.admin.*;


class ServerTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode
{
  private AdminController c;
  private int serverId;
  private MutableTreeNode destRoot = null;
  private MutableTreeNode userRoot = null;
  
  public ServerTreeNode(AdminController c, int serverId)
  {
  	super("Server #" + serverId);
  	this.c = c;
  	this.serverId = serverId;

    destRoot = new DefaultMutableTreeNode("Destinations");
    add(destRoot);
    userRoot = new DefaultMutableTreeNode("Users");
    add(userRoot);
  }

  public void refresh(DefaultTreeModel treeModel)
  {
  }

  public String getDescription()
  {
    StringBuffer sb = new StringBuffer();
    sb.append("<font face=Arial><b>Server #");
    sb.append(serverId);
    sb.append("</b><br></font>");
    return sb.toString();
  }

  public JPopupMenu getContextMenu()
  {
    JPopupMenu popup = new JPopupMenu("Server");

    CreateDestinationAction cda = new CreateDestinationAction();
    if (!c.isJndiConnected() || !c.isAdminConnected())
      cda.setEnabled(false);
    popup.add(new JMenuItem(cda));

    CreateUserAction cua = new CreateUserAction();
    if (!c.isAdminConnected())
      cua.setEnabled(false);
    popup.add(new JMenuItem(cua));

		popup.addSeparator();

		StopServerAction ssa = new StopServerAction();
		if (!c.isAdminConnected())
			ssa.setEnabled(false);
		popup.add(new JMenuItem(ssa));

    return popup;
  }

  public ImageIcon getImageIcon()
  {
    return AdminToolConstants.serverIcon;
  }

  public int getServerId() { return serverId; }

  public MutableTreeNode getDestinationRoot() { return destRoot; }

  public MutableTreeNode getUserRoot() { return userRoot; }

  public String toString() { return "Server #" + serverId; }

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
        User user = (User) utn.getUser();
        rs.add(user);
      }
      catch (ClassCastException cce) {}
    }

    return rs;
  }

  private class CreateDestinationAction extends AbstractAction
  {
    public CreateDestinationAction()
    {
      super("Create Destination...");
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
      try
      {
  	    CreateDestinationDialog cdd = CreateDestinationDialog.showDialog();

        if (!cdd.getActionCancelled())
          c.createDestination(ServerTreeNode.this, cdd.getDestinationName(), cdd.getDestinationType());
      }
      catch (Exception x) {
      	x.printStackTrace();
        JOptionPane.showMessageDialog(null, x.getMessage());
      }
    }
  }

	private class StopServerAction extends AbstractAction
	{
		public StopServerAction()
		{
			super("Stop Server", AdminToolConstants.stopIcon);
		}

		public void actionPerformed(java.awt.event.ActionEvent e)
		{
			try
			{
				Object[] options = { "OK", "CANCEL" };
				int conf = JOptionPane.showOptionDialog(AdminTool.getInstance(), "You are about to stop this server. Please click OK to proceed.",
					"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

				if (conf == 0)
					c.stopServer(ServerTreeNode.this);
			}
			catch (Exception x) {
				x.printStackTrace();
				JOptionPane.showMessageDialog(null, x.getMessage());
			}
		}
	}

  private class CreateUserAction extends AbstractAction
  {
    public CreateUserAction()
    {
      super("Create User...");
    }

    public void actionPerformed(java.awt.event.ActionEvent e)
    {
      try
      {
        while (true) {
          CreateUserDialog cud = CreateUserDialog.showDialog();

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
              c.createUser(ServerTreeNode.this, cud.getUserName(), cud.getPassword());
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
