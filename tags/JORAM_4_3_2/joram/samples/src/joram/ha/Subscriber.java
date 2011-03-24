/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - ScalAgent Distributed Technologies
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
package ha;

import org.objectweb.joram.client.jms.admin.*;
import org.objectweb.joram.client.jms.ha.tcp.TopicHATcpConnectionFactory;
import org.objectweb.joram.client.jms.ConnectionFactory;
import org.objectweb.joram.client.jms.Topic;
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;
import javax.jms.TopicConnectionFactory;

public class Subscriber {

  public static void main(String[] args) 
    throws Exception {
    System.out.println();
    System.out.println("Subscribes and listens to topic...");

    // use joram Admin insteadof Jndi.
    TopicConnectionFactory tcf =
      TopicHATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
    ((ConnectionFactory)tcf).getParameters().connectingTimer = 30;
    
    AdminModule.connect(tcf, "root", "root");
    
    Topic topic = Topic.create(0,"topic");

    AdminModule.disconnect();

    Connection cnx = tcf.createConnection("anonymous", "anonymous");
    Session sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    MessageConsumer sub = sess.createConsumer(topic);

    sub.setMessageListener(new Listener());

    cnx.start();

    System.in.read();


    System.out.println();
    System.out.println("Subscriber closed.");
  }
}