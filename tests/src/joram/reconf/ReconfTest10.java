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

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.objectweb.joram.client.jms.admin.AdminModule;
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
public class ReconfTest10 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest10().run();
  }

  public void run() {
    try {
      String network = System.getProperty("Network", SimpleNetwork.class.getName());
      startAgentServer((short)0, (File)null, new String[] {"-DNTNoLockFile=true"});

      Thread.sleep(1000L);
      
      TcpConnectionFactory cf0 = (TcpConnectionFactory) TcpConnectionFactory.create("localhost", 2560);
      cf0.getParameters().connectingTimer = 20;
      AdminModule.connect((ConnectionFactory) cf0, "root", "root" );
      
      User.create("anonymous", "anonymous", 0);
      
      // Adds a domain D0 and a server S1.
      
      AdminModule.addDomain("D0", network, 0, 17770);

      AdminModule.addServer(1, "localhost", "D0", 17771, "s1",
                            new String[]{"org.objectweb.joram.mom.proxies.tcp.TcpProxyService"},
                            new String[]{"2561"});

      System.out.println("trace1: " + AdminModule.getConfiguration());
      
      deployAgentServer((short) 1, "./s1");
      startAgentServer((short) 1, new File("./s1"), new String[] {"-DNTNoLockFile=true"});
      
      Queue q0 = checkQueue((short) 0);
      Queue q1 = checkQueue((short) 1);

      ConnectionFactory cf1 = TcpConnectionFactory.create("localhost", 2560);

      checkQueue(cf1, q0);
      checkQueue(cf1, q1);
      
      // First stop the server because it must be reachable in order to be stopped.
      AdminModule.stopServer(1);
      // Then clean the configuration: the server is not reachable anymore.
      AdminModule.removeServer(1);

      AdminModule.removeDomain("D0");

      checkQueue((short) 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      endTest();
    }
  }
}
