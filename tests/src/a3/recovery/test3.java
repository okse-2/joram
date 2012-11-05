/*
 * Copyright (C) 2001 - SCALAGENT
 */
package a3.recovery;

import fr.dyade.aaa.agent.*;
import framework.TestCase;

public class test3 extends TestCase {
  public test3() {
    super();
  }

  protected void setUp() throws Exception {
    startAgentServer((short) 1);

    timeout = Long.getLong("timeout", 60000L).longValue();

    Slave slave = new Slave((short) 1);
    Master master = new Master((short) 0);
    master.slave1 = slave.getId();

    master.deploy();
    slave.deploy();
  }

  protected void tearDown() {
    crashAgentServer((short) 1);
  }

  public static void main(String args[]) {
    new test3().runTest(args);
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
//      System.out.println("step#" + state + "->" + not);
        if (state == 0) {
          assertEquals("step#" + state, slave1, from);
          assertEquals("step#" + state, 
                       "a3.recovery.test3$Echo",
                       not.getClass().getName());
          sendTo(slave1, new DeleteNot(getId()));
        } else if (state == 1) {
          assertEquals("step#" + state,
                       new AgentId(slave1.getTo(),
                                   slave1.getTo(),
                                   AgentId.FactoryIdStamp),
                       from);
          assertEquals("step#" + state, 
                       "fr.dyade.aaa.agent.DeleteAck",
                       not.getClass().getName());
          sendTo(slave1, new Echo());
        } else if (state == 2) {
          assertTrue("step#" + state, from.isNullId());
          assertEquals("step#" + state,
                       "fr.dyade.aaa.agent.UnknownAgent",
                       not.getClass().getName());
          if (not instanceof UnknownAgent) {
            assertEquals("step#" + state,
                         slave1,
                         ((UnknownAgent) not).agent);
          }
          crashAgentServer((short) 1);
	  Thread.sleep(1000);
          startAgentServer((short) 1);

          sendTo(slave1, new Echo());
        } else if (state == 3) {
          assertTrue("step#" + state, from.isNullId());
          assertEquals("step#" + state,
                       "fr.dyade.aaa.agent.UnknownAgent",
                       not.getClass().getName());
          if (not instanceof UnknownAgent) {
            assertEquals("step#" + state,
                         slave1,
                         ((UnknownAgent) not).agent);
          }

          Slave agent = new Slave((short) 1);
          slave2 = agent.getId();
          agent.deploy();
          sendTo(slave2, new Echo());
        } else if (state == 4) {
          assertEquals("step#" + state, slave2, from);
          assertEquals("step#" + state, 
                       "a3.recovery.test3$Echo",
                       not.getClass().getName());
          stopAgentServer((short) 1);
	  // Wait in order to prevent WAIT status on TCP connection
	  Thread.currentThread().sleep(2000L);
          startAgentServer((short) 1);

          sendTo(slave2, new Echo());
        } else if (state < 100) {
          // Should be > ATransaction.NBC in order to ensure that the agent is
          // stored on disk.
          assertEquals("step#" + state, slave2, from);
          assertEquals("step#" + state, 
                       "a3.recovery.test3$Echo",
                       not.getClass().getName());
          sendTo(slave2, new Echo());
        } else if (state == 100) {
          assertEquals("step#" + state, slave2, from);
          assertEquals("step#" + state, 
                       "a3.recovery.test3$Echo",
                       not.getClass().getName());
          sendTo(slave2, new DeleteNot(getId()));
        } else if (state == 101) {
          assertEquals("step#" + state,
                       new AgentId(slave2.getTo(),
                                   slave2.getTo(),
                                   AgentId.FactoryIdStamp),
                       from);
          assertEquals("step#" + state, 
                       "fr.dyade.aaa.agent.DeleteAck",
                       not.getClass().getName());
          sendTo(slave2, new Echo());
        } else if (state == 102) {
          assertTrue("step#" + state, from.isNullId());
          assertEquals("step#" + state,
                       "fr.dyade.aaa.agent.UnknownAgent",
                       not.getClass().getName());
          if (not instanceof UnknownAgent) {
            assertEquals("step#" + state,
                         slave2,
                         ((UnknownAgent) not).agent);
          }
          sendTo(slave1, new Echo());
        } else if (state == 103) {
          assertTrue("step#" + state, from.isNullId());
          assertEquals("step#" + state,
                       "fr.dyade.aaa.agent.UnknownAgent",
                       not.getClass().getName());
          if (not instanceof UnknownAgent) {
            assertEquals("step#" + state,
                         slave1,
                         ((UnknownAgent) not).agent);
          }
          endTest();
          // never reached
        }
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
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
