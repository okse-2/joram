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

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.Server;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;

import fr.dyade.aaa.agent.SimpleNetwork;

/**
 * Tests basic server reconfiguration: 2 initial servers in a SimpleNetwork domain
 *  - Adds a third server (S2) in the domain D0, then removes it.
 *  - Adds a third server (S3) in the domain D0, then removes it.
 *  - Removes S2 and the useless domain (D0).
 *  
 * This test works with classic networks: SimpleNetwork, PoolNetwork, etc.
 */
public class ReconfTest11 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest11().run();
  }

  public void run() {
    try {
      String network = System.getProperty("Network", SimpleNetwork.class.getName());
      startAgentServer((short) 0, new String[] { "-DTransaction.UseLockFile=false" });

      Thread.sleep(1000L);
      
      ConnectionFactory cf0 = TcpConnectionFactory.create("localhost", 2560);
      ((TcpConnectionFactory) cf0).getParameters().connectingTimer = 20;
      AdminModule.connect(cf0, "root", "root");
      
      User.create("anonymous", "anonymous", 0);
      
      // Adds a domain D0.
      
      AdminModule.addDomain("D0", network, 0, 17770);

      // Adds a server s1, deploy it and start it.
      
      AdminModule.addServer(1, "localhost", "D0", 17771, "s1",
                            new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
                            new String[]{"2561"});

      System.out.println("trace1: " + AdminModule.getConfiguration());
      
      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });

      Queue q0 = checkQueue((short) 0);
      Queue q1 = checkQueue((short) 1);

      // Adds a server s2, deploy it and start it.

      AdminModule.addServer(2, "localhost", "D0", 17772, "s2",
                            new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
                            new String[]{"2562"});

      System.out.println("trace2: " + AdminModule.getConfiguration());
      
      deployAgentServer((short) 2, "./s2");
      startAgentServer((short) 2, new String[] { "-DTransaction.UseLockFile=false",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s2/a3servers.xml" });
      
      Queue q2 = checkQueue((short) 2);

      // Adds a server s3, deploy it but don't start it.

      AdminModule.addServer(3, "localhost", "D0", 17773, "s3",
                            new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
                            new String[]{"2563"});

      System.out.println("trace3: " + AdminModule.getConfiguration());

      // Adds a server s4, deploy it and start it.

      AdminModule.addServer(4, "localhost", "D0", 17774, "s4",
                            new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
                            new String[]{"2564"});

      System.out.println("trace4: " + AdminModule.getConfiguration());
      
      deployAgentServer((short) 4, "./s4");
      startAgentServer((short) 4, new String[] { "-DTransaction.UseLockFile=false",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s4/a3servers.xml" });
      
      Queue q4 = checkQueue((short) 4);

      // Test all running servers through a connection to server s1
      
      ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 2561);
      
      User.create("anonymous", "anonymous", 1);

      checkQueue(cf1, q0);
      checkQueue(cf1, q1);
      checkQueue(cf1, q2);
      checkQueue(cf1, q4);
      
      System.out.println("trace3");
      
      Map<String,Boolean> retValue = new HashMap();
      try {
        Server[] servers = AdminModule.getServers();
        AdminModule.disconnect();

        if( servers != null ) {
          for( int i=0; i<servers.length; i++ ) {
            String serverName = servers[i].getName() + " (" + servers[i].getHostName() + ")";
            int sid = servers[i].getId();
            try {
              ConnectionFactory cfi = TcpConnectionFactory.create(servers[i].getHostName(), 2560 + sid);
              ((TcpConnectionFactory) cfi).getParameters().connectingTimer = 20;
              AdminModule.connect(cfi, "root", "root");

              System.out.println("trace4: " + sid + " ok");
            } catch( Throwable t ) {
              System.out.println("trace4: " + sid + " nok");
              t.printStackTrace();
            } finally {
              AdminModule.disconnect();
            }
          }
        }
      } finally {
          AdminModule.disconnect();
      }

      AdminModule.connect(cf0, "root", "root");
      
      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(1);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(1);
      
      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(2);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(2);
      
//      // First stop the server because it must be reachable in order to be stopped.
//      AdminModule.stopServer(3);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(3);
      
      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(4);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(4);

      AdminModule.removeDomain("D0");

      checkQueue((short) 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      killAgentServer((short) 1);
      killAgentServer((short) 2);
      killAgentServer((short) 4);
      endTest();
    }
  }
  
}
