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
package monitoring;

import java.util.Enumeration;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Consumes messages from the monitoring topic.
 */
public class Monitor {

  public static void main(String[] args) throws Exception {
    System.out.println();
    System.out.println("Listens to the monitoring topic...");

    Context ictx = new InitialContext();
    Topic topic = (Topic) ictx.lookup("MonitoringTopic");
    ConnectionFactory cf = (ConnectionFactory) ictx.lookup("cf");
    ictx.close();

    Connection cnx = cf.createConnection();
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer subs = sess.createConsumer(topic);

    subs.setMessageListener(new MonitorMsgListener());

    cnx.start();

    System.in.read();
    cnx.close();

    System.out.println();
    System.out.println("Consumer closed.");
  }
  
  static class MonitorMsgListener implements MessageListener {
    public void onMessage(Message message) {
      System.out.println(" ");
      System.out.println(" --> Monitoring message received :");
      try {
        Enumeration enumNames = message.getPropertyNames();
        while (enumNames.hasMoreElements()) {
          String name = (String) enumNames.nextElement();
          System.out.println(name + " : " + message.getObjectProperty(name));
        }
      } catch (JMSException exc) {
        exc.printStackTrace();
      }
    }
  }
  
}
