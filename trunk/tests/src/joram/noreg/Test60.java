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
import javax.jms.XAConnectionFactory;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;
import framework.TestCase;

/**
 * Verifies that the opening and closing of a great number of connections does not
 * cause memory leaks (see JORAM-20). We now verify the number of threads created
 * during the test.
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
      Thread.sleep(1000);

      System.gc();
      long m1 = Runtime.getRuntime().maxMemory();
      long m2 = Runtime.getRuntime().freeMemory();
      
      int tc0 = Thread.activeCount();
      System.out.println("Threads count = " + tc0);
//      Thread[] tarray = new Thread[50];
//      int tc = Thread.enumerate(tarray);
//      for (int i=0; i<tc; i++)
//        System.out.println("Thread[" + i + "] = " + tarray[i].getName());
      long start = System.currentTimeMillis();
      
      int tc1 = -1; int tc2 = -1;
      Connection[] cnx = new Connection[10];
      for (int i=0; i<10000; i++) {
        if (i>9) {
          tc1 = Thread.activeCount();
          cnx[i%10].close();
          cnx[i%10] = null;
        }
        if ((i%2) == 0)
          cnx[i%10] = ((XAConnectionFactory) cf).createXAConnection("anonymous", "anonymous");
        else
          cnx[i%10] = ((ConnectionFactory) cf).createConnection("anonymous", "anonymous");
        cnx[i%10].start();
        
//        if ((i%100) == 99) {
//          tc2 = Thread.activeCount();
//          assertTrue("Bad number of threads: " + tc2 + " != " + tc1, (tc2 == tc1));
//          System.out.println("#" + i + " - Threads count = " + tc2);
//        }
        Thread.sleep(5);
      }
      
      for (int i=0; i<10; i++) {
        cnx[i].close();
        cnx[i] = null;
      }
      long end = System.currentTimeMillis();
      Thread.sleep(1000);
      
      tc2 = Thread.activeCount();
      assertTrue("Bad number of final threads: " + tc2 + " != " + tc0, (tc2 == tc0));
      System.out.println("Threads count = " + tc2);
//      tc = Thread.enumerate(tarray);
//      for (int i=0; i<tc; i++)
//        System.out.println("Thread[" + i + "] = " + tarray[i].getName());

      System.gc();
      System.gc();
      long m3 = Runtime.getRuntime().maxMemory();
      long m4 = Runtime.getRuntime().freeMemory();

      System.out.println("dt=" + (end-start) + ", m1=" + m1 + ", m2=" + m2 + ", m3=" + m3 + ", m4=" + m4);

//      Thread.sleep(120000);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();     
    }
  }
}
