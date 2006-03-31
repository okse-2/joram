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
import javax.naming.*;

/**
 * Produces messages on the cluster queue.
 */
public class Producer {
  static Context ictx = null; 

  public static void main(String[] args) throws Exception {

    int i = new Integer(args[0]).intValue();
    System.setProperty("location", ""+i);

    int nbMsg = new Integer(args[1]).intValue();

    int sleep = Integer.getInteger("sleep", 0).intValue();
    int nbMsgSleep = Integer.getInteger("nbMsgSleep", 10).intValue();

    System.out.println();
    System.out.println("Produces " + nbMsg + " messages on the cluster queue...");
    System.out.println("sleep = " + sleep + ", nbMsgSleep=" + nbMsgSleep);

    ictx = new InitialContext();
    Destination clusterQueue = (Destination) ictx.lookup("clusterQueue");
    System.out.println("clusterQueue = " + clusterQueue);
    QueueConnectionFactory cf = (QueueConnectionFactory) ictx.lookup("qcf"+i);
    ictx.close();

    QueueConnection cnx = cf.createQueueConnection("user"+i,"user"+i);
    QueueSession sess = cnx.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(null);

    TextMessage msg = sess.createTextMessage();
    long time = System.currentTimeMillis();
    System.out.println("FirstTime = " + time);

    int j;
    for (j = 0; j < nbMsg; j++) {
      msg.setText("location " + i +" : Test number " + j);
      producer.send(clusterQueue, msg);
      if (sleep > 0 && (j % nbMsgSleep) == 0) {
        Thread.sleep(sleep);
      }
    }
    time = System.currentTimeMillis() - time;
    System.out.println("time = " + time);

    cnx.close();
  }
}
