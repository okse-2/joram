/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s): (ScalAgent D.T.)
 * Contributor(s): Badolle Fabien (ScalAgent D.T.)
 */

package joram.ha; 

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.ha.tcp.HATcpConnectionFactory;

public class PublisherClient {

  public static void main(String[] args) throws Exception {
    ConnectionFactory cf = HATcpConnectionFactory.create("hajoram://localhost:2560,localhost:2561,localhost:2562");
    ((HATcpConnectionFactory) cf).getParameters().connectingTimer = 30;

    AdminModule.connect((HATcpConnectionFactory) cf, "root", "root");
    Topic topic = 
      org.objectweb.joram.client.jms.Topic.create(0, "topic");
    ((org.objectweb.joram.client.jms.Topic)topic).setFreeReading();
    ((org.objectweb.joram.client.jms.Topic)topic).setFreeWriting();

    Connection cnx = cf.createConnection(
      "root", "root");
    Session session = cnx.createSession(
      false, Session.AUTO_ACKNOWLEDGE);    
    MessageProducer producer = session.createProducer(topic);
    cnx.start();

    int i = 0;
    while (true) {
      Message msg = session.createTextMessage("messagepublisher #" + i++);
      producer.send(msg);
      Thread.sleep(2000);
    }
  }
}
