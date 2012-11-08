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

public class test8 extends TestCase {
  public test8() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 1200000L;
    Test8Agent agent = new Test8Agent();
    agent.deploy();
    Channel.sendTo(new AgentId((short) 1, (short) 1, 10),
                   new Notification());
    Channel.sendTo(agent.getId(), new Notification());
  }

  public static void main(String args[]) {
    new test8().runTest(args);
  }
}

class Test8Agent extends Agent {
  int state = 0;

  public Test8Agent() {
    super();
  }

  public void react(AgentId from, Notification not) {
    TestCase.assertEquals(not.getClass().getName(),
                          "fr.dyade.aaa.agent.Notification");
    if (state == 0) {
      TestCase.assertTrue(from.isNullId());
       Channel.sendTo(new AgentId((short) 1, (short) 1, 10),
                      new Notification());
       Channel.sendTo(getId(), new Notification());
    } else {
      TestCase.assertEquals(getId(), from);
      TestCase.endTest();
    }
    state += 1;
  }
}
