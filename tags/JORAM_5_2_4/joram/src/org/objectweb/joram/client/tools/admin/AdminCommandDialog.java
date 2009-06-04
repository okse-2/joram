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

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.util.monolog.api.*;

public class AdminCommandDialog extends JDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JLabel msgLabel;
  private JButton abortButton;

  private AdminController ctrl;

  public AdminCommandDialog(Frame frame,
                            AdminController ctrl) {
    super(frame, true);
    this.ctrl = ctrl;

    msgLabel = new JLabel("The command is running...");

    // Buttons
    abortButton = new JButton("Abort");
    abortButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        abort();
      }
    });

    getRootPane().setDefaultButton(abortButton);

    Container contentPane = getContentPane();
    contentPane.add(msgLabel, BorderLayout.CENTER);
    contentPane.add(abortButton, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
	public void windowClosing(WindowEvent e) {
          abort();
        }
      });  
    
    setLocationRelativeTo(frame);
    pack();
  }

  private void abort() {
    try {
      AdminModule.abortRequest();
    } catch (Exception exc) {
      if (Log.logger.isLoggable(BasicLevel.DEBUG))
        Log.logger.log(BasicLevel.DEBUG, "", exc);
    }
    setVisible(false);
  }

  public void showDialog() {
    setVisible(true);
  }

  public void close() {
    setVisible(false);
  }
}
