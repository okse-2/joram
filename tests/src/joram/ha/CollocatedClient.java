/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2005 - 2009 ScalAgent Distributed Technologies
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
package joram.ha;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.Destination;
import org.objectweb.joram.client.jms.Queue;
import org.objectweb.joram.client.jms.Session;
import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.ha.local.HALocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

public class CollocatedClient extends TestCase {

  public static Process startHACollocatedClient(short sid,
                                                String rid,
                                                String dest) throws Exception {

    String javapath = new File(new File(System.getProperty("java.home"), "bin"), "java").getPath();
    String classpath = System.getProperty("java.class.path");

    List argv = new ArrayList();
    argv.add(javapath);
    argv.add("-classpath");
    argv.add(classpath);
    System.out.println(classpath);
    argv.add("-DnbClusterExpected=2");
    argv.add("-DTransaction=fr.dyade.aaa.util.NullTransaction");
    argv.add("-Dcom.sun.management.jmxremote");
    argv.add("-Ddest=" + dest);

    argv.add(CollocatedClient.class.getName());
    argv.add(Short.toString(sid));
    argv.add("s" + sid);
    argv.add(rid);

    String[] command = (String[]) argv.toArray(new String[argv.size()]);

    Process p = Runtime.getRuntime().exec(command);

    p.getErrorStream().close();
    p.getInputStream().close();
    p.getOutputStream().close();

    return p;
  }

  public static void main(String[] args) throws Exception {
    AgentServer.init(args);
    AgentServer.start();
    
    Thread.sleep(1000L);
    
    File file = File.createTempFile("client", ".txt", new File("."));
    PrintWriter pw = new PrintWriter(new FileOutputStream(file), true);

    try {
      ConnectionFactory cf = null;

      cf = HALocalConnectionFactory.create();

      AdminModule.connect(cf, "root", "root");

      User user = User.create("anonymous", "anonymous", 0);

      Destination dest = null;
      if (System.getProperty("dest", "topic").equals("queue")) {
        dest = Queue.create(0, "queue");
      } else {
        dest = Topic.create(0, "topic");
      }
      dest.setFreeReading();
      dest.setFreeWriting();
      pw.println(dest);
      pw.flush();

      Queue syncq = Queue.create(0, "syncq");
      syncq.setFreeReading();
      syncq.setFreeWriting();

      AdminModule.disconnect();    
  
      cf = new HALocalConnectionFactory();      
      Connection cnx = cf.createConnection("anonymous", "anonymous");
      Session session = (Session) cnx.createSession(true, Session.AUTO_ACKNOWLEDGE);
      MessageConsumer cons = session.createConsumer(dest);
      MessageProducer prod = session.createProducer(syncq);
      cnx.start();

      TextMessage msg = session.createTextMessage("started");
      prod.send(msg);
      session.commit();
      
      int i = 0;
      int idx = -1;
      long start = System.currentTimeMillis();
      pw.println("client#" + args[2] + " start - " + start);
      pw.flush();
      while (true) {
        msg = (TextMessage) cons.receive();
        prod.send(msg);
        session.commit();
        
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
    } catch (Exception exc) {
      exc.printStackTrace(pw);
    }
  }
}
