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

public class CreateServerDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static CreateServerDialog dialog;
  private static JLabel nameLabel;
  private static JLabel hostNameLabel;
  private static JLabel serverIdLabel;
  private static JLabel portLabel;

  private Frame parent = null;
  private String name = "";
  private String hostName = "";
  private short serverId;
  private int port;

  private JTextField nameField;
  private JTextField hostNameField;
  private JTextField serverIdField;  
  private JTextField portField;
  private JButton submitButton;
  private boolean actionCancelled = false;

  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static CreateServerDialog initialize(Frame parent) {
    // These JLabel objects are required by the constructor
    nameLabel = new JLabel("Server name: ");
    hostNameLabel = new JLabel("Host name (address): ");
    serverIdLabel = new JLabel("Server id: ");
    portLabel = new JLabel("Port: ");

    dialog = new CreateServerDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static CreateServerDialog showDialog() {
    dialog.nameField.setText("");
    dialog.hostNameField.setText("");
    
    dialog.setTitle("Create server");
    dialog.submitButton.setText("Apply");
    
    dialog.setActionCancelled(false);
    dialog.setLocationRelativeTo(dialog.parent);
    dialog.setVisible(true);
    
    return dialog;
  }


  private CreateServerDialog(Frame frame)
  {
    super(frame, true);

    parent = frame;

    // Buttons
    submitButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateServerDialog.dialog.setVisible(false);
        CreateServerDialog.dialog.setActionCancelled(true);
      }
    });

    submitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateServerDialog.dialog.setVisible(false);
        name = nameField.getText();
        serverId = Short.parseShort(
          serverIdField.getText());
        hostName = hostNameField.getText();
        port = Integer.parseInt(
          portField.getText());
      }
    });
    getRootPane().setDefaultButton(submitButton);

    // Main part of the dialog
    nameField = new JTextField(name, 20);    
    hostNameField = new JTextField(hostName, 20);
    serverIdField = new JTextField("" + serverId, 20);
    portField = new JTextField("" + port, 20);
    JLabel[] labels = {nameLabel, 
                       hostNameLabel, 
                       serverIdLabel,
                       portLabel};
    JTextField[] textFields = {nameField, 
                               hostNameField, 
                               serverIdField, 
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

  public String getHostName() { return hostName; }
  
  public void setHostName(String hostName)
  {
  	this.hostName = hostName;
  }
  
  public short getServerId() { return serverId; }
  
  public void setServerId(short serverId)
  {
  	this.serverId = serverId;
  }

  public int getPort() { return port; }
  
  public void setPort(int port)
  {
  	this.port = port;
  }
}
