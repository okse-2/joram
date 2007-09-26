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


public class CreateFactoryDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static CreateFactoryDialog dialog;

  private Frame parent = null;
  
  private boolean actionCancelled = false;

  private String host;
  private int port;
  private String factoryName;
  private String factoryType;

  private JTextField hostField = null;
  private JFormattedTextField portField = null;
  private JTextField factoryNameField = null;
  private ButtonGroup typeGroup = null;
  private JRadioButton defaultType = null;


  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static CreateFactoryDialog initialize(Frame parent) {
    dialog = new CreateFactoryDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static CreateFactoryDialog showDialog() throws Exception {
    if (dialog != null) {
      dialog.setActionCancelled(false);
      dialog.setLocationRelativeTo(dialog.parent);
      dialog.hostField.setText("");
      dialog.portField.setText("");
      dialog.factoryNameField.setText("");
      dialog.defaultType.setSelected(true);
      dialog.setVisible(true);
    }
    else {
      throw new Exception("CreateFactoryDialog not initialized");
    }
    
    return dialog;
  }


  private CreateFactoryDialog(Frame frame)
  {
    super(frame, "Create a connection factory", true);

    parent = frame;

    // Buttons
    final JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateFactoryDialog.dialog.setVisible(false);
        CreateFactoryDialog.dialog.setActionCancelled(true);
      }
    });
    createButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateFactoryDialog.dialog.setVisible(false);
        host = hostField.getText();
        port = Integer.parseInt(portField.getText());
        factoryName = factoryNameField.getText();
        factoryType = typeGroup.getSelection().getActionCommand();
      }
    });
    getRootPane().setDefaultButton(createButton);

    // Form panel
    hostField = new JTextField(host, 30);
    NumberFormat portFormat = new DecimalFormat("####0");
    portFormat.setMaximumIntegerDigits(5);
    portField = new JFormattedTextField(portFormat);
    portField.setValue(new Integer(port));
    factoryNameField = new JTextField(factoryName, 30);
    JLabel[] labels = {new JLabel("Host: "), new JLabel("Port: "), new JLabel("Name: ")};
    JTextField[] textFields = {hostField, portField, factoryNameField};
    JPanel formPanel = new InputFormPanel(labels, textFields);
    formPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // Type panel
    JRadioButton cfButton = new JRadioButton("Connection Factory");
    cfButton.setActionCommand("CF");
    cfButton.setSelected(true);
    defaultType = cfButton;

    JRadioButton qcfButton = new JRadioButton("Queue Connection Factory");
    qcfButton.setActionCommand("QCF");
    JRadioButton tcfButton = new JRadioButton("Topic Connection Factory");
    tcfButton.setActionCommand("TCF");
    JRadioButton xcfButton = new JRadioButton("XA Connection Factory");
    xcfButton.setActionCommand("XCF");
    JRadioButton xqcfButton = new JRadioButton("XA Queue Connection Factory");
    xqcfButton.setActionCommand("XQCF");
    JRadioButton xtcfButton = new JRadioButton("XA Topic Connection Factory");
    xtcfButton.setActionCommand("XTCF");

    // Group the radio buttons.
    typeGroup = new ButtonGroup();
    typeGroup.add(cfButton);
    typeGroup.add(qcfButton);
    typeGroup.add(tcfButton);
    typeGroup.add(xcfButton);
    typeGroup.add(xqcfButton);
    typeGroup.add(xtcfButton);
    JPanel typePanel = new JPanel();
    typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
    typePanel.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createTitledBorder("Factory Type"),
                          BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    typePanel.add(cfButton);
    typePanel.add(qcfButton);
    typePanel.add(tcfButton);
    typePanel.add(xcfButton);
    typePanel.add(xqcfButton);
    typePanel.add(xtcfButton);
    
    // Button panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
    buttonPanel.add(Box.createHorizontalGlue());
    buttonPanel.add(createButton);
    buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonPanel.add(cancelButton);

    //Put everything together, using the content pane's BorderLayout.
    Container contentPane = getContentPane();
    contentPane.add(formPanel, BorderLayout.NORTH);
    contentPane.add(typePanel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    pack();
  }

  public boolean getActionCancelled() { return actionCancelled; }
    
  public void setActionCancelled(boolean cancelled)
  {
  	actionCancelled = cancelled;
  }

  public String getHost() { return host; }
  
  public void setHost(String host)
  {
    this.host = host;
  }
  
  public int getPort() { return port; }

  public void setPort(int port)
  {
    this.port = port;
  }
  
  public String getFactoryName() { return factoryName; }
  
  public void setFactoryName(String name)
  {
  	factoryName = name;
  }

  public String getFactoryType() { return factoryType; }
}
