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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.Message;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.objectweb.joram.client.jms.admin.Subscription;
import org.objectweb.joram.client.jms.admin.User;

public class SubscriptionPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private DefaultListModel msgListModel;

  private User user;

  private Subscription sub;

  private JLabel idLabel;
  private JLabel countLabel;
  private JList msgList;
  private JTextArea msgDisplay;

  public SubscriptionPanel(AdminController controller) {
    super(new BorderLayout());

    JPanel form = new JPanel();

    GridBagLayout gridbag = new GridBagLayout();    
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    // c.weightx = 1;
//     c.weighty = 1;
    c.insets = new Insets(5,5,5,5);
    form.setLayout(gridbag);

    JLabel title = new JLabel("Subscription Information");
    title.setFont(new Font("Arial", Font.BOLD, 18));
    title.setHorizontalAlignment(JLabel.LEFT);
    c.gridx = 0;
    c.gridy = 0;
    gridbag.setConstraints(title, c);
    form.add(title);

    JLabel label = new JLabel("Subscription Name:");
    c.gridx = 0;
    c.gridy = 1;
    gridbag.setConstraints(label, c);
    form.add(label);

    idLabel = new JLabel();
    c.gridx = 1;
    c.gridy = 1;
    gridbag.setConstraints(idLabel, c);
    form.add(idLabel);
    
    label = new JLabel("Message Count:");
    c.gridx = 0;
    c.gridy = 2;
    gridbag.setConstraints(label, c);
    form.add(label);
    
    countLabel = new JLabel();
    c.gridx = 1;
    c.gridy = 2;
    gridbag.setConstraints(countLabel, c);
    form.add(countLabel);

    msgListModel = new DefaultListModel();
    msgList = new JList(msgListModel);
    msgList.setSelectionMode(
      ListSelectionModel.SINGLE_INTERVAL_SELECTION);

    JScrollPane listScroller = new JScrollPane(msgList);
    listScroller.getViewport().setScrollMode(
      JViewport.SIMPLE_SCROLL_MODE);
    listScroller.setPreferredSize(new Dimension(400, 80));
    listScroller.setMinimumSize(new Dimension(400, 80));
    listScroller.setAlignmentX(LEFT_ALIGNMENT);

    c.gridx = 0;
    c.gridy = 3;
    c.gridwidth = 4;
    gridbag.setConstraints(listScroller, c);
    form.add(listScroller);

    JButton loadBt = new JButton("Load message");
    loadBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String selectedMsgId = 
            (String)msgList.getSelectedValue();
          if (selectedMsgId != null) {
            try {
              loadMessage(selectedMsgId);
            } catch (Exception exc) {
              JOptionPane.showMessageDialog(
                AdminTool.getInstance(),
                exc.getMessage(), "Load message", 
                JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      });
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 1;
    gridbag.setConstraints(loadBt, c);
    form.add(loadBt);

    JButton deleteBt = new JButton("Delete message");
    deleteBt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String selectedMsgId = 
            (String)msgList.getSelectedValue();
          if (selectedMsgId != null) {
            try {
              deleteMessage(selectedMsgId);
              msgListModel.removeElementAt(
                msgList.getSelectedIndex());
            } catch (Exception exc) {
              JOptionPane.showMessageDialog(
                AdminTool.getInstance(),
                exc.getMessage(), "Delete message", 
                JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      });
    c.gridx = 1;
    c.gridy = 4;
    gridbag.setConstraints(deleteBt, c);
    form.add(deleteBt);
    
    msgDisplay = new JTextArea();
    JScrollPane msgScroller = new JScrollPane(msgDisplay);
    msgScroller.getViewport().setScrollMode(
      JViewport.SIMPLE_SCROLL_MODE);
    msgScroller.setPreferredSize(new Dimension(400, 80));
    msgScroller.setMinimumSize(new Dimension(400, 80));
    msgScroller.setAlignmentX(LEFT_ALIGNMENT);

    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 4;
    gridbag.setConstraints(msgScroller, c);
    form.add(msgScroller);

    add(form, BorderLayout.NORTH); 
  }

  public void setSubscription(Subscription sub) {
    this.sub = sub;
    idLabel.setText(sub.getName());
    setMessageCount(sub.getMessageCount());
  }

  private void setMessageCount(int msgCount) {
    countLabel.setText("" + msgCount);
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void loadMessageIds() throws Exception {
    msgListModel.removeAllElements();
    String[] ids = user.getMessageIds(
      sub.getName());
    for (int i = 0; i < ids.length; i++) {
      msgListModel.addElement(ids[i]);
    }
    setMessageCount(ids.length);
  }

  public void loadMessage(String msgId) throws Exception {
    Message msg = user.getMessage(sub.getName(), msgId);
    msgDisplay.setText(msg.toString());
  }

  public void deleteMessage(String msgId) throws Exception {
    user.deleteMessage(
      sub.getName(), msgId);
  }
}
