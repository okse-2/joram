/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.reconf;

import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.NameAlreadyUsedException;
import org.objectweb.joram.client.jms.admin.StartFailureException;
import org.objectweb.joram.client.jms.admin.User;

/**
 * Tests basic server reconfiguration: Check various errors during reconfiguration
 *  - Bad port number.
 *  - Domain already exist.
 *  - Try to remove non empty domain.
 *  - Try to remove a routing server.
 */
public class ReconfTest2 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest2().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0, new String[0]);

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      
      checkQueue((short) 0);

      try {
        AdminModule.addDomain("D0", 0, 99999999);
      } catch (Exception exc) {
        assertTrue("Exception expected: port out of range", exc instanceof StartFailureException);
      }

      AdminModule.addDomain("D0", 0, 17770);

      try {
        AdminModule.addDomain("D0", 0, 17770);
      } catch (Exception exc) {
        //System.out.println("Expected error: " + exc);
        assertTrue("Exception expected: domain name already used", exc instanceof NameAlreadyUsedException);
      }

      AdminModule.addServer(1, "localhost", "D0", 17771, "s1");

      try {
        AdminModule.removeDomain("D0");
      } catch (Exception exc) {
        //System.out.println("Expected error: " + exc);
        assertTrue("Exception expected: domain contains more than 1 server", exc instanceof AdminException);
      }

      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new String[] { "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      checkQueue((short) 1);

      AdminModule.addDomain("D1", 1, 18770);

      AdminModule.addServer(2, "localhost", "D1", 18771, "s2");
      deployAgentServer((short) 2, "s2");
      startAgentServer((short) 2, new String[] { "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      checkQueue((short) 2);

      try {
        AdminModule.removeServer(1);
      } catch (Exception exc) {
        assertTrue("Exception expected: server belongs to more than 1 domain", exc instanceof AdminException);
      }

      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(2);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(2);
      // Removes the corresponding domain
      AdminModule.removeDomain("D1");

      checkQueue((short) 1);
      
      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(1);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(1);
      // Removes the corresponding domain
      AdminModule.removeDomain("D0");
      
      checkQueue((short) 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      killAgentServer((short) 1);
      killAgentServer((short) 2);
      endTest();
    }
  }

}
