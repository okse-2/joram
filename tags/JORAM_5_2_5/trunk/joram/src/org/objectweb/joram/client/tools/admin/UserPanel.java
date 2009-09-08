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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.admin.User;


/**
 * @author afedoro
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UserPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final AdminController c;

  private User user = null;
  private JLabel idLabel = new JLabel("");
  private JTextField thresholdField = new JTextField(10);
  private JComboBox dmqCombo = new JComboBox();

  public UserPanel(AdminController c) {
  	super(new BorderLayout());
    this.c = c;

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JLabel title = new JLabel("User Information");
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setHorizontalAlignment(JLabel.LEFT);
    add(title, BorderLayout.NORTH); 

    Box form = Box.createVerticalBox();
    form.add(Box.createVerticalStrut(25));
    JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    idPanel.add(new JLabel("User Name: "));
    idPanel.add(idLabel);
    form.add(idPanel);
    JPanel dtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dtPanel.add(new JLabel("Threshold: "));
    dtPanel.add(thresholdField);
    form.add(dtPanel);
    JPanel dmqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dmqPanel.add(new JLabel("Dead Message Queue: "));
    dmqPanel.add(dmqCombo);
    form.add(dmqPanel);
    form.add(Box.createVerticalStrut(30));

    JButton applyButton = new JButton("Apply Changes"); 
    applyButton.addActionListener(new ApplyActionListener());
    form.add(applyButton, BorderLayout.SOUTH);
    form.add(Box.createVerticalStrut(250));
    add(form, BorderLayout.CENTER);
  }

  private class ApplyActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String t = thresholdField.getText();
      if (t != null && t.length() > 0) {
        try {
          c.setUserThreshold(user, Integer.parseInt(t));
        }
        catch (Exception exc) {
          thresholdField.setText("");
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }
      else {
      	try {
          c.unsetUserThreshold(user);
      	}
      	catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
      	}
      }

      int i = dmqCombo.getSelectedIndex();
      if (i > 0) {
        try {
          c.setUserDMQ(user, (Queue) dmqCombo.getSelectedItem());
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }
      else {
        try {
          c.unsetUserDMQ(user);
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }
    }
  }

  public void setUser(User user) {
    this.user = user;
    try {
      idLabel.setText((String) user.code().get("name"));
    } catch (Exception exc) {}
  }

  public void setThreshold(String threshold) { thresholdField.setText(threshold); }

  public void setDMQList(java.util.List dmqs, Queue ddmq) {
  	dmqCombo.removeAllItems();
	dmqCombo.addItem("No Dead Message Queue");

    for (Iterator i = dmqs.iterator(); i.hasNext();) {
      Queue dmq = (Queue) i.next();
      dmqCombo.addItem(dmq);

      // TODO: This comparison is not very clean and should be improved
      if (ddmq != null && ddmq.toString().equals(dmq.toString()))
        dmqCombo.setSelectedItem(dmq);
    }
  }
}
