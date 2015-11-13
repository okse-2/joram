/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - 2009 ScalAgent Distributed Technologies
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
package joram.local;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.local.LocalConnectionFactory;

import fr.dyade.aaa.agent.AgentServer;

/**
 * Test local connection without sever init and with server init.
 */
public class Test2 extends framework.TestCase {
  public static void main(String args[]) throws Exception {
    new Test2().run();
  }

  public void run() {
    try {
      Exception expectedExc = null;
      try {
        System.out.println("Should throw an Exception.");
        ConnectionFactory cf = LocalConnectionFactory.create();
        cf.createConnection();
      } catch (Exception exc) {
        exc.printStackTrace();
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      AgentServer.init((short) 0, "s0", null);

      expectedExc = null;
      try {
        System.out.println("Should throw an exception.");
        ConnectionFactory cf = LocalConnectionFactory.create();
        Connection cnx = cf.createConnection();
        cnx.close();
      } catch (Exception exc) {
        exc.printStackTrace();
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      AgentServer.start();

      Thread.sleep(1000L);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}
