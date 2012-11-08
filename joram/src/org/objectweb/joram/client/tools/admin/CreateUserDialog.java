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


public class CreateUserDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static CreateUserDialog dialog;
  private static JLabel nameLabel;
  private static JLabel passwdLabel;
  private static JLabel passwd2Label;

  private Frame parent = null;
  private String name = "";
  private String passwd = "";
  private String passwd2 = "";

  private JTextField nameField = null;
  private JPasswordField passwdField = null;
  private JPasswordField passwd2Field = null;
  private JButton submitButton = null;
  private boolean actionCancelled = false;

  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static CreateUserDialog initialize(Frame parent) {
    // These JLabel objects are required by the constructor
    nameLabel = new JLabel("User name: ");
    passwdLabel = new JLabel("Password: ");
    passwd2Label = new JLabel("Password again: ");

    dialog = new CreateUserDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static CreateUserDialog showDialog() {
  	return showDialog("", false);
  }

  public static CreateUserDialog showDialog(String name, boolean passwdOnly) {
    dialog.nameField.setText(name);
    dialog.passwdField.setText("");
    dialog.passwd2Field.setText("");
    
    if (passwdOnly) {
      dialog.setTitle("Change Password");
      dialog.nameField.setEditable(false);
      dialog.submitButton.setText("Apply");
    }
    else {
      dialog.setTitle("Create User");
      dialog.nameField.setEditable(true);
      dialog.submitButton.setText("Create");
    }
    
    dialog.setActionCancelled(false);
    dialog.setLocationRelativeTo(dialog.parent);
    dialog.setVisible(true);
    return dialog;
  }


  private CreateUserDialog(Frame frame)
  {
    super(frame, true);

    parent = frame;

    // Buttons
    submitButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateUserDialog.dialog.setVisible(false);
        CreateUserDialog.dialog.setActionCancelled(true);
      }
    });

    submitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateUserDialog.dialog.setVisible(false);
        name = nameField.getText();
        passwd = new String(passwdField.getPassword());
        passwd2 = new String(passwd2Field.getPassword());
      }
    });
    getRootPane().setDefaultButton(submitButton);

    // Main part of the dialog
    nameField = new JTextField(name, 20);
    passwdField = new JPasswordField(passwd, 20);
    passwd2Field = new JPasswordField(passwd2, 20);
    JLabel[] labels = {nameLabel, passwdLabel, passwd2Label};
    JTextField[] textFields = {nameField, passwdField, passwd2Field};
    JPanel form = new InputFormPanel(labels, textFields);
    form.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    //Lay out the buttons from left to right.
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(submitButton);
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
  
  public String getUserName() { return name; }
  
  public void setUserName(String name)
  {
  	this.name = name;
  }
  
  public String getPassword() { return passwd; }

  public String getConfirmationPassword() { return passwd2; }
  
  public void setPassword(String passwd)
  {
    this.passwd = passwd;
    this.passwd2 = passwd;
  }
}
