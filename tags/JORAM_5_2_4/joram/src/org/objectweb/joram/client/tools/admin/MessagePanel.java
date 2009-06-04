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

import java.awt.*;
import javax.swing.*;
import java.util.*;

import javax.jms.*;

public class MessagePanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JTextArea msgDisplay;

  public MessagePanel() {
    super(new BorderLayout());
    msgDisplay = new JTextArea();
    JScrollPane listScroller = new JScrollPane(msgDisplay);
    listScroller.getViewport().setScrollMode(
      JViewport.SIMPLE_SCROLL_MODE);
    listScroller.setPreferredSize(new Dimension(400, 80));
    listScroller.setMinimumSize(new Dimension(400, 80));
    listScroller.setAlignmentX(LEFT_ALIGNMENT);
    add(listScroller, BorderLayout.CENTER); 
  }

  public void setMessage(Message msg) throws Exception {
    StringBuffer buf = new StringBuffer();
    if (msg instanceof TextMessage) {
      buf.append("Text message");
      buf.append("\nText: " + ((TextMessage)msg).getText());
    } else if (msg instanceof MapMessage) {
      buf.append("Map message");
      buf.append("\nMapped values:");
      MapMessage mapMsg = (MapMessage)msg;
      Enumeration e = mapMsg.getMapNames();
      while (e.hasMoreElements()) {
        String name = (String) e.nextElement();
        buf.append("\n - " + name + ": " + 
                   mapMsg.getString(name));
      }
    } else if (msg instanceof ObjectMessage) {
      buf.append("Object message");
      buf.append(
        "\nObject (as a string): " + 
        ((ObjectMessage)msg).getObject());
    } else if (msg instanceof StreamMessage) {
      buf.append("Stream message");
    } else if (msg instanceof BytesMessage) {
      buf.append("Bytes message");
    } 
    buf.append("\nProperties: " + msg.getJMSType());
    Enumeration propNames = msg.getPropertyNames();
    while (propNames.hasMoreElements()) {
      String propName = (String)propNames.nextElement();
      buf.append("\n - " + propName + ": " +
                 msg.getObjectProperty(propName));
    }
    buf.append("\nCorrelation id: " + msg.getJMSCorrelationID());
    buf.append("\nDelivery mode: " + msg.getJMSDeliveryMode());
    buf.append("\nDestination id: " + msg.getJMSDestination());
    buf.append("\nExpiration time: " + msg.getJMSExpiration());
    buf.append("\nIdentifier: " + msg.getJMSMessageID());
    buf.append("\nPriority: " + msg.getJMSPriority());
    buf.append("\nRedelivered: " + msg.getJMSRedelivered());
    buf.append("\nReply to: " + msg.getJMSReplyTo());
    buf.append("\nTime stamp: " + msg.getJMSTimestamp());
    msgDisplay.setText(buf.toString());
  }
}
