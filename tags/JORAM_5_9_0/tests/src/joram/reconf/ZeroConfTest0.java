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

import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.admin.User;
import org.objectweb.joram.client.jms.admin.server.ZeroconfJoramServer;

import fr.dyade.aaa.agent.SimpleNetwork;

/**
 * Tests ZeroConfJoramServer class:
 *  - Creates a new Joram server, starts it and tests it.
 */
public class ZeroConfTest0 extends ReconfTestBase {

  public static void main(String[] args) {
    new ZeroConfTest0().run();
  }

  public void run() {
    try {
      startAgentServer((short)0, new String[] {"-DTransaction.UseLockFile=false"});

      AdminModule.connect("localhost", 2560, "root", "root", 60);
      User.create("anonymous", "anonymous", 0);
      checkQueue((short) 0);

      Process s1 = startProcess("org.objectweb.joram.client.jms.admin.server.ZeroconfJoramServer",
                                new String[] {"-D" + ZeroconfJoramServer.ADMIN_HOST_NAME + "=localhost",
                                              "-D" + ZeroconfJoramServer.ADMIN_PORT + "=2560"},
                                new String[] {});
      Thread.sleep(10000);
      checkQueue((short) 1);
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
