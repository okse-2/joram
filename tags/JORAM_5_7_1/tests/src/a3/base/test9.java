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

public class test9 extends TestCase {
  public test9() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 1200000L;

    Slave slave = new Slave((short) 0);
    Master master = new Master((short) 0);
    master.slave1 = slave.getId();

    master.deploy();
    slave.deploy();
  }

  protected void tearDown() {
  }

  public static void main(String args[]) {
    new test9().runTest(args);
  }

  static class Echo extends Notification {}

  static class Master extends Agent {
    AgentId slave1, slave2;
    int state = 0;

    public Master(short to) {
      super(to);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        sendTo(slave1, new Echo());
      }
    }

    public void react(AgentId from, Notification not) {
      try {
        if (state == 0) {
          TestCase.assertEquals("step#" + state, slave1, from);
          TestCase.assertEquals("step#" + state, 
                                "a3.base.test9$Echo",
                                not.getClass().getName());
          sendTo(slave1, new DeleteNot(getId()));
        } else if (state == 1) {
          TestCase.assertEquals("step#" + state,
                                new AgentId(slave1.getTo(),
                                            slave1.getTo(),
                                            AgentId.FactoryIdStamp),
                                from);
          TestCase.assertEquals("step#" + state, 
                                "fr.dyade.aaa.agent.DeleteAck",
                                not.getClass().getName());
          sendTo(slave1, new Echo());
        } else if (state == 2) {
          TestCase.assertTrue("step#" + state, from.isNullId());
          TestCase.assertEquals("step#" + state,
                                "fr.dyade.aaa.agent.UnknownAgent",
                                not.getClass().getName());
          if (not instanceof UnknownAgent) {
            TestCase.assertEquals("step#" + state,
                                  slave1,
                                  ((UnknownAgent) not).agent);
          }
	
          Slave agent = new Slave((short) 0);
          slave2 = agent.getId();
          agent.deploy();
          sendTo(slave2, new Echo());
        } else if (state < 1000) {
          // Should be > ATransaction.NBC in order to ensure that the agent is
          // stored on disk.
          TestCase.assertEquals("step#" + state, slave2, from);
          TestCase.assertEquals("step#" + state, 
                                "a3.base.test9$Echo",
                                not.getClass().getName());
          sendTo(slave2, new Echo());
        } else if (state == 1000) {
          TestCase.assertEquals("step#" + state, slave2, from);
          TestCase.assertEquals("step#" + state, 
                                "a3.base.test9$Echo",
                                not.getClass().getName());
          sendTo(slave2, new DeleteNot(getId()));
        } else if (state == 1001) {
          TestCase.assertEquals("step#" + state,
                                new AgentId(slave2.getTo(),
                                            slave2.getTo(),
                                            AgentId.FactoryIdStamp),
                                from);
          TestCase.assertEquals("step#" + state, 
                                "fr.dyade.aaa.agent.DeleteAck",
                                not.getClass().getName());
          sendTo(slave2, new Echo());
        } else if (state == 1002) {
          TestCase.assertTrue("step#" + state, from.isNullId());
          TestCase.assertEquals("step#" + state,
                                "fr.dyade.aaa.agent.UnknownAgent",
                                not.getClass().getName());
          if (not instanceof UnknownAgent) {
            TestCase.assertEquals("step#" + state,
                                  slave2,
                                  ((UnknownAgent) not).agent);
          }
          sendTo(slave1, new Echo());
        } else if (state == 1003) {
          TestCase.assertTrue("step#" + state, from.isNullId());
          TestCase.assertEquals("step#" + state,
                                "fr.dyade.aaa.agent.UnknownAgent",
                                not.getClass().getName());
          if (not instanceof UnknownAgent) {
            TestCase.assertEquals("step#" + state,
                                  slave1,
                                  ((UnknownAgent) not).agent);
          }
          TestCase.endTest();
        }
      } catch (Exception exc) {
        TestCase.error(exc);
        TestCase.endTest();
      }

//  System.out.println("step#" + state + "->" + not);
      state += 1;
    }
  }

  static class Slave extends Agent {
    Slave(short to) {
      super(to);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if (not instanceof Echo)
        sendTo(from, not);
      else
        super.react(from, not);
    }
  }
}
