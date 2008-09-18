/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - France Telecom R&D
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
package cluster.queue;

import javax.jms.*;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener {
  String ident = null;
  int nbMsg = 0;
  long startTime = -1;
  int sleep;
  int nbMsgSleep;

  public MsgListener() {}

  public MsgListener(String ident) {
    this.ident = ident;
    int sleep = Integer.getInteger("sleep", 10).intValue();
    int nbMsgSleep = Integer.getInteger("nbMsgSleep", 10).intValue();
    System.out.println("sleep = " + sleep + ", nbMsgSleep=" + nbMsgSleep);
  }

  public void onMessage(Message msg) {
    try {
      nbMsg++;
      if (nbMsg == 1)
        startTime = System.currentTimeMillis();
      long time = System.currentTimeMillis();
      System.out.println("LastTime = " + time);
      time = time - startTime;
      System.out.println("time = " + time + " nbMsg=" + nbMsg);

      if (msg instanceof TextMessage) {
        if (ident == null) 
          System.out.println(((TextMessage) msg).getText());
        else
          System.out.println(ident + ": " + ((TextMessage) msg).getText());
      } else if (msg instanceof ObjectMessage) {
        if (ident == null) 
          System.out.println(((ObjectMessage) msg).getObject());
        else
          System.out.println(ident + ": " + ((ObjectMessage) msg).getObject());
      }

      if (sleep > 0 && (nbMsg % nbMsgSleep) == 0) {
        try {
          Thread.sleep(sleep);
        } catch (Exception e) {}
      }

    } catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
