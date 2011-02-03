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
import javax.swing.tree.*;

import org.objectweb.joram.client.jms.Topic;

class TopicTreeNode extends DestinationTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private SubscriberRootTreeNode subscriberRoot;

  private Topic topic;
  
  public TopicTreeNode(AdminController c, 
                       Topic dest) {
    super(c, dest);
    topic = dest;

    subscriberRoot = new SubscriberRootTreeNode();
    add(subscriberRoot);
  }

  public final Topic getTopic() {
    return topic;
  }
}