/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):Badolle Fabien (ScalAgent D.T.)
 * Contributor(s): 
 */
package joram.cluster;


import org.objectweb.joram.client.jms.Topic;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;

import framework.TestCase;

/**
 * Test : can't cluster an already clustered topic
 * 
 */
public class Test2 extends TestCase {

  public static void main(String[] args) {
    new Test2().run();
  }

  public void run() {
    try {
      System.out.println("server start");
      startAgentServer((short) 0);
      startAgentServer((short) 1);
      startAgentServer((short) 2);
      startAgentServer((short) 3);

      admin();
      System.out.println("admin config ok");

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      System.out.println("Server stop ");
      stopAgentServer((short) 0);
      stopAgentServer((short) 1);
      stopAgentServer((short) 2);
      stopAgentServer((short) 3);
      endTest();
    }
  }

  public void admin() throws Exception {
    // conexion
    AdminModule.connect("root", "root", 60);

    Topic top0 = Topic.create(0);
    Topic top1 = Topic.create(1);
    Topic top2 = Topic.create(2);
    Topic top3 = Topic.create(3);

    top0.addClusteredTopic(top1);
    top2.addClusteredTopic(top3);

    AdminException exc = null;
    try {
      top0.addClusteredTopic(top2);
    } catch (AdminException aexc) {
      exc = aexc;
    }
    assertNotNull(exc);

    assertEquals(2, top0.getClusterFellows().size());
    assertEquals(2, top1.getClusterFellows().size());
    assertEquals(2, top2.getClusterFellows().size());
    assertEquals(2, top3.getClusterFellows().size());

    AdminModule.disconnect();
  }
}
