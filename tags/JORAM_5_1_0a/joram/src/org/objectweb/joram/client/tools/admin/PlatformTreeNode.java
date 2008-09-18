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

import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

import org.objectweb.joram.client.jms.admin.*;

class PlatformTreeNode extends DefaultMutableTreeNode
    implements AdminTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private AdminController c;
  
  public PlatformTreeNode(AdminController c,
                          String title) {
    super(title);
    this.c = c;
  }

  public void refresh(DefaultTreeModel treeModel) {
  }

  public String getDescription() {
    StringBuffer sb = new StringBuffer();
    sb.append("<font face=Arial><b>Platform");
    sb.append("</b><br></font>");
    return sb.toString();
  }

  public JPopupMenu getContextMenu() {
    JPopupMenu popup = new JPopupMenu("Platform");

    

    SaveConfigAction save = new SaveConfigAction();
    if (! c.isAdminConnected())
      save.setEnabled(false);
    popup.add(new JMenuItem(save));

    return popup;
  }

  public ImageIcon getImageIcon()
  {
    return AdminToolConstants.homeIcon;
  }

  public String toString() { return "Platform"; }

  private class SaveConfigAction extends AbstractAction {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SaveConfigAction() {
      super("Save config..."); 
    }
    
    public void actionPerformed(ActionEvent e) {
      String platformConfig;
      try {
        platformConfig = AdminModule.getConfiguration();
      } catch (Exception exc) {
        System.err.println("Failed to load config: " + exc);
        return;
      }

      File currentDirectory = new File(System.getProperty("user.dir"));
      JFileChooser fileChooser = new JFileChooser(currentDirectory);
      int res = fileChooser.showSaveDialog(AdminTool.getInstance());
      switch (res) {
      case JFileChooser.APPROVE_OPTION :
        try {
          File platformConfigFile = fileChooser.getSelectedFile();
          FileOutputStream fos = new FileOutputStream(platformConfigFile);
          PrintWriter pw = new PrintWriter(fos);
          pw.println(platformConfig);
          pw.flush();
          pw.close();
          fos.close();
        } catch (Exception exc) {
          System.err.println("Failed to save config: " + exc);
        }
      case JFileChooser.CANCEL_OPTION :
      case JFileChooser.ERROR_OPTION :
      default :
      }
    }
  }
}
