/*
 * Created on Oct 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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

import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.objectweb.joram.client.jms.admin.*;


/**
 * @author afedoro
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ACLPanel extends JPanel {
	/**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final Dimension LIST_DIMENSION = new Dimension(120, 100);

	private JList userList = new JList();
	private DefaultListModel userListModel = new DefaultListModel();
	private JList accessList = new JList();
	private DefaultListModel accessListModel = new DefaultListModel();
	private JButton addButton = new JButton("Add >>");
	private JButton removeButton = new JButton("<< Remove");
	private java.util.List authorized = null;
	private java.util.List unauthorized = null;

  /**
   * Default constructor 
   */
  public ACLPanel(String title) {
    super();
    setLayout(new BorderLayout());

		JLabel titleLabel = new JLabel(" " + title, AdminToolConstants.lockIcon, SwingConstants.LEFT);
		add(titleLabel, BorderLayout.NORTH);
		userList.setModel(userListModel);
		userList.addListSelectionListener(new UnauthorizedSelectionListener());
		JScrollPane userScrollList = new JScrollPane(userList);
		userScrollList.setPreferredSize(LIST_DIMENSION);
		add(userScrollList, BorderLayout.WEST);
		Box aclButtonBox = Box.createVerticalBox();
		addButton.setEnabled(false);
		addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		addButton.addActionListener(new AddActionListener());
		aclButtonBox.add(addButton);
		aclButtonBox.add(Box.createVerticalStrut(50));
		removeButton.setEnabled(false);
		removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		removeButton.addActionListener(new RemoveActionListener());
		aclButtonBox.add(removeButton);
		add(aclButtonBox, BorderLayout.CENTER);
		accessList.setModel(accessListModel);
		accessList.addListSelectionListener(new AuthorizedSelectionListener());
		JScrollPane accessScrollList = new JScrollPane(accessList);
		accessScrollList.setPreferredSize(LIST_DIMENSION);
		add(accessScrollList, BorderLayout.EAST);
  }

	public void setupLists(java.util.List users, java.util.List auth) {
		authorized = auth;
		unauthorized = new ArrayList();
		userListModel.removeAllElements();
		accessListModel.removeAllElements();
		for (int i = 0; i < auth.size(); i++)
			accessListModel.addElement(getUserName((User) auth.get(i)));
		for (int i = 0; i < users.size(); i++) {
			String name = getUserName((User) users.get(i));
			if (!accessListModel.contains(name)) {
				userListModel.addElement(name);
				unauthorized.add(users.get(i));
			}
		}
		addButton.setEnabled(false);
	}

	public java.util.List getNewlyAuthorizedUsers() {
		java.util.List users = new ArrayList();

		for (Enumeration e = accessListModel.elements(); e.hasMoreElements();) {
			String id = (String) e.nextElement();
			if (findUser(authorized, id) == null)
				users.add(findUser(unauthorized, id));
		}

		return users;
	}

	public java.util.List getNewlyUnauthorizedUsers() {
		java.util.List users = new ArrayList();

		for (Enumeration e = userListModel.elements(); e.hasMoreElements();) {
			String id = (String) e.nextElement();
			if (findUser(unauthorized, id) == null)
				users.add(findUser(authorized, id));
		}

		return users;
	}

	private User findUser(java.util.List list, String id) {
		for (Iterator i = list.iterator(); i.hasNext();) {
			User user = (User) i.next();
			if (user.code().get("name").equals(id))
				return user;
		}

		return null; 
	}

	private String getUserName(User user) {
		Map code = user.code();
		if (code == null)
			return null;
		return (String) code.get("name");
	}

	private class AddActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int[] sel = userList.getSelectedIndices();

			if (sel.length == 0)
				return;

			for (int i = sel.length - 1; i >= 0; i--)
				accessListModel.addElement(userListModel.remove(sel[i]));

			userList.clearSelection();
			addButton.setEnabled(false);
		}
	}

	private class RemoveActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int[] sel = accessList.getSelectedIndices();

			if (sel.length == 0)
				return;

			for (int i = sel.length - 1; i >= 0; i--)
				userListModel.addElement(accessListModel.remove(sel[i]));

			accessList.clearSelection();
			removeButton.setEnabled(false);
		}
	}

	private class UnauthorizedSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (!userList.isSelectionEmpty()) {
				addButton.setEnabled(true);
				removeButton.setEnabled(false);
				accessList.clearSelection();
			}
		}
	}

	private class AuthorizedSelectionListener implements ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			if (!accessList.isSelectionEmpty()) {
				addButton.setEnabled(false);
				removeButton.setEnabled(true);
				userList.clearSelection();
			}
		}
	}
}
