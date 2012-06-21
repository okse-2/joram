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
 *  - Adds one HttpNetwork domain (D0) with s0 'client'.
 *  - Adds a server (S1) 'server' of D0.
 *  - Stops the server S1 then removes it.
 *  - Removes D0.
 *  
 * This test works is specialized for HttpNetwork.
 */
public class ReconfTest7 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest7().run();
  }

  public void run() {
    try {
      String network = HttpNetwork.class.getName();
      startAgentServer((short) 0, new String[] { "-DTransaction.UseLockFile=false" });

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      
      AdminModule.getLocalServer();
      
      checkQueue((short) 0);

      // Adds a domain D0 (Server S0 client)
      
      AdminModule.addDomain("D0", network, 0, 0);
      
      checkQueue((short) 0);     

      // Adds a server S1
      
      AdminModule.addServer(1, "localhost", "D0", 17770, "s1");
      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      
      checkQueue((short) 0);     
      checkQueue((short) 1);
      
      // Removes server S1 
      
      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(1);
      // Cleans the configuration: the server is not reachable anymore.
      AdminModule.removeServer(1);

      checkQueue((short) 0);
      
      // Removes the corresponding domain
      
      System.out.println("remove D0");
      
      System.out.println("D0 removed");
      
      checkQueue((short) 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      crashAgentServer((short) 0);
      killAgentServer((short) 1);
      endTest();
    }
  }
}
