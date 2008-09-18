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
package deadMQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class DMQListener implements MessageListener {
  
  public void onMessage(Message msg) {
    System.out.println(" ");
    System.out.println("DMQ watcher got a message:");
    try {
      if (msg instanceof TextMessage)
        System.out.println(((TextMessage) msg).getText());
      System.out.println("Delivery count: " + msg.getIntProperty("JMSXDeliveryCount"));
      System.out.println(msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1"));
    } catch (JMSException jE) {
      jE.printStackTrace();
    }
  }
}
