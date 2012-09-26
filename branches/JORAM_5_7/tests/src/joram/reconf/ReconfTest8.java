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
 *  - Adds one HttpNetwork domain (D0) with s0 'client'.
 *  - Adds a server (S1) 'server' of D0.
 *  - Adds a server S2 'client' of D0.
 *  - Stops and restart S1.
 *  - Adds a server S3 'client' of D0.
 *  - Stops the server S2 then removes it.
 *  - Stops and restart S3.
 *  - Stops and restart S1.
 *  - Stops the server S3 then removes it.
 *  - Stops the server S1 then removes it.
 *  - Removes D0.
 *  
 * This test works is specialized for HttpNetwork.
 */
public class ReconfTest8 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest8().run();
  }

  public void run() {
    try {
      String network = HttpNetwork.class.getName();
      startAgentServer((short) 0, new String[] { "-DNTNoLockFile=true" });

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      
      checkQueue((short) 0);

      // Adds a domain D0 (Server S0 client)
      
      AdminModule.addDomain("D0", network, 0, 0);
      
      checkQueue((short) 0);     

      // Adds a server S1
      
      AdminModule.addServer(1, "localhost", "D0", 17770, "s1");
      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      
      checkQueue((short) 0);     
      checkQueue((short) 1);

      // Adds a server S2

      AdminModule.addServer(2, "localhost", "D0", 0, "s2");
      deployAgentServer((short) 2, "./s2");
      startAgentServer((short) 2, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      
      checkQueue((short) 0);     
      checkQueue((short) 1);
      checkQueue((short) 2);

      // Stops the server S1 then restart it

      crashAgentServer((short) 1);
      Thread.sleep(1000L);
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 0);     
      checkQueue((short) 1);
      checkQueue((short) 2);

      // Adds a server S3 to domain D0

      AdminModule.addServer(3, "localhost", "D0", 0, "s3");
      deployAgentServer((short) 3, "./s3");
      startAgentServer((short) 3, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });
      
      checkQueue((short) 0);     
      checkQueue((short) 1);
      checkQueue((short) 2);
      checkQueue((short) 3);
      
      // Removes the server S2
      
      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(2);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(2);

      checkQueue((short) 3);

      // Stops the server S3 then restart it

      System.out.println("crash and restart #3"); Thread.sleep(5000L);
      crashAgentServer((short) 3);
      Thread.sleep(1000L);
      startAgentServer((short) 3, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s3/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 3);

      // Stops the server S1 then restart it

      System.out.println("crash and restart #1"); Thread.sleep(5000L);
      crashAgentServer((short) 1);
      Thread.sleep(1000L);
      startAgentServer((short) 1, new String[] { "-DNTNoLockFile=true",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      Thread.sleep(1000L);
      
      checkQueue((short) 1);
      checkQueue((short) 3);
      
      // Removes server S3 
      
      System.out.println("stop and remove #3"); Thread.sleep(5000L);
      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(3);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(3);
      
      checkQueue((short) 1);
      
      // Removes server S1 
      
      System.out.println("stop and remove #1"); Thread.sleep(5000L);
      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(1);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(1);

      checkQueue((short) 0);
      
      // Removes the corresponding domain
      System.out.println("remove D0"); Thread.sleep(5000L);
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
      endTest();
    }
  }
}
