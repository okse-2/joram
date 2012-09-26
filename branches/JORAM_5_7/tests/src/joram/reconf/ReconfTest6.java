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

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.HttpNetwork;

/**
 * Tests basic server reconfiguration: (2 domains with routing)
 *  - Adds one domain (D0) and one server (S1).
 *  - Adds a second domain (D1 to server S1) and another server (S2).
 *  - Stops the server S1 then restarts it.
 *  - Removes S2, then S1.
 *  
 * This test works is specialized for HttpNetwork.
 */
public class ReconfTest6 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest6().run();
  }

  public void run() {
    try {
      String network = HttpNetwork.class.getName();
      startAgentServer((short) 0, new String[] { "-DNTNoLockFile=true" });

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      
      checkQueue((short) 0);

      // Adds a domain D0 and a server S1
      
      AdminModule.addDomain("D0", network, 0, 17770);

      AdminModule.addServer(1, "localhost", "D0", 0, "s1");
      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      
      checkQueue((short) 1);

      // Adds a second domain D1 (routed by S1) and a server S2

      AdminModule.addDomain("D1", network, 1, 18770);

      //System.out.println("Add server s2");
      AdminModule.addServer(2, "localhost", "D1", 0, "s2");
      deployAgentServer((short) 2, "./s2");
      startAgentServer((short) 2, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      
      checkQueue((short) 1);
      checkQueue((short) 2);

      // Stops the server S1 then restart it

      crashAgentServer((short) 1);
      Thread.sleep(1000L);
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 1);
      checkQueue((short) 2);

      // Adds a server S3 to domain D0

      AdminModule.addServer(3, "localhost", "D0", 0, "s3");
      deployAgentServer((short) 3, "./s3");
      startAgentServer((short) 3, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });
      
      checkQueue((short) 3);

      // Stops the server S3 then restart it

      crashAgentServer((short) 3);
      Thread.sleep(1000L);
      startAgentServer((short) 3, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 3);

      // Adds a server S4 to domain D1

      AdminModule.addServer(4, "localhost", "D1", 0, "s4");
      deployAgentServer((short) 4, "./s4");
      startAgentServer((short) 4, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s4/a3servers.xml" });
      
      checkQueue((short) 4);

      // Stops the server S4 then restart it

      crashAgentServer((short) 4);
      Thread.sleep(1000L);
      startAgentServer((short) 4, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s4/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 4);

      // Stops the server S1 then restart it

      crashAgentServer((short) 1);
      Thread.sleep(1000L);
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 1);
      checkQueue((short) 2);
      checkQueue((short) 4);

      // Removes server S2, S4 and domain D1

      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(2);
      AdminModule.stopServer(4);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(2);
      AdminModule.removeServer(4);
      // Removes the corresponding domain
      AdminModule.removeDomain("D1");

      checkQueue((short) 1);
      
      // Removes server S1, S3 and domain D0

      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(1);
      AdminModule.stopServer(3);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(1);
      AdminModule.removeServer(3);
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
      killAgentServer((short) 3);
      killAgentServer((short) 4);
      endTest();
    }
  }
}
