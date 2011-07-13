/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package classic;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.objectweb.joram.client.jms.MessageConsumer;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Session;

public class AConsumer {
  public static void main(String[] args) throws Exception {
    Context context = new InitialContext();
    ConnectionFactory cf = (ConnectionFactory) context.lookup("cf");
    javax.jms.Queue queue =(Queue)context.lookup("queue");
    context.close();
    
    Connection cnx = cf.createConnection();
    Session session = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
//    session.setImplicitAck(true);
    session.setQueueMessageReadMax(100);
    MessageConsumer consumer = (MessageConsumer) session.createConsumer(queue);
    consumer.setMessageListener(new MyListener());
    cnx.start();
    
    System.in.read();
    
    session.close();
    cnx.close();
    System.exit(0);
  }
}

class MyListener implements MessageListener {
  int nbMess=0;
  
  public void onMessage(Message message) {
    nbMess++;

    if ( (nbMess % 1000) ==0 )
      System.out.println(nbMess + " messages read");
  }
}
