/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
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
package cluster.queue;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.jms.*;
import javax.naming.*;

/**
 * Consumes messages from the cluster queue.
 */
public class XConsumer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {
    ConnectionFactory cf = null;
    Queue dest = null;

    if (args.length != 1)
      throw new Exception("Bad number of argument");

    ictx = new InitialContext();
    try {
      if (args[0].equals("-")) {
        // Choose a connection factory and the associated topic depending of
        // the location property.
        cf = (ConnectionFactory) ictx.lookup("clusterCF");
        dest = (Queue) ictx.lookup("clusterQueue");
      } else {
        cf = (ConnectionFactory) ictx.lookup("cf" + args[0]);
        dest = (Queue) ictx.lookup("queue" + args[0]);
        System.setProperty("location", "server" + args[0]);
      }
    } finally {
      ictx.close();
    }

    Connection cnx = cf.createConnection("anonymous", "anonymous");
    Session session = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer sub = session.createConsumer(dest);

    String location = System.getProperty("location");
    if (location != null)
      System.out.println("Subscribes and listens to queue on " + location);

    sub.setMessageListener(new MsgListener("location" + location + " listener"));
    cnx.start();

    System.out.println("Press a key to exit..");
    System.in.read();

    cnx.close();
  }

  static class Counter {
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
  static class MsgListener implements MessageListener {
    String ident = null;
    int nbMsg = 0;
    long startTime = -1;
    int mps;
    Hashtable<String, Counter> stats;

    public MsgListener() {}

    public MsgListener(String ident) {
      this.ident = ident;
      mps = Integer.getInteger("mps", mps).intValue();
      System.out.println("mps = " + mps);
      stats = new Hashtable<String, Counter>();
    }

    public void onMessage(Message msg) {
      try {
        nbMsg++;
        long time = System.currentTimeMillis();
        if (nbMsg == 1) {
          startTime = time;
        } else {
          long delta = ((nbMsg *1000L)/mps) - (time - startTime);
          if (delta > 0)
            try {
              Thread.sleep(delta);
            } catch (InterruptedException e) {}
        }
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

        if ((nbMsg % 100) == 99) {
          StringBuffer strbuf = new StringBuffer();
          strbuf.append(ident).append(": time=").append(time).append(", nbMsg=").append(nbMsg).append(", mps=").append((nbMsg*1000L)/time);
          for (Enumeration<String> e = stats.keys(); e.hasMoreElements();) {
            String key = e.nextElement();
            strbuf.append(key).append("->").append(stats.get(key).get()).append(',');
          }
          System.out.println(strbuf.toString());
        }
      } catch (Throwable jE) {
        jE.printStackTrace();
      }
    }
  }
}