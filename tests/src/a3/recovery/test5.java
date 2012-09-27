/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 - 2008 ScalAgent Distributed Technologies
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

package a3.recovery;

import fr.dyade.aaa.agent.*;
import framework.TestCase;
import a3.base.Echo;

public class test5 extends TestCase {
  public test5() {
    super();
  }

  public static short remote = (short) 1;
  public static long pause = 15000L;

  protected void setUp() throws Exception {
    pause = Long.getLong("pause", pause).longValue();
    timeout = pause +120000L;
    Test5Agent agent = new Test5Agent();
    agent.deploy();
    Channel.sendTo(agent.getId(), new Notification());
  }

  protected void tearDown() {
    killAgentServer(remote);
  }

  public static void main(String args[]) {
    new test5().runTest(args);
  }
}

class Test5Agent extends Agent {
  int state = 0;
  AgentId echo;

  public Test5Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    try {
      System.out.println("state"+state);
      switch (state) {
      case 0:
        TestCase.assertTrue("step#" + state, from.isNullId());
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");
        Echo agent = new Echo(test5.remote);
        echo = agent.getId();
        agent.deploy(getId());

        // 1st message should be sent quickly when connection occured, and
        // others should be sent slowly in order to permit to remote node
        // acknowledge the first one during this time. So in PoolCnxNetwork
        // prior to 1.17 a synchronization bug occured.
        sendTo(echo, new Test5Not(10));
        sendTo(echo, new Test5Not(50000));
        sendTo(echo, new Test5Not(50000));
        sendTo(echo, new Test5Not(50000));
        sendTo(echo, new Test5Not(50000));

        sendTo(getId(), new Notification());
        break;
      case 1:
        TestCase.assertEquals("step#" + state, getId(), from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");
        Thread.currentThread().sleep(test5.pause);
        TestCase.startAgentServer(test5.remote);

        break;
      case 2:
        TestCase.assertEquals("step#" + state,
                              from,
                              new AgentId(test5.remote,
                                          test5.remote,
                                          AgentId.FactoryIdStamp));
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.AgentCreateReply");
        sendTo(echo, new Notification());
        break;
      case 3:
      case 4:
      case 5:
      case 6:
        TestCase.assertEquals("step#" + state, echo, from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "a3.recovery.Test5Not");
        break;
      case 7:
        TestCase.assertEquals("step#" + state, echo, from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "a3.recovery.Test5Not");
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

class Test5Not extends Notification {
  int array[] = null;

  Test5Not(int size) {
    array = new int[size];
    for (int i=0; i<array.length; i++) {
      array[i] = i;
    }
  }
}
