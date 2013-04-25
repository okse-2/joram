/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;

import fr.dyade.aaa.agent.SimpleNetwork;

/**
 * Tests error during reconfiguration:
 *  - Starts server S1 before adding it to the configuration.
 *  - Adds S1 to domain D0 and tests it.
 *  
 * This test works with classic networks: SimpleNetwork, PoolNetwork, etc.
 */
public class ReconfTest12 extends ReconfTestBase {

  public static void main(String[] args) {
    new ReconfTest12().run();
  }

  public void run() {
    try {
      String network = System.getProperty("Network", SimpleNetwork.class.getName());
      startAgentServer((short) 0, new String[] {"-DTransaction.UseLockFile=false"});

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      
      checkQueue((short) 0);

      // Adds a domain D0 and a server S1      
      AdminModule.addDomain("D0", network, 0, 17770);

      Thread.sleep(2000L);
      
      File sdir = new File("./s1");
      sdir.mkdir();
      
      String configXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<config>\n" +
      "  <property name=\"Transaction\" value=\"fr.dyade.aaa.ext.NGTransaction\"/>\n" +
      "  <domain name=\"D0\" network=\"" +  network + "\"/>\n" +
      "  <server id=\"0\" name=\"s0\" hostname=\"localhost\">\n" +
      "    <network domain=\"D0\" port=\"17770\"/>\n" +
      "    <service class=\"fr.dyade.aaa.agent.AdminProxy\" args=\"7890\"/>\n" +
      "    <service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\" args=\"root root\"/>\n" +
      "    <service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\" args=\"2560\"/>\n" +
      "  </server>\n" +
      "  <server id=\"1\" name=\"s1\" hostname=\"localhost\">\n" +
      "    <network domain=\"D0\" port=\"17771\"/>\n" +
      "    <service class=\"fr.dyade.aaa.agent.AdminProxy\" args=\"7891\"/>\n" +
      "    <service class=\"org.objectweb.joram.mom.proxies.ConnectionManager\" args=\"root root\"/>\n" +
      "    <service class=\"org.objectweb.joram.mom.proxies.tcp.TcpProxyService\" args=\"2561\"/>\n" +
      "  </server>\n" +
      "</config>\n";
      
      File sconfig = new File(sdir, "a3servers.xml");
      
      FileOutputStream fos = new FileOutputStream(sconfig);
      PrintWriter pw = new PrintWriter(fos);
      pw.println(configXml);
      pw.flush();
      pw.close();
      fos.close();
      
      startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false",
          "-Dfr.dyade.aaa.agent.A3CONF_FILE=./s1/a3servers.xml" });
      
      Thread.sleep(2000L);
     
      AdminModule.addServer(1, "localhost", "D0", 17771, "s1");
      
      checkQueue((short) 1);
      
      // Removes server S1, S3 and domain D0

      // First stop the server (it must be reachable in order to be stopped).
      AdminModule.stopServer(1);
      // Removes the corresponding domain
      AdminModule.removeDomain("D0");
      
      checkQueue((short) 0);
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short) 0);
      killAgentServer((short) 1);
      endTest();
    }
  }
}
