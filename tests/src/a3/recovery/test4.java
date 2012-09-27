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

package a3.recovery;

import fr.dyade.aaa.agent.*;
import framework.TestCase;
import a3.base.Echo;

public class test4 extends TestCase {
  public test4() {
    super();
  }

  public static short remote = (short) 1;
  public static long pause = 15000L;

  protected void setUp() throws Exception {
    pause = Long.getLong("pause", 15000L).longValue();
    timeout = pause +1200000L;
    Test4Agent agent = new Test4Agent();
    agent.deploy();
    Channel.sendTo(agent.getId(), new Notification());
  }

  protected void tearDown() {
    killAgentServer(remote);
  }

  public static void main(String args[]) {
    new test4().runTest(args);
  }
}

class Test4Agent extends Agent {
  int state = 0;
  AgentId echo;

  public Test4Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    try {
      switch (state) {
      case 0:
        TestCase.assertTrue("step#" + state, from.isNullId());
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");
        Echo agent = new Echo(test4.remote);
        echo = agent.getId();
        agent.deploy(getId());
        sendTo(getId(), new Notification());
        break;
      case 1:
        TestCase.assertEquals("step#" + state, getId(), from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");
        Thread.currentThread().sleep(test4.pause);
        TestCase.startAgentServer(test4.remote);
        break;
      case 2:
        TestCase.assertEquals("step#" + state,
                              from,
                              new AgentId(test4.remote,
                                          test4.remote,
                                          AgentId.FactoryIdStamp));
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.AgentCreateReply");
        sendTo(echo, new Notification());
        break;
      case 3:
        TestCase.assertEquals("step#" + state, echo, from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");
        TestCase.endTest();
        // never reached
      }
      state += 1;
    } catch (Exception exc) {
      TestCase.error(exc);
      TestCase.endTest();
    }
  }
}
