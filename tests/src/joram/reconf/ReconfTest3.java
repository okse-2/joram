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

/**
 * Tests basic server reconfiguration: 2 initial servers in a SimpleNetwork domain
 *  - Adds a third server (S2) in the domain D0, then removes it.
 *  - Adds a third server (S3) in the domain D0, then removes it.
 *  - Removes S2 and the useless domain (D0).
 *  
 * This test works with classic networks: SimpleNetwork, PoolNetwork, etc.
 */
public class ReconfTest3 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest3().run();
  }

  public void run() {
    try {
      startAgentServer((short) 0, new String[0]);
      startAgentServer((short) 1, new String[0]);

      Thread.sleep(1000L);

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);

      checkQueue((short) 0);
      checkQueue((short) 1);

      AdminModule.addServer(2, "localhost", "D0", 17772, "s2");
      deployAgentServer((short) 2, "./s2");
      startAgentServer((short) 2, new String[] { "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      
      checkQueue((short) 2);
      checkQueue((short) 1);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(2);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(2);

      checkQueue((short) 1);

      AdminModule.addServer(3, "localhost", "D0", 17773, "./s3");
      deployAgentServer((short) 3, "s3");
      startAgentServer((short) 3, new String[] { "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });

      checkQueue((short) 3);
      checkQueue((short) 1);

      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(3);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(3);

      checkQueue((short) 1);

      AdminModule.stopServer(1);
      AdminModule.removeServer(1);

      AdminModule.removeDomain("D0");

      checkQueue((short) 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Stop server s0");
      stopAgentServer((short) 0);
      killAgentServer((short) 1);
      killAgentServer((short) 2);
      killAgentServer((short) 3);
      endTest();
    }
  }
}
