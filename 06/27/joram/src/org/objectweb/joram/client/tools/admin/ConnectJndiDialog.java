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


public class ConnectJndiDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static ConnectJndiDialog dialog;
  private static JLabel hostLabel;
  private static JLabel portLabel;
  private static JLabel ctxLabel;

  private Frame parent = null;
  private String jndiHost;
  private int jndiPort;
  private String namedCtx = "";
  private JTextField hostField = null;
  private JFormattedTextField portField = null;
  private JTextField ctxField = null;
  private boolean actionCancelled = false;

  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static ConnectJndiDialog initialize(Frame parent) {
    // These JLabel objects are required by the constructor
    hostLabel = new JLabel("Host: ");
    portLabel = new JLabel("Port: ");
    ctxLabel = new JLabel("Context: ");

    dialog = new ConnectJndiDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static ConnectJndiDialog showDialog() throws Exception {
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


  private ConnectJndiDialog(Frame frame)
  {
    super(frame, "Connect to JNDI directory", true);

    parent = frame;

  	jndiHost = System.getProperty(AdminController.PROP_JNDI_HOST) != null ? System.getProperty(AdminController.PROP_JNDI_HOST) : AdminController.DEFAULT_JNDI_HOST;
  	String portStr = System.getProperty(AdminController.PROP_JNDI_PORT) != null ? System.getProperty(AdminController.PROP_JNDI_PORT) : AdminController.DEFAULT_JNDI_PORT;
  	jndiPort = Integer.parseInt(portStr);

    // Buttons
    JButton cancelButton = new JButton("Cancel");
    final JButton connectButton = new JButton("Connect");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConnectJndiDialog.dialog.setVisible(false);
        ConnectJndiDialog.dialog.setActionCancelled(true);
      }
    });
    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ConnectJndiDialog.dialog.setVisible(false);
        jndiHost = hostField.getText();
        jndiPort = Integer.parseInt(portField.getText());
        namedCtx = ctxField.getText();
      }
    });
    getRootPane().setDefaultButton(connectButton);

    // Main part of the dialog
    hostField = new JTextField(jndiHost, 30);
    NumberFormat portFormat = new DecimalFormat("####0");
    portFormat.setMaximumIntegerDigits(5);
    portField = new JFormattedTextField(portFormat);
    portField.setValue(new Integer(jndiPort));
    ctxField = new JTextField(namedCtx, 30);
    JLabel[] labels = {hostLabel, portLabel, ctxLabel};
    JTextField[] textFields = {hostField, portField, ctxField};
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
  
  public String getJndiHost() { return jndiHost; }
  
  public void setJndiHost(String host)
  {
  	jndiHost = host;
  }
  
  public int getJndiPort() { return jndiPort; }
  
  public void setJndiPort(int port)
  {
  	jndiPort = port;
  }
  
  public String getNamedContext() { return namedCtx; }
  
  public void setNamedContext(String ctx)
  {
  	namedCtx = ctx;
  }
}
