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

public class test7 extends TestCase {
  public test7() {
    super();
  }

  public static short remote = (short) 1;
  public static long pause = 5000L;

  protected void setUp() throws Exception {
    pause = Long.getLong("pause", pause).longValue();
    timeout = pause +1200000L;
    Test7Agent agent = new Test7Agent();
    agent.deploy();
    Channel.sendTo(agent.getId(), new Notification());
    TestCase.startAgentServer(remote);
  }

  protected void tearDown() {
    killAgentServer(remote);
  }

  public static void main(String args[]) {
    new test7().runTest(args);
  }
}

class Test7Agent extends Agent {
  int state = 0;
  AgentId echo;

  public Test7Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    System.out.println("step#" + state + ": " + from + ", " + not);
    try {
      switch (state) {
      case 0:
        TestCase.assertTrue("step#" + state, from.isNullId());
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");

        Echo agent = new Echo(test7.remote);
        echo = agent.getId();
        agent.deploy(getId());
	System.out.println("step fin");
	//sendTo(from, new Notification());
        break;
      case 1:
        TestCase.assertEquals("step#" + state,
                              from,
                              new AgentId(test7.remote,
                                          test7.remote,
                                          AgentId.FactoryIdStamp));
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.AgentCreateReply");
	
	// Sends many messages to Echo agent in order to change local
        // and remote clock.
	sendTo(echo, new Test7Not());
	sendTo(echo, new Test7Not());
	sendTo(echo, new Test7Not());
	sendTo(echo, new Test7Not());
	sendTo(echo, new Test7Not());

        break;
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
        TestCase.assertEquals("step#" + state, echo, from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "a3.recovery.Test7Not");
 
        if (state == 6) sendTo(getId(), new Notification());

        break;
      case 7:
        TestCase.assertEquals("step#" + state, getId(), from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.Notification");

        TestCase.stopAgentServer(test7.remote);
        Thread.currentThread().sleep(test7.pause);
        TestCase.startAgentServer(test7.remote);

        agent = new Echo(test7.remote);
        echo = agent.getId();
        agent.deploy(getId());
	sendTo(echo, new Test7Not());

        break;
      case 8:
        TestCase.assertEquals("step#" + state,
                              from,
                              new AgentId(test7.remote,
                                          test7.remote,
                                          AgentId.FactoryIdStamp));
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "fr.dyade.aaa.agent.AgentCreateReply");
        break;
      case 9:
        TestCase.assertEquals("step#" + state, echo, from);
        TestCase.assertEquals("step#" + state,
                              not.getClass().getName(),
                              "a3.recovery.Test7Not");

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

class Test7Not extends Notification { }
