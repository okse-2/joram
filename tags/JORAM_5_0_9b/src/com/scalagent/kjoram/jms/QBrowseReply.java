/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s): Nicolas Tachker (ScalAgent)
 */
package com.scalagent.kjoram.jms;

import com.scalagent.kjoram.messages.Message;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A <code>QBrowseReply</code> instance is used by a JMS client proxy for
 * forwarding a <code>BrowseReply</code> destination notification,
 * actually replying to a client <code>QBrowseRequest</code>.
 */
public class QBrowseReply extends AbstractJmsReply
{
  /** The message carried by this reply. */
  private Message message = null;
  /** The vector of messages carried by this reply. */
  private Vector messages = null;


  /**
   * Constructs a <code>QBrowseReply</code> instance.
   *
   * @param destReply  The queue reply actually forwarded.
   */
  public QBrowseReply(com.scalagent.kjoram.comm.BrowseReply destReply)
  {
    super(destReply.getCorrelationId());
    Vector vec = destReply.getMessages();
    if (vec != null && vec.size() == 1)
      message = (Message) vec.elementAt(0);
    else
      messages = vec;
  }

  /**
   * Constructs an empty <code>QBrowseReply</code>.
   */
  private QBrowseReply(int correlationId)
  {
    super(correlationId);
  }

  /**
   * Constructs a <code>QBrowseReply</code>.
   */
  private QBrowseReply(int correlationId, Message message)
  {
    super(correlationId);
    this.message = message;
  }

  /**
   * Constructs a <code>QBrowseReply</code>.
   */
  private QBrowseReply(int correlationId, Vector messages)
  {
    super(correlationId);
    this.messages = messages;
  }

  public QBrowseReply() {
    messages = new Vector();
  }

  /** Returns the vector of messages carried by this reply. */
  public Vector getMessages()
  {
    if (message != null) {
      Vector vec = new Vector();
      vec.addElement(message);
      return vec;
    }
    return messages;
  }

  public void addMessage(Message msg) {
    messages.addElement(msg);
  }

  public void setMessage(Message msg) {
    message = msg;
  }

  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    // Coding and adding the messages into a array:
    int size = messages.size();
    if (size > 0) {
      Vector arrayMsg = new Vector();
      for (int i = 0; i<size; i++) {
        Message msg = (Message) messages.elementAt(0);
        messages.removeElementAt(0);
        arrayMsg.insertElementAt(msg.soapCode(),i);
      }
      if (arrayMsg != null)
        h.put("arrayMsg",arrayMsg);
    } else {
      if (message != null) {
        h.put("singleMsg",message.soapCode());
      }
    }
    return h;
  }

  /** 
   * Transforms a hashtable of primitive values into a
   * <code>QBrowseReply</code> reply.
   */
  public static Object soapDecode(Hashtable h) {
    QBrowseReply req = new QBrowseReply();
    req.setCorrelationId(((Integer) h.get("correlationId")).intValue());
    Vector arrayMsg = (Vector) h.get("arrayMsg");
    if (arrayMsg != null) {
      for (int i = 0; i<arrayMsg.size(); i++)
        req.addMessage(Message.soapDecode((Hashtable) arrayMsg.elementAt(i)));
    } else
      req.setMessage(Message.soapDecode((Hashtable)h.get("singleMsg")));
    return req;
  }
}
