/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 - 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s): 
 */
package bridge;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener {
  String who;
  
  public MsgListener(String who) {
    this.who = who;
  }
  
  public void onMessage(Message msg) {
    try {
      if (msg instanceof TextMessage)
        System.out.println(who + " receive on acquisition topic: " + ((TextMessage) msg).getText());
    }
    catch (JMSException exc) {
      System.err.println("Exception in listener: " + exc);
    }
  }
}
