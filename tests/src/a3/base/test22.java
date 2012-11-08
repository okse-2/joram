/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2008 ScalAgent Distributed Technologies
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

import fr.dyade.aaa.agent.*;
import framework.TestCase;

/**
 * This test verify the sending the network behavior with a great number of
 * servers.
 */
public class test22 extends TestCase {
  public test22() {
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

    int bounce = Integer.getInteger("bounce", 50).intValue();
    timeout = 1500 * bounce;

    Coordinator coordinator = new Coordinator(2*(nb-1));
    coordinator.deploy();
    
    for (int i=1; i<nb; i++) {
      Ping ping = new Ping((short) 0);
      Pong pong = new Pong((short) i);

      ping.pong = pong.getId();
      ping.coordinator = coordinator.getId();
      ping.bounce = bounce;
      
      pong.ping = ping.getId();
      pong.coordinator = coordinator.getId();
      pong.bounce = bounce;

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
    new test22().runTest(args);
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
    public int errors;
    
    public StopNot(int errors) {
      this.errors = errors;
    }
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
        assertEquals(((StopNot) not).errors, 0);
//        System.out.println("StopNot from " + from);
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

    public int getBounce() {
      return bounce;
    }
    
    public Ping(short to) {
      super(to);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        for (int i=bounce; i>=0; i--) {
          sendTo(pong, new Ball(i));
        }
      }
    }

    public void react(AgentId from, Notification not) {
      try {
        assertTrue(from.equals(pong));
        assertEquals(not.getClass().getName(), "a3.base.test22$Ball");
        if (not instanceof Ball) {
          Ball ball = (Ball) not;
          assertEquals("Ping" + getId(), ball.bounce, bounce);
//          System.out.println(getId() + " receives " + ball.bounce + " from " + from);
          if (ball.bounce == 0) {
            sendTo(coordinator, new StopNot(0));
          } else {
            bounce -= 1;
          }
        }
      } catch (Exception exc) {
        error(exc);
        endTest();
      }
    }
  }

  static class Pong extends Agent {
    AgentId ping;
    AgentId coordinator;
    int bounce;
    int errors = 0;

    public int getBounce() {
      return bounce;
    }
    
    public Pong(short to) {
      super(to);
    }

    protected void agentInitialize(boolean firstime) throws Exception {
      if (firstime) {
        for (int i=bounce; i>=0; i--) {
          sendTo(ping, new Ball(i));
        }
      }
    }

    public void react(AgentId from, Notification not) {
      try {
        Ball ball = (Ball) not;
        if (ball.bounce != bounce) errors += 1;
        if (ball.bounce == 0) {
          sendTo(coordinator, new StopNot(errors));
        } else {
          bounce -= 1;
        }
      } catch (Exception exc) {
        errors += 1;
      }
    }
  }
}
