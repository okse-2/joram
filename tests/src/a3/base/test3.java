/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */

package a3.base;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test3 extends TestCase {
  public test3() {
    super();
  }
  
  boolean router = false;

  protected void setUp() throws Exception {
    timeout = 1200000L;
    router = new Boolean(System.getProperty("Router",
                                            "false")).booleanValue();
    startAgentServer((short) 1);
    if (router) startAgentServer((short) 2);

    Test agent = new Test();
    agent.deploy();
    Channel.sendTo(agent.getId(), new Notification());
  }

  protected void tearDown() {
    crashAgentServer((short) 1);
    if (router) crashAgentServer((short) 2);
  }

  public static void main(String args[]) {
    new test3().runTest(args);
  }

  static class Test extends Agent {
    int state = 0;
    AgentId echo;

    public Test() {
      super();
    }

    public void react(AgentId from, Notification not) {
      try {
        switch (state) {
        case 0:
          assertTrue(from.isNullId());
          assertEquals(not.getClass().getName(),
                       "fr.dyade.aaa.agent.Notification");
          Echo agent = new Echo((short) 1);
          echo = agent.getId();
          agent.deploy(getId());
          break;
        case 1:
          assertEquals(from,
                       new AgentId((short) 1,(short) 1,
                                   AgentId.FactoryIdStamp));
          assertEquals(not.getClass().getName(),
                       "fr.dyade.aaa.agent.AgentCreateReply");
          sendTo(echo, new Notification());
          break;
        case 2:
          assertEquals(echo, from) ;
          assertEquals(not.getClass().getName(),
                       "fr.dyade.aaa.agent.Notification");
          endTest();
          // never reached
        }
        state += 1;
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
  }
}
