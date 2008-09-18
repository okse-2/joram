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
import org.objectweb.kjoram.Message;
import org.objectweb.kjoram.MessageConsumer;
import org.objectweb.kjoram.Queue;
import org.objectweb.kjoram.Session;
import org.objectweb.kjoram.TcpConnectionFactory;
import org.objectweb.kjoram.TextMessage;


/**
 * Consumes messages from the queue and from the topic.
 */
public class Consumer extends MIDlet {
  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Consume the queue...");

    AdminModule.connect("root", "root", 60);
    Queue queue = Queue.createQueue("queue");
    //Topic topic = Topic.createTopic("topic");
    //AdminModule.disconnect();
    
    TcpConnectionFactory cf = new TcpConnectionFactory();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer recv = sess.createConsumer(queue);
    //MessageConsumer subs = sess.createConsumer(topic);

    cnx.start();
    Message msg;
    
    do {
      msg = recv.receive(1000);
        if (msg instanceof TextMessage) {
          System.out.println(((TextMessage) msg).getText());
        } else {
          System.out.println("msg = " + msg);
        }
    } while (msg != null);
 
    cnx.close();

    System.out.println();
    System.out.println("Consumer closed.");
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
