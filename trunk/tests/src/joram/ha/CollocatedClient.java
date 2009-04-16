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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.ha.local.HALocalConnectionFactory;
import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Topic;


import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.ha.local.TopicHALocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

public class CollocatedClient extends TestCase {

  public static void main(String[] args) throws Exception {
    AgentServer.init(args);
    AgentServer.start();

    File file = new File("traces" + System.currentTimeMillis() + ".txt");
    PrintWriter pw = new PrintWriter(new FileOutputStream(file), true);
    
    AdminModule.connect("localhost", 2560, "root", "root", 60);

    String name = System.getProperty("name", "topic");
    Destination dest = null;
    if (name.equals("queue")) {
      dest = Queue.create(0, "queue");
    } else {
      dest = Topic.create(0, "topic");
    }

    pw.println("Destination " + dest);

    AdminModule.disconnect();    

    HALocalConnectionFactory cf = new HALocalConnectionFactory();
    Connection cnx = cf.createConnection("anonymous", "anonymous");
    Session session = (Session) cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
    session.setTopicActivationThreshold(50);
    session.setTopicPassivationThreshold(150);
    session.setTopicAckBufferMax(10);

    MessageConsumer cons = session.createConsumer(dest);
    cnx.start();
 
    int i = 0;
    int idx = -1;
    long start = System.currentTimeMillis();
    pw.println("client#" + args[2] + " start - " + start);
    pw.flush();
    while (true) {
      TextMessage msg = (TextMessage) cons.receive();
      int idx2 = msg.getIntProperty("index");
      if ((idx != -1) && (idx2 != idx +1)) {
        pw.println("Message lost #" + (idx +1) + " - " + idx2);
      }
      idx = idx2;
      pw.println("client#" + args[2] + " - msg#" + msg.getText());
      if ((i %1000) == 999) {
        long end = System.currentTimeMillis();
        pw.println("Round #" + (i /1000) + " - " + (end - start));
        start = end;
      }
      i++;
      pw.flush();
    }
  }
}
