/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
package joram.admin;

import javax.jms.ConnectionFactory;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.UnknownServerException;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.mom.proxies.ConnectionManager;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.ServerDesc;
import framework.TestCase;

/**
 * This test checks the result of some
 * {@link AdminModule#invokeStaticServerMethod(String, String, Class[], Object[])}
 * calls.
 */
public class AdminTest7 extends TestCase {

  public static void main(String[] args) {
    new AdminTest7().run();
  }

  public void run() {
    try {

      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000);

      // Create an administration connection on server #0
      ConnectionFactory cf = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf).getParameters().connectingTimer = 60;

      AdminModule.connect(cf, "root", "root");

      String result = AdminModule.invokeStaticServerMethod(0, "fr.dyade.aaa.agent.AgentServer",
          "getStatusInfo", new Class[0], new Object[0]);
      assertEquals(AgentServer.getStatusInfo(), result);

      result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getStatusInfo", null,
          new Object[0]);
      assertEquals(AgentServer.getStatusInfo(), result);

      result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getStatusInfo",
          new Class[0], null);
      assertEquals(AgentServer.getStatusInfo(), result);

      result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getStatusInfo", null,
          null);
      assertEquals(AgentServer.getStatusInfo(), result);

      result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getHostname",
          new Class[] { Short.TYPE }, new Object[] { new Short((short) 0) });
      assertEquals(AgentServer.getHostname((short) 0), result);

      result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getServerId",
          new Class[0], new Object[0]);
      assertEquals(AgentServer.getServerId(), new Short(result).shortValue());

      result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getServiceArgs",
          new Class[] { Short.TYPE, String.class }, new Object[] { new Short((short) 0),
              ConnectionManager.class.getName() });
      assertEquals(AgentServer.getServiceArgs((short) 0, ConnectionManager.class.getName()), result);
      

      // *** Test some erroneous calls
      // incorrect number of args
      AdminException expectedExc = null;
      try {
        result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getHostname",
            new Class[] { Short.TYPE }, null);
      } catch (AdminException exc) {
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      // Incorrect parameter type
      expectedExc = null;
      try {
        result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getHostname",
            new Class[] { Integer.TYPE }, new Object[] { new Integer(0) });
      } catch (AdminException exc) {
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      // The resulting call throws an exception
      expectedExc = null;
      try {
        result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "getServiceArgs",
            new Class[] { Short.TYPE, String.class }, new Object[] { new Short((short) 0),
                "IncorrectParameter" });
      } catch (AdminException exc) {
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      // Use forbidden class (due to c/s serialization)
      expectedExc = null;
      try {
        result = AdminModule.invokeStaticServerMethod("fr.dyade.aaa.agent.AgentServer", "addServerDesc",
            new Class[] { ServerDesc.class }, new Object[] { new ServerDesc((short) 1, "s1", "localhost",
                12345) });
      } catch (AdminException exc) {
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      // Incorrect server
      expectedExc = null;
      try {
        result = AdminModule.invokeStaticServerMethod(8, "fr.dyade.aaa.agent.AgentServer", "getHostname",
            new Class[] { Short.TYPE }, new Object[] { new Short((short) 0) });
      } catch (UnknownServerException exc) {
        expectedExc = exc;
      }
      assertNotNull(expectedExc);

      // *** Test on another server
      startAgentServer((short) 1);
      result = AdminModule.invokeStaticServerMethod(1, "fr.dyade.aaa.agent.AgentServer", "getServerId", null, null);
      assertEquals("1", result);

      AdminModule.disconnect();

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 1);
      endTest();     
    }
  }
}
