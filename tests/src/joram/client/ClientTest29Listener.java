/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2012 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
package joram.client;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

/**
 * Test memory leak with subscriptions with connection close.
 */
public class ClientTest29Listener implements MessageListener {

  public static void main(String[] args) {
    new ClientTest29Listener().run();
  }

  public void run() {
    try {
      javax.naming.Context jndiCtx = new javax.naming.InitialContext();  
      Topic topic1 = (Topic) jndiCtx.lookup("topic1");
      Topic topic2 = (Topic) jndiCtx.lookup("topic2");
      ConnectionFactory cf = (ConnectionFactory) jndiCtx.lookup("cf");
      jndiCtx.close();

      ((org.objectweb.joram.client.jms.ConnectionFactory) cf).getParameters().cnxPendingTimer = ClientTest29.pending;
      Connection cnx1 = cf.createConnection("root", "root");

      Session session1 = cnx1.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons1 = session1.createConsumer(topic1);
      cons1.setMessageListener(this);

      cnx1.start();

      Connection cnx2 = cf.createConnection("root", "root");

      Session session2 = cnx2.createSession(false, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons2 = session2.createConsumer(topic2);
      cons2.setMessageListener(this);

      cnx2.start();
    } catch (Throwable exc) {
      exc.printStackTrace();
    }
  }

  public void onMessage(Message msg) {
    // Log the message receipt.
  }
}
