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
 * Tests basic server reconfiguration:
 *  - Adds one domain (D0) and one server (S1).
 *  - Adds a server S2.
 *  - Crash server S0 and restarts it.
 *  - Stops server S1 and restarts it.
 *  - Crash server S2 and restarts it.
 *  - Removes the servers S1 and S2, then the domain D0.
 *  
 * This test works is specialized for HttpNetwork.
 */
public class ReconfTest5 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest5().run();
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
      
      // Adds a second server S2
      
      AdminModule.addServer(2, "localhost", "D0", 0, "s2");
      deployAgentServer((short) 2, "./s2");
      startAgentServer((short) 2, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });

      checkQueue((short) 2);

      System.out.println("trace1: " + AdminModule.getConfiguration());
      
      // Stops the server S0 then restart it

      AdminModule.disconnect();
      
      crashAgentServer((short) 0);
      Thread.sleep(1000L);
      startAgentServer((short) 0, new String[] { "-DNTNoLockFile=true" });
      Thread.sleep(1000L);

      AdminModule.connect("localhost", 2560, "root", "root", 60);

      checkQueue((short) 0);
      System.out.println("trace1: " + AdminModule.getConfiguration());
      checkQueue((short) 1);      
      checkQueue((short) 2);
      
      // Stops the server S1 then restart it
      
      AdminModule.stopServer(1);
      Thread.sleep(2000L);
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });

      checkQueue((short) 1);
      checkQueue((short) 2);
      
      // Crash the server S2 then restart it
      
      crashAgentServer((short) 2);
      Thread.sleep(1000L);
      startAgentServer((short) 2, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      
      // Removes the servers S1 and S2, and the domain D0
      
      AdminModule.stopServer(1);
      AdminModule.removeServer(1);
      
      checkQueue((short) 2);
      
      AdminModule.stopServer(2);
      AdminModule.removeServer(2);
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
