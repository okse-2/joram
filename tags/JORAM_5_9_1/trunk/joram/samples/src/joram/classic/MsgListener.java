/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): ScalAgent Distributed Technologies
 */
package classic;

import java.util.Enumeration;

import javax.jms.*;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener {
  private String ident = null;

  public MsgListener() {
    ident = "listener";
  }

  public MsgListener(String ident) {
    this.ident = ident;
  }

  public void onMessage(Message msg) {
    try {
      Destination destination = msg.getJMSDestination();
      Destination replyTo = msg.getJMSReplyTo();

      System.out.println(ident + " receives message from=" + destination + ",replyTo=" + replyTo);
      Enumeration e = msg.getPropertyNames();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        String value = msg.getStringProperty(key);
        System.out.println("\t" + key + " = " + value);
      }

      if (msg instanceof TextMessage) {
        System.out.println(ident + ": " + ((TextMessage) msg).getText());
      } else if (msg instanceof ObjectMessage) {
        System.out.println(ident + ": " + ((ObjectMessage) msg).getObject());
      }
    } catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
