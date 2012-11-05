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


public class CreateDestinationDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static CreateDestinationDialog dialog;

  private Frame parent = null;
  
  private boolean actionCancelled = false;

  private String destName;
  private String destType;

  private JTextField destNameField = null;
  private ButtonGroup typeGroup = null;
  private JRadioButton defaultType = null;


  /**
   * Set up the dialog.  The first argument can be null,
   * but it really should be a component in the dialog's
   * controlling frame.
   */
  public static CreateDestinationDialog initialize(Frame parent) {
    dialog = new CreateDestinationDialog(parent);
    
    return dialog;
  }

  /**
   * Show the initialized dialog.  The first argument should
   * be null if you want the dialog to come up in the center
   * of the screen.  Otherwise, the argument should be the
   * component on top of which the dialog should appear.
   */
  public static CreateDestinationDialog showDialog() throws Exception {
    if (dialog != null) {
      dialog.setActionCancelled(false);
      dialog.setLocationRelativeTo(dialog.parent);
      dialog.destNameField.setText("");
      dialog.defaultType.setSelected(true);
      dialog.setVisible(true);
    }
    else {
      throw new Exception("CreateDestinationDialog not initialized");
    }
    
    return dialog;
  }


  private CreateDestinationDialog(Frame frame)
  {
    super(frame, "Create a destination", true);

    parent = frame;

    // Buttons
    final JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateDestinationDialog.dialog.setVisible(false);
        CreateDestinationDialog.dialog.setActionCancelled(true);
      }
    });
    createButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CreateDestinationDialog.dialog.setVisible(false);
        destName = destNameField.getText();
        destType = typeGroup.getSelection().getActionCommand();
      }
    });
    getRootPane().setDefaultButton(createButton);

    // Name panel
    JPanel namePanel = new JPanel();
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    namePanel.add(new JLabel("Name: "));
    destNameField = new JTextField(destName, 30);
    namePanel.add(destNameField);

    // Type panel
    JRadioButton qButton = new JRadioButton("Queue");
    qButton.setActionCommand("Q");
    qButton.setSelected(true);
    defaultType = qButton;

    JRadioButton tButton = new JRadioButton("Topic");
    tButton.setActionCommand("T");
    JRadioButton dmqButton = new JRadioButton("Dead Message Queue");
    dmqButton.setActionCommand("DMQ");

    // Group the radio buttons.
    typeGroup = new ButtonGroup();
    typeGroup.add(qButton);
    typeGroup.add(tButton);
    typeGroup.add(dmqButton);
    JPanel typePanel = new JPanel();
    typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.Y_AXIS));
    typePanel.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createTitledBorder("Destination Type"),
                          BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    typePanel.add(qButton);
    typePanel.add(tButton);
    typePanel.add(dmqButton);
    
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
    contentPane.add(namePanel, BorderLayout.NORTH);
    contentPane.add(typePanel, BorderLayout.CENTER);
    contentPane.add(buttonPanel, BorderLayout.SOUTH);

    pack();
  }

  public boolean getActionCancelled() { return actionCancelled; }
    
  public void setActionCancelled(boolean cancelled)
  {
  	actionCancelled = cancelled;
  }
  
  public String getDestinationName() { return destName; }
  
  public void setDestinationName(String name)
  {
  	destName = name;
  }

  public String getDestinationType() { return destType; }
}
