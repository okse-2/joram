/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2004 - 2014 ScalAgent Distributed Technologies
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
 * Initial developer(s): Nicolas Tachker (ScalAgent D.T.)
 * Contributor(s):
 */
package cluster.queue;

import java.util.*;
import javax.jms.*;

class Counter {
  int counter = 0;
  
  Counter() {}
  
  void inc() {
    counter += 1;
  }
  
  int get() {
    return counter;
  }
}

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class MsgListener implements MessageListener {
  String ident = null;
  int nbMsg = 0;
  long startTime = -1;
  int sleep;
  int nbMsgSleep;
  Hashtable<String, Counter> stats;
  
  public MsgListener() {}

  public MsgListener(String ident) {
    this.ident = ident;
    sleep = Integer.getInteger("sleep", 10).intValue();
    nbMsgSleep = Integer.getInteger("nbMsgSleep", 10).intValue();
    System.out.println("sleep = " + sleep + ", nbMsgSleep=" + nbMsgSleep);
    stats = new Hashtable<String, Counter>();
  }

  public void onMessage(Message msg) {
    try {
      nbMsg++;
      long time = System.currentTimeMillis();
      if (nbMsg == 1)
        startTime = time;
      time = time - startTime;

      String location = (String) msg.getStringProperty("location");
      Counter counter = stats.get(location);
      if (counter == null) {
        counter = new Counter();
        counter.inc();
        stats.put(location, counter);
      } else {
        counter.inc();
      }
      
      if (sleep > 0 && (nbMsg % nbMsgSleep) == 0) {
        try {
          Thread.sleep(sleep);
        } catch (InterruptedException e) {}
      }

      if ((nbMsg % 100) == 99) {
        StringBuffer strbuf = new StringBuffer();
        strbuf.append(ident).append(": time = ").append(time).append(" nbMsg=").append(nbMsg);
        for (Enumeration<String> e = stats.keys(); e.hasMoreElements();) {
          String key = e.nextElement();
          strbuf.append(key).append("->").append(stats.get(key).get()).append(',');
        }
        System.out.println(strbuf.toString());
      }
    } catch (JMSException jE) {
      System.err.println("Exception in listener: " + jE);
    }
  }
}
