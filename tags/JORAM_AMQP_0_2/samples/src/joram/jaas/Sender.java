/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 - ScalAgent Distributed Technologies
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
package jaas;

import java.util.Properties;

import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

  /**
   * Sends messages on the queue.
   */
  public class Sender {
    static Context ictx = null; 

    public static void main(String[] args) throws Exception {
      System.out.println();
      System.out.println("Sends messages on the queue (jaas identity)...");

      Properties env = new Properties();
      env.put("java.naming.factory.initial", "fr.dyade.aaa.jndi2.client.NamingContextFactory");
      env.put("java.naming.factory.host", "localhost");
      env.put("java.naming.factory.port", "16400");
      ictx = new InitialContext(env);
      
      Queue queue = (Queue) ictx.lookup("queue");
      QueueConnectionFactory qcf = (QueueConnectionFactory) ictx.lookup("qcf");
      ictx.close();

      QueueConnection qc = qcf.createQueueConnection();
      QueueSession qs = qc.createQueueSession(true, 0);
      QueueSender qsend = qs.createSender(queue);
      TextMessage msg = qs.createTextMessage();

      qc.start();
      int i;
      for (i = 0; i < 10; i++) {
        msg.setText("Test number " + i);
        qsend.send(msg);
      }

      qs.commit();
      System.out.println(i + " messages sent.");
      
      qc.close();
    }
  }
