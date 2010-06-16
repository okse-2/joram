/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - 2010 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package collector;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener {
  String ident = null;

  public MsgListener() {
  }

  public MsgListener(String ident) {
    this.ident = ident;
  }

  public void onMessage(Message msg) {
    try {
      Destination destination = msg.getJMSDestination();
      Destination replyTo = msg.getJMSReplyTo();

      System.out.println();
      System.out.println("URL collected:");
      System.out.println(ident + ": from=" + destination + ",replyTo=" + replyTo);

      Enumeration e = msg.getPropertyNames();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        String value = msg.getStringProperty(key);
        System.out.println(ident + ": " + key + " = " + value);
      }

//      if (msg instanceof TextMessage) {
//        System.out.println(((TextMessage) msg).getText());
//      } else if (msg instanceof BytesMessage) {
//        byte b;
//        try {
//          while (true) {
//            b = ((BytesMessage) msg).readByte();
//            System.out.print((char) b);
//          }
//        } catch (JMSException eof) {
//          System.out.println();
//        }
//      } else if (msg instanceof ObjectMessage) {
//        System.out.println(((ObjectMessage) msg).getObject());
//      }
    } catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }
}
