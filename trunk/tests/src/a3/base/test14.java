/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2001 - 2009 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

public class test14 extends TestCase {
  public test14() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 15000L;

    startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false" });

    Test14Master master = new Test14Master();
    Test14Slave slave = new Test14Slave((short) 1);
    master.slave = slave.getId();
    slave.master = master.getId();
    master.deploy();
    slave.deploy();
  }

  protected void tearDown() {
    crashAgentServer((short) 1);
  }

  public static void main(String args[]) {
    new test14().runTest(args);
  }

  static class Test14Not extends Notification {
    boolean creation;

    Test14Not(boolean creation) {
      this.creation = creation;
    }
  }

  static class Test14Master extends Agent {
    int state = 0;
    AgentId slave;

    public Test14Master() {
      super();
    }

    public void react(AgentId from, Notification not) {
      try {
        switch (state) {
        case 0:
          TestCase.assertEquals(from, slave);
          TestCase.assertEquals(not.getClass().getName(),
                                "a3.base.test14$Test14Not");
          TestCase.assertTrue(((Test14Not) not).creation);
          // Stop, then start AgentServer#1
          crashAgentServer((short) 1);
          startAgentServer((short) 1, new String[] { "-DTransaction.UseLockFile=false" });
          // Then send a not to slave in order to reload it !!
          sendTo(slave, new Notification());
          break;
        case 1:
          TestCase.assertEquals(from, slave);
          TestCase.assertEquals(not.getClass().getName(),
                                "a3.base.test14$Test14Not");
          TestCase.assertFalse(((Test14Not) not).creation);
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

  static class Test14Slave extends Agent {
    AgentId master;

    public Test14Slave(short to) {
      super(to);
    }

    public void agentInitialize(boolean creation) {
      sendTo(master, new Test14Not(creation));
    }

    public void react(AgentId from, Notification not) {
    }
  }
}
