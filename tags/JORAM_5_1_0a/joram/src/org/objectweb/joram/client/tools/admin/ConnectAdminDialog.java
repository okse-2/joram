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
import java.awt.*;
import java.awt.event.*;
import java.text.*;


public class ConnectAdminDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static ConnectAdminDialog dialog;
  private static JLabel hostLabel;
  private static JLabel portLabel;
  private static JLabel userLabel;
  private static JLabel passwdLabel;

  private Frame parent = null;
  private String adminHost;
  private int adminPort;
  private String adminUser;
  private String adminPasswd;

  private JTextField hostField = null;
  private JFormattedTextField portField = null;
  private JTextField userField = null;
  private JPasswordField passwdField = null;
  private boolean actionCancelled = false;

  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static ConnectAdminDialog initialize(Frame parent) {
    // These JLabel objects are required by the constructor
    hostLabel = new JLabel("Host: ");
    portLabel = new JLabel("Port: ");
    userLabel = new JLabel("User: ");
    passwdLabel = new JLabel("Password: ");

    dialog = new ConnectAdminDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static ConnectAdminDialog showDialog() throws Exception {
    if (dialog != null) {
      dialog.setActionCancelled(false);
      dialog.setLocationRelativeTo(dialog.parent);
      dialog.setVisible(true);
    }
    else {
      throw new Exception("ConnectDialog not initialized");
    }
    
    return dialog;
  }


  private ConnectAdminDialog(Frame frame)
  {
    super(frame, "Connect to Admin server", true);

    parent = frame;

  	adminHost = AdminController.DEFAULT_ADMIN_HOST;
  	String portStr = AdminController.DEFAULT_ADMIN_PORT;
  	adminPort = Integer.parseInt(portStr);

    // Buttons
    JButton cancelButton = new JButton("Cancel");
    final JButton connectButton = new JButton("Connect");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConnectAdminDialog.dialog.setVisible(false);
        ConnectAdminDialog.dialog.setActionCancelled(true);
      }
    });
    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConnectAdminDialog.dialog.setVisible(false);
        adminHost = hostField.getText();
        adminPort = Integer.parseInt(portField.getText());
        adminUser = userField.getText();
        adminPasswd = new String(passwdField.getPassword());
      }
    });
    getRootPane().setDefaultButton(connectButton);

    // Main part of the dialog
    hostField = new JTextField(adminHost, 30);
    NumberFormat portFormat = new DecimalFormat("####0");
    portFormat.setMaximumIntegerDigits(5);
    portField = new JFormattedTextField(portFormat);
    portField.setValue(new Integer(adminPort));
    userField = new JTextField(adminUser, 30);
    passwdField = new JPasswordField(adminPasswd, 30);
    JLabel[] labels = {hostLabel, portLabel, userLabel, passwdLabel};
    JTextField[] textFields = {hostField, portField, userField, passwdField};
    JPanel form = new InputFormPanel(labels, textFields);
    form.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    //Lay out the buttons from left to right.
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(connectButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPane.add(cancelButton);

    //Put everything together, using the content pane's BorderLayout.
    Container contentPane = getContentPane();
    contentPane.add(form, BorderLayout.CENTER);
    contentPane.add(buttonPane, BorderLayout.SOUTH);

    pack();
  }

  public boolean getActionCancelled() { return actionCancelled; }
    
  public void setActionCancelled(boolean cancelled)
  {
  	actionCancelled = cancelled;
  }
  
  public String getAdminHost() { return adminHost; }
  
  public void setAdminHost(String host)
  {
  	adminHost = host;
  }
  
  public int getAdminPort() { return adminPort; }
  
  public void setAdminPort(int port)
  {
  	adminPort = port;
  }
  
  public String getAdminUser() { return adminUser; }
  
  public void setAdminUser(String user)
  {
    adminUser = user;
  }
  
  public String getAdminPassword() { return adminPasswd; }
  
  public void setAdminPassword(String passwd)
  {
    adminPasswd = passwd;
  }
}
