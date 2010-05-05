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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s):
 */
package joram.recovery;

import javax.jms.Connection;
import javax.jms.Session;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.common.Configuration;
import fr.dyade.aaa.util.NTransaction;

/**
 * Check transaction properties after a restart of the agent server.
 */
public class Recover_5 extends framework.TestCase {
  
  static Connection cnx;
  static Session session;

  public static void main(String args[]) throws Exception {
    new Recover_5().run();
  }

  public void run() {
    try {
      System.out.println("Start Server#0");
      
      Configuration.putProperty("NTLogFileSize", "88");
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();

      Thread.sleep(1000L);
      
      // Check a3servers.xml properties
      assertEquals(33, ((NTransaction) AgentServer.getTransaction()).getMaxLogFileSize());
      assertEquals(32768, ((NTransaction) AgentServer.getTransaction()).getLogThresholdOperation());
      assertEquals(2048, ((NTransaction) AgentServer.getTransaction()).getMaxLogMemorySize());
      assertEquals(500, ((NTransaction) AgentServer.getTransaction()).getLogMemoryCapacity());
      
      System.out.println("Stop Server#0");
      AgentServer.stop();
      AgentServer.reset();

      // Change the properties
      Configuration.putProperty("NTLogFileSize", "88");
      Configuration.putProperty("Transaction", "fr.dyade.aaa.FakeClass");
      Configuration.putProperty("NTLogMemoryCapacity", "1000");
      Thread.sleep(1000L);
      
      System.out.println("Start Server#0");
      AgentServer.init((short) 0, "s0", null);
      AgentServer.start();
      
      Thread.sleep(1000L);
      
      // Verify the properties after a restart.
      assertEquals(33, ((NTransaction) AgentServer.getTransaction()).getMaxLogFileSize());
      assertEquals(32768, ((NTransaction) AgentServer.getTransaction()).getLogThresholdOperation());
      assertEquals(2048, ((NTransaction) AgentServer.getTransaction()).getMaxLogMemorySize());
      assertEquals(500, ((NTransaction) AgentServer.getTransaction()).getLogMemoryCapacity());

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      AgentServer.stop();
      endTest();
    }
  }
}
