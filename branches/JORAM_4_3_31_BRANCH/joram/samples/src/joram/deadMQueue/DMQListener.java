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

import javax.jms.*;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class DMQListener implements MessageListener
{
  String ident;

  public DMQListener(String ident)
  {
    this.ident = ident;
  }


  public void onMessage(javax.jms.Message msg)
  {
    System.out.println("Listener got from " + ident + ":");
    try {
      if (msg instanceof TextMessage)
        System.out.println(((TextMessage) msg).getText());

      System.out.println("Delivery count: "
                         + msg.getIntProperty("JMSXDeliveryCount"));
    }
    catch (JMSException jE) {}
  }
}
