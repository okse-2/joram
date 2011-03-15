/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2008 - 2010 ScalAgent Distributed Technologies
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
import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Notification;
import framework.TestCase;

/**
 * This test verify the sending the network behavior with a great number of
 * servers.
 */
public class test21 extends TestCase {
  public test21() {
    super();
  }

  int nb = 10;
  boolean router = false;

  protected void setUp() throws Exception {
    router = new Boolean(System.getProperty("Router", "false")).booleanValue();
    System.out.println("Router=" + router);
    
    nb  = Integer.getInteger("nb", nb).intValue();
    System.out.println("nb=" + nb);
    
    for (int i=1; i<nb; i++) {
      startAgentServer((short) i);
    }
    if (router) startAgentServer((short) nb);

    Thread.sleep(2000L);

    int bounce = Integer.getInteger("bounce", 500).intValue();
    timeout = 1000 * bounce;

    Coordinator coordinator = new Coordinator(nb-1);
    coordinator.deploy();
    
    for (int i=1; i<nb; i++) {
      Ping ping = new Ping((short) 0);
      Pong pong = new Pong((short) i);

      ping.pong = pong.getId();
      ping.coordinator = coordinator.getId();
      ping.bounce = bounce;

      ping.deploy();
      pong.deploy();
    }
  }

  protected void tearDown() {
    for (int i=1; i<nb; i++) {
      crashAgentServer((short) i);
    }
    if (router) crashAgentServer((short) nb);
  }


  public static void main(String args[]) {
    new test21().runTest(args);
  }

  static class Ball extends Notification {
    public int bounce;

    public Ball(int bounce) {
      this.bounce = bounce;
    }

    public StringBuffer toString(StringBuffer output) {
      output.append('(');
      super.toString(output);
      output.append(",bounce=").append(bounce);
      output.append(')');

      return output;
    }
  }

  static class StopNot extends Notification {
  }
  
  static class Coordinator extends Agent {
    int nb;
    
    public int getNb() {
      return nb;
    }
    
    public Coordinator(int nb) {
      this.nb = nb;
    }
    
    public void react(AgentId from, Notification not) {
      if (not instanceof StopNot) {
        nb -= 1;
        if (nb == 0) endTest();
        // never reached
      }
    }
  }
  
  static class Ping extends Agent {
    AgentId pong;
    AgentId coordinator;
    int bounce;
    int errors;

    public int getBounce() {
      return bounce;
    }
    
    public Ping(short to) {
      super(to);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime)
        sendTo(pong, new Ball(bounce));
    }

    public void react(AgentId from, Notification not) {
      try {
        assertTrue(from.equals(pong));
        assertEquals(not.getClass().getName(),
                     "a3.base.test21$Ball");
        if (not instanceof Ball) {
          Ball ball = (Ball) not;
          assertEquals(bounce, ball.bounce);
          if (ball.bounce == 0) {
            sendTo(coordinator, new StopNot());
          } else {
            bounce -= 1;
            sendTo(pong, new Ball(bounce));
          }
        }
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
  }

  static class Pong extends Agent {
    public Pong(short to) {
      super(to);
    }

    public void react(AgentId from, Notification not) throws Exception {
      if (not instanceof Ball) {
        sendTo(from, new Ball(((Ball) not).bounce));
      }
    }
  }
}
