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
package classic;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.objectweb.kjoram.AdminModule;
import org.objectweb.kjoram.Connection;
import org.objectweb.kjoram.MessageProducer;
import org.objectweb.kjoram.Queue;
import org.objectweb.kjoram.Session;
import org.objectweb.kjoram.TcpConnectionFactory;
import org.objectweb.kjoram.TextMessage;

/**
 * Produces messages on the queue and on the topic.
 */
public class Producer extends MIDlet {

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Produces messages on the queue...");

    AdminModule.connect("root", "root", 60);
    
    //Queue queue = new Queue("#0.0.1026","queue");
    Queue queue = Queue.createQueue("queue");
    //Topic topic = Topic.createTopic("topic");
    //AdminModule.disconnect();
    
    System.out.println("Queue = " + queue);
    
    TcpConnectionFactory cf = new TcpConnectionFactory();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageProducer producer = sess.createProducer(null);

    TextMessage msg = sess.createTextMessage();

    int i;
    for (i = 0; i < 10; i++) {
      msg.setText("Test number " + i);
      producer.send(msg, queue);
      //producer.send(msg, topic);
    }

    System.out.println(i + " messages sent.");

    cnx.close();
  }

  protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    // TODO Auto-generated method stub
    
  }

  protected void pauseApp() {
    // TODO Auto-generated method stub
    
  }

  protected void startApp() throws MIDletStateChangeException {
    try {
      main(null);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
