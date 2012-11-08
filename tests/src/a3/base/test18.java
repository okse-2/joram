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

import fr.dyade.aaa.agent.Agent;
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.agent.Notification;
import fr.dyade.aaa.agent.ExpiredNot;
import framework.BaseTestCase;
import framework.TestCase;

public class test18 extends TestCase {
  
  public test18() {
    super();
  }

  protected void setUp() throws Exception {
    timeout = 10000L;

    DMQAgent dmqAgent = new DMQAgent((short) 0);
    ReceiveAgent receiveAgent = new ReceiveAgent((short) 1);
    dmqAgent.deploy();
    receiveAgent.deploy();
    
    Notification not = new Notification();
    not.setDeadNotificationAgentId(dmqAgent.getId());
    Channel.sendTo(receiveAgent.getId(), not);
    
    not = new Notification();
    not.setDeadNotificationAgentId(dmqAgent.getId());
    not.setExpiration(System.currentTimeMillis() - 100);
    Channel.sendTo(receiveAgent.getId(), not);
    
    startAgentServer((short) 1);
    
  }

  public static void main(String args[]) {
    new test18().runTest(args);
  }
  
  protected void tearDown() {
    crashAgentServer((short) 1);
  }

  static class DMQAgent extends Agent {
    public DMQAgent(short to) {
      super(to);
    }

    public void react(AgentId from, Notification not) {
      BaseTestCase.assertEquals(ExpiredNot.class.getName(),
                                not.getClass().getName());
      BaseTestCase.endTest();
    }
  }

  static class ReceiveAgent extends Agent {

    public ReceiveAgent(short to) {
      super(to);
    }

    public void react(AgentId from, Notification not) {
      BaseTestCase.assertTrue(from.isNullId());
      BaseTestCase.assertEquals(Notification.class.getName(),
                                not.getClass().getName());
    }
  }
}
