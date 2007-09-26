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
import java.awt.*;
import java.awt.event.*;

public class CreateDomainDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static CreateDomainDialog dialog;
  private static JLabel nameLabel;
  private static JLabel portLabel;

  private Frame parent = null;
  private String name = "";
  private int port;

  private JTextField nameField;
  private JTextField portField;
  private JButton submitButton;
  private boolean actionCancelled = false;

  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static CreateDomainDialog initialize(Frame parent) {
    // These JLabel objects are required by the constructor
    nameLabel = new JLabel("Domain name: ");
    portLabel = new JLabel("Port: ");

    dialog = new CreateDomainDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static CreateDomainDialog showDialog() {
    dialog.nameField.setText("");
    
    dialog.setTitle("Create domain");
    dialog.submitButton.setText("Apply");
    
    dialog.setActionCancelled(false);
    dialog.setLocationRelativeTo(dialog.parent);
    dialog.setVisible(true);
    
    return dialog;
  }


  private CreateDomainDialog(Frame frame) {
    super(frame, true);

    parent = frame;

    // Buttons
    submitButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateDomainDialog.dialog.setVisible(false);
        CreateDomainDialog.dialog.setActionCancelled(true);
      }
    });

    submitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateDomainDialog.dialog.setVisible(false);
        name = nameField.getText();
        port = Integer.parseInt(
          portField.getText());
      }
    });
    getRootPane().setDefaultButton(submitButton);

    // Main part of the dialog
    nameField = new JTextField(name, 20);    
    portField = new JTextField("" + port, 20);
    JLabel[] labels = {nameLabel, 
                       portLabel};
    JTextField[] textFields = {nameField, 
                               portField};
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
  
  public String getName() { return name; }
  
  public void setName(String name)
  {
  	this.name = name;
  }

  public int getPort() { return port; }
  
  public void setPort(int port)
  {
  	this.port = port;
  }
}
