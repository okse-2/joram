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
package topicTree;

import javax.jms.*;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener
{
  String ident = null;

  public MsgListener()
  {}

  public MsgListener(String ident)
  {
    this.ident = ident;
  }

  public void onMessage(Message msg)
  {
    try {
      if (msg instanceof TextMessage) {
        if (ident == null) 
          System.out.println(((TextMessage) msg).getText());
        else
          System.out.println(ident + ": " + ((TextMessage) msg).getText());
      }
      else if (msg instanceof ObjectMessage) {
        if (ident == null) 
          System.out.println(((ObjectMessage) msg).getObject());
        else
          System.out.println(ident + ": " + ((ObjectMessage) msg).getObject());
      }
    }
    catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
