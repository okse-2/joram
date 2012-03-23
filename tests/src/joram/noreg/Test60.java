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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.noreg;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Debug;
import framework.TestCase;

/**
 * Verifies that the opening and closing of a great number of connections does not
 * cause memory leaks (see JORAM-20).
 * 
 * TODO (AF): How to verify that there is no memory leak ?
 */
public class Test60 extends TestCase {

  public static void main(String[] args) {
    new Test60().run();
  }

  public void run() {
    try {
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      Thread.sleep(1000);
      
      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 16010);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;
      
      AdminModule.connect(cf, "root", "root");

      // Create the anonymous user needed for test
      User.create("anonymous", "anonymous");
      
      AdminModule.disconnect();

      System.gc();
      long m1 = Runtime.getRuntime().maxMemory();
      long m2 = Runtime.getRuntime().freeMemory();
      
      long start = System.currentTimeMillis();
      Connection[] cnx = new Connection[10];
      for (int i=0; i<50000; i++) {
        if (i>9) {
          cnx[i%10].close();
          cnx[i%10] = null;
        }
        cnx[i%10] = cf.createConnection("anonymous", "anonymous");
        cnx[i%10].start();
        
        Thread.sleep(5);
      }
      
      for (int i=0; i<10; i++) {
        cnx[i].close();
        cnx[i] = null;
      }
      long end = System.currentTimeMillis();
      
      System.gc();
      System.gc();
      long m3 = Runtime.getRuntime().maxMemory();
      long m4 = Runtime.getRuntime().freeMemory();

      System.out.println("dt=" + (end-start) + ", m1=" + m1 + ", m2=" + m2 + ", m3=" + m3 + ", m4=" + m4);

      Thread.sleep(10000);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();     
    }
  }
}
