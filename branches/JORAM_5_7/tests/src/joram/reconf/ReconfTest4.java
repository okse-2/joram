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

import fr.dyade.aaa.agent.SimpleNetwork;

/**
 * Tests basic server reconfiguration: a unique server in the initial configuration
 *  - Adds a domain D0.
 *  - Adds 5 servers (S1, S2, S3, S4 and S5) in the domain D0, then starts them.
 *  - Crashes S1 then removes it.
 *  - Adds S6 in the domain D0.
 *  - Crashes S3, restarts it, then removes it.
 *  - Iteratively removes the servers S2, S3, S4, S5 and S6, then the unused domain (D0).
 *  
 * This test works with classic networks: SimpleNetwork, PoolNetwork, etc.
 */
public class ReconfTest4 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest4().run();
  }

  public void run() {
    try {
      String network = System.getProperty("Network", SimpleNetwork.class.getName());
      startAgentServer((short) 0, new String[] { "-DNTNoLockFile=true" });

      Thread.sleep(1000L);

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      
      // Adds a domain D0 and servers S1, S2, S3, S4 and S5
      
      AdminModule.addDomain("D0", network, 0, 17770);

      AdminModule.addServer(1, "localhost", "D0", 17771, "s1");
      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      
      checkQueue((short) 1);

      AdminModule.addServer(2, "localhost", "D0", 17772, "s2");
      deployAgentServer((short) 2, "./s2");
      startAgentServer((short) 2, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      
      checkQueue((short) 2);

      AdminModule.addServer(3, "localhost", "D0", 17773, "s3");
      deployAgentServer((short) 3, "./s3");
      startAgentServer((short) 3, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });
      
      checkQueue((short) 3);

      AdminModule.addServer(4, "localhost", "D0", 17774, "s4");
      deployAgentServer((short) 4, "./s4");
      startAgentServer((short) 4, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s4/a3servers.xml" });
      
      checkQueue((short) 4);

      AdminModule.addServer(5, "localhost", "D0", 17775, "s5");
      deployAgentServer((short) 5, "./s5");
      startAgentServer((short) 5, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s5/a3servers.xml" });
      
      checkQueue((short) 0);
      checkQueue((short) 1);
      checkQueue((short) 2);
      checkQueue((short) 3);
      checkQueue((short) 4);
      checkQueue((short) 5);
      
      // Stops the server S1

      crashAgentServer((short) 1);
      Thread.sleep(1000L);
      
      checkQueue((short) 0);
      checkQueue((short) 2);
      checkQueue((short) 3);
      checkQueue((short) 4);
      checkQueue((short) 5);
      
      Thread.sleep(1000L);
      AdminModule.removeServer(1);
      
      checkQueue((short) 0);
      checkQueue((short) 2);
      checkQueue((short) 3);
      checkQueue((short) 4);
      checkQueue((short) 5);

      // Adds a server S6
      
      AdminModule.addServer(6, "localhost", "D0", 17776, "s6");
      deployAgentServer((short) 6, "./s6");
      startAgentServer((short) 6, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s6/a3servers.xml" });
      
      checkQueue((short) 0);
      checkQueue((short) 2);
      checkQueue((short) 3);
      checkQueue((short) 4);
      checkQueue((short) 5);
      checkQueue((short) 6);

      // Stops the server S1 then restart it

      crashAgentServer((short) 3);
      Thread.sleep(1000L);
      startAgentServer((short) 3, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 0);
      checkQueue((short) 2);
      checkQueue((short) 3);
      checkQueue((short) 4);
      checkQueue((short) 5);
      checkQueue((short) 6);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(2);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(2);

      checkQueue((short) 3);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(3);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(3);
      
      checkQueue((short) 4);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(4);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(4);
      
      checkQueue((short) 5);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(5);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(5);
      
      checkQueue((short) 6);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(6);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(6);

      checkQueue((short) 0);

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
      killAgentServer((short) 5);
      killAgentServer((short) 6);
      endTest();
    }
  }
}
