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
 * Contributor(s):
 */
package org.objectweb.joram.shared.client;

import org.objectweb.joram.shared.messages.Message;
import java.util.Hashtable;
import java.util.Map;
import java.util.Enumeration;
import java.util.Vector;

/**
 * A <code>ProducerMessages</code> instance is sent by a
 * <code>MessageProducer</code> when sending messages.
 */
public class ProducerMessages extends AbstractJmsRequest
{
  /** The wrapped message. */
  private Message message = null;
  /** The wrapped messages. */
  private Vector messages = null;

  /**
   * Indicates whether the produced messages
   * are asynchronously send or not
   * (without or with an acknowledgement).
   */
  private boolean asyncSend = false;
  
  /**
   * Constructs a <code>ProducerMessages</code> instance.
   *
   * @param dest  Name of the destination the messages are sent to.
   */
  public ProducerMessages(String dest)
  {
    super(dest);
    messages = new Vector();
  }

  /**
   * Constructs a <code>ProducerMessages</code> instance carrying a single
   * message.
   *
   * @param dest  Name of the destination the messages are sent to.
   * @param msg  Message to carry.
   */
  public ProducerMessages(String dest, Message msg)
  {
    super(dest);
    message = msg;
  }


  /** Adds a message to deliver. */
  public void addMessage(Message msg)
  {
    messages.addElement(msg);
  }

  /** set a message to deliver. */
  public void setMessage(Message msg) {
    message = msg;
  }

  /** Adds messages to deliver. */
  public void addMessages(Vector msgs)
  {
    for (Enumeration e = msgs.elements(); e.hasMoreElements(); )
      messages.addElement(e.nextElement());
  }

  /** Returns the produced messages. */
  public Vector getMessages()
  {
    if (message != null) {
      Vector vec = new Vector();
      vec.add(message);
      return vec;
    }
    return messages;
  }
  
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  public final boolean getAsyncSend() {
    return asyncSend;
  }
 
  /**
   * Transforms this request into a hashtable of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    // Coding and adding the messages into a array:
    int size = 0;
    if (messages != null)
      size = messages.size();
    if (size > 0) {
      Hashtable [] arrayMsg = new Hashtable[size];
      for (int i = 0; i<size; i++) {
        Message msg = (Message) messages.elementAt(0);
        messages.removeElementAt(0);
        arrayMsg[i] = msg.soapCode();
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
   * Transforms a hastable of primitive values into a
   * <code>ProducerMessages</code> request.
   */
  public static Object soapDecode(Hashtable h) {
    ProducerMessages req = new ProducerMessages(null);
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    Object [] arrayMsg = (Object []) h.get("arrayMsg");
    if (arrayMsg != null) {
      for (int i = 0; i<arrayMsg.length; i++)
        req.addMessage(Message.soapDecode((Hashtable) arrayMsg[i]));
    } else
      req.setMessage(Message.soapDecode((Hashtable) h.get("singleMsg")));
    return req;
  }

  public String toString() {
    return '(' + super.toString() +
      ",message=" + message + 
      ",messages=" + messages +
      ",asyncSend=" + asyncSend + ')';
  }
}
