/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2006 ScalAgent Distributed Technologies
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package org.objectweb.joram.client.tools.admin;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Iterator;

import org.objectweb.joram.client.jms.admin.*;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;

/**
 * @author afedoro
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DestinationPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final AdminController c;

  private Destination dest = null;
  private JLabel idLabel = new JLabel("");
  private JLabel nameLabel = new JLabel("");
  private JLabel typeLabel = new JLabel("");
  private JLabel pendingMsgsLabel = new JLabel("");
  private JLabel pendingReqsLabel = new JLabel("");
  private JTextField thresholdField = new JTextField(10);
  private JComboBox dmqCombo = new JComboBox();
  private JCheckBox freeRead = new JCheckBox();
  private JCheckBox freeWrite = new JCheckBox();
  private ACLPanel readingACL = new ACLPanel("Reading Access Control List");
  private ACLPanel writingACL = new ACLPanel("Writing Access Control List");
  private boolean nonZeroThreshold = false;
  private boolean dmqSelected = false;

  public DestinationPanel(AdminController c) {
    super(new BorderLayout());
    this.c = c;

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JLabel title = new JLabel("Destination Information");
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setHorizontalAlignment(JLabel.LEFT);
    add(title, BorderLayout.NORTH); 

    Box form = Box.createVerticalBox();
    form.add(Box.createVerticalStrut(15));
    JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    idPanel.add(new JLabel("Destination identifier: "));
    idPanel.add(idLabel);
    form.add(idPanel);
    JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    namePanel.add(new JLabel("JNDI name: "));
    namePanel.add(nameLabel);
    form.add(namePanel);
    JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    typePanel.add(new JLabel("Destination type: "));
    typePanel.add(typeLabel);
    form.add(typePanel);

    JPanel pendingMsgsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    pendingMsgsPanel.add(new JLabel("Pending messages: "));
    pendingMsgsPanel.add(pendingMsgsLabel);
    form.add(pendingMsgsPanel);
    JPanel pendingReqsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    pendingReqsPanel.add(new JLabel("Pending requests: "));
    pendingReqsPanel.add(pendingReqsLabel);
    form.add(pendingReqsPanel);

    JPanel dtPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dtPanel.add(new JLabel("Threshold: "));
    dtPanel.add(thresholdField);
    form.add(dtPanel);
    JPanel dmqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    dmqPanel.add(new JLabel("Dead Message Queue: "));
    dmqPanel.add(dmqCombo);
    form.add(dmqPanel);
    JPanel frPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    frPanel.add(freeRead);
    frPanel.add(new JLabel(" Allow free reading"));
    form.add(frPanel);
    JPanel fwPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    fwPanel.add(freeWrite);
    fwPanel.add(new JLabel(" Allow free writing"));
    form.add(fwPanel);

    form.add(Box.createVerticalStrut(15));
    form.add(readingACL);

    form.add(Box.createVerticalStrut(15));
    form.add(writingACL);

    form.add(Box.createVerticalStrut(25));

    JButton applyButton = new JButton("Apply Changes"); 
    applyButton.addActionListener(new ApplyActionListener());
    form.add(applyButton, BorderLayout.SOUTH);
    form.add(Box.createVerticalStrut(20));
    add(form, BorderLayout.CENTER);
  }

  private class ApplyActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String t = thresholdField.getText();
      if (thresholdField.isEnabled()) {
        if (t != null && t.length() > 0) {
          try {
            Queue q = (Queue) dest;
            c.setQueueThreshold(q, Integer.parseInt(t));
          }
          catch (Exception exc) {
            thresholdField.setText("");
            JOptionPane.showMessageDialog(null, exc.getMessage());
          }
        }
        else if (nonZeroThreshold) {
      	  try {
            Queue q = (Queue) dest;
            c.unsetQueueThreshold(q);
      	  }
      	  catch (Exception exc) {
            JOptionPane.showMessageDialog(null, exc.getMessage());
      	  }
        }
      }

      int i = dmqCombo.getSelectedIndex();
      if (i > 0) {
        try {
          c.setDestinationDMQ(dest, (DeadMQueue) dmqCombo.getSelectedItem());
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }
      else if (dmqSelected) {
        try {
          c.unsetDestinationDMQ(dest);
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }

      boolean fr = freeRead.isSelected();
      if (fr) {
        try {
          c.setFreeReading(dest);
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }
      else {
        try {
          c.unsetFreeReading(dest);
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }

      boolean fw = freeWrite.isSelected();
      if (fw) {
        try {
          c.setFreeWriting(dest);
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }
      else {
        try {
          c.unsetFreeWriting(dest);
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(null, exc.getMessage());
        }
      }

      try {
        for (Iterator it = readingACL.getNewlyAuthorizedUsers().iterator();
             it.hasNext();)
          c.setReader((User) it.next(), dest);
	
        for (Iterator it = readingACL.getNewlyUnauthorizedUsers().iterator();
             it.hasNext();)
          c.unsetReader((User) it.next(), dest);
	
        for (Iterator it = writingACL.getNewlyAuthorizedUsers().iterator();
             it.hasNext();)
          c.setWriter((User) it.next(), dest);
	
        for (Iterator it = writingACL.getNewlyUnauthorizedUsers().iterator();
             it.hasNext();)
          c.unsetWriter((User) it.next(), dest);
      }
      catch (Exception exc) {
        JOptionPane.showMessageDialog(null, exc.getMessage());
      }
    }
  }

  public void setDestination(Destination dest) {
    this.dest = dest;
    idLabel.setText(dest.getName());
    typeLabel.setText(dest.getType());
    String name = c.findDestinationJndiName(dest);
    nameLabel.setText((name == null ? "Unknown" : name));
  }

  public void setPendingMessages(int count) {
    if (count >= 0)
      pendingMsgsLabel.setText(Integer.toString(count));
    else
      pendingMsgsLabel.setText("N/A");
  }

  public void setPendingRequests(int count) {
    if (count >= 0)
      pendingReqsLabel.setText(Integer.toString(count));
    else
      pendingReqsLabel.setText("N/A");
  }

  public void setThreshold(String threshold) {
    thresholdField.setText(threshold);
    nonZeroThreshold = (!"".equals(threshold));
  }

  public void setThresholdActive(boolean val) {
    thresholdField.setEnabled(val);
  }

  public void setFreeReading(boolean val) {
    freeRead.setSelected(val);
  }

  public void setFreeWriting(boolean val) {
    freeWrite.setSelected(val);
  }

  public void setDMQList(java.util.List dmqs, DeadMQueue ddmq) {
    dmqCombo.removeAllItems();
    dmqCombo.addItem("No Dead Message Queue");

    for (Iterator i = dmqs.iterator(); i.hasNext();) {
      DeadMQueue dmq = (DeadMQueue) i.next();
      dmqCombo.addItem(dmq);

      // TODO: This comparison is not very clean and should be improved
      dmqSelected = false;
      if (ddmq != null && ddmq.toString().equals(dmq.toString())) {
        dmqCombo.setSelectedItem(dmq);
        dmqSelected = true;
      }
    }
  }

  public void setReadingACL(java.util.List users, java.util.List auth) {
    readingACL.setupLists(users, auth);
  }

  public void setWritingACL(java.util.List users, java.util.List auth) {
    writingACL.setupLists(users, auth);
  }
}
