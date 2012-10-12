/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package a3.base;

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.DeleteNot;
import framework.BaseTestCase;
import framework.TestCase;

/**
 * This test verify the sending of notifications during agentInitialize and
 * agentFinalize.
 */
public class test20 extends TestCase {
  public test20() {
    super();
  }

  public void setUp() throws Exception {
    timeout = 15000L;
    startAgentServer((short) 1);

    Thread.sleep(1000L);

    TestAgent20A A = new TestAgent20A();
    TestAgent20B B = new TestAgent20B((short) 1);

    A.B = B.getId();
    B.A = A.getId();

    A.deploy();
    B.deploy();
  }

  public static void main(String args[]) {
    new test20().runTest(args);
  }
  
  protected void tearDown() {
    killAgentServer((short) 1);
  }

  static class TestAgent20A extends Agent {
    int state = 0;

    AgentId B;

    public TestAgent20A() {
      super();
    }

    public void react(AgentId from, Notification not) throws Exception {
//       System.out.println("react(" + from + ", " + not + ")");

      BaseTestCase.assertEquals(Notification.class.getName(),
                                not.getClass().getName());
      if (state == 0) {
        BaseTestCase.assertTrue(from.equals(B));
        stopAgentServer((short) 1);
        // Be careful the notification sent during agentFinalize could be
        // jammed by the server stop. So we have to retart the server to
        // receive it !!
        Thread.sleep(1000L);
        startAgentServer((short) 1);
        sendTo(B, new Notification());
      } else if (state == 1) {
        BaseTestCase.assertTrue(from.equals(B));
        stopAgentServer((short) 1);
        BaseTestCase.endTest();
      }
      state += 1;
    }
  }

  static class TestAgent20B extends Agent {
    AgentId A;

    public TestAgent20B(short to) {
      super(to);
    }

    public void agentInitialize(boolean firsttime) {
      sendTo(A, new Notification());
    }

    public void react(AgentId from, Notification not) {
    }
  }
}
