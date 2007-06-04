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
package ha;

import fr.dyade.aaa.agent.*;

import org.objectweb.joram.client.jms.ha.local.*;
import org.objectweb.joram.client.jms.admin.AdminModule;

import javax.jms.*;
import javax.jms.MessageConsumer;

import java.io.*;

import framework.TestCase;

public class CollocatedClient extends TestCase {

  public static void main(String[] args) throws Exception {
    AgentServer.init(args);
    AgentServer.start();

    File file = new File("traces" + System.currentTimeMillis() + ".txt");
    PrintWriter pw = new PrintWriter(
      new FileOutputStream(file));

    pw.println("Collocated client #" + args[2] + 
               " - create collocated connection");
    
    TopicHALocalConnectionFactory cf = new TopicHALocalConnectionFactory();
    TopicConnection cnx = cf.createTopicConnection("root", "root");
    
    pw.println("Collocated client #" + args[2] + 
               " - connected to the master replica");
    
    pw.println("Collocated client #" + args[2] + 
               " - get topic");
    
    AdminModule.connect(cf, "root", "root");
    Topic topic = 
      org.objectweb.joram.client.jms.Topic.create(0, "topic");
    ((org.objectweb.joram.client.jms.Topic)topic).setFreeReading();
    ((org.objectweb.joram.client.jms.Topic)topic).setFreeWriting();

    pw.println("Collocated client #" + args[2] + 
               " - subscribe");
    
    Session session = cnx.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
    TopicSubscriber tsub = session.createDurableSubscriber(topic, "test");
    //MessageConsumer tsub = session.createConsumer(topic);
    cnx.start();
   
    while (true) {
      TextMessage msg = (TextMessage)tsub.receive();
      pw.println("Collocated client #" + args[2] + 
                 " - received message: " + msg.getText());
      pw.flush();
    }
  }
}
